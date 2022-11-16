package eppic.rest.jobs;

import com.mongodb.client.MongoDatabase;
import eppic.model.shared.StatusOfJob;
import eppic.rest.commons.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Native java implementation of JobManager.
 * @author Jose Duarte
 */
public class NativeJobManager implements JobManager
{

	private static final Logger logger = LoggerFactory.getLogger(NativeJobManager.class);
	private final ExecutorService executor;

	/**
	 * Submission ids to tasks
 	 */
	private final Map<String, ShellTask> tasks;

	private final String jobsDirectory;

	/**
	 * Creates instance of native job manager.
	 * @param jobsDirectory directory where results of jobs are stored
	 * @param numWorkers the number of worker slots
	 */
	public NativeJobManager(String jobsDirectory, int numWorkers) {

		executor = Executors.newFixedThreadPool(numWorkers);
		tasks = new HashMap<>();

		this.jobsDirectory = jobsDirectory;
	}

	@Override
	public String startJob(String submissionId,
						   List<String> command,
						   File jobDirectory,
						   String baseNameForOutput,
						   int nrOfThreadsForSubmission,
						   MongoDatabase mongoDb,
						   EmailData emailData) throws JobHandlerException {
		try {

			if (tasks.containsKey(submissionId)) {
				throw new JobHandlerException("Submission id " +submissionId+ " has been already used in NativeJobManager");
			}

			ShellTask shellTask = new ShellTask(command, jobDirectory, baseNameForOutput, submissionId, mongoDb, emailData);
			Future<Integer> future = executor.submit(shellTask);

			shellTask.setOutput(future);
			tasks.put(submissionId, shellTask);

	      	return submissionId;
		}
		catch(Exception e) {
			throw new JobHandlerException(e);
		}
	}

	@Override
	public StatusOfJob getStatusOfJob(String submissionId) throws JobHandlerException {
		StatusOfJob statusOfJob;

		try {
			ShellTask task = tasks.get(submissionId);
			if (task == null) {
				// task==null : job is not present in this instance of NativeJobManager, which happens if jetty was restarted
				// we still try to see if we can find it in file system, in case the process kept running and could finish
				if(checkIfJobWasFinishedSuccessfully(submissionId)) {
					logger.info("Job '{}' is unknown to the native job manager. However its finish file was found. Considering it FINISHED", submissionId);
					statusOfJob = StatusOfJob.FINISHED;
				} else {
					// can't say much more. It could be running, it could be stopped, it could be in error. Let's just say we don't know about it
					logger.info("Job '{}' is unknown to the native job manager and its finish file could not be found. Considering it NONEXISTING", submissionId);
					statusOfJob = StatusOfJob.NONEXISTING;
				}
			} else {
				Future<Integer> future = task.getOutput();

				if (future.isCancelled()) {
					statusOfJob = StatusOfJob.STOPPED;
				} else if (future.isDone()) {

					int finishStatus = future.get();
					if (finishStatus == 0) {
						statusOfJob = StatusOfJob.FINISHED;
					} else if (finishStatus == ShellTask.CANT_START_PROCESS_ERROR_CODE) {
						logger.warn("Something went wrong when starting job execution for job {}", submissionId);
						statusOfJob = StatusOfJob.ERROR;
					} else if (finishStatus == ShellTask.SIGTERM_ERROR_CODE) {
						logger.info("The job '{}' was stopped", submissionId);
						statusOfJob = StatusOfJob.STOPPED;
					} else {
						logger.info("Job {} reported non-0 exit status {}", submissionId, finishStatus);
						statusOfJob = StatusOfJob.ERROR;
					}


				} else {

					if (task.isRunning()) {
						statusOfJob = StatusOfJob.RUNNING;
					} else {
						statusOfJob = StatusOfJob.QUEUING;
					}

				}
			}

		}
		catch (Exception e) {
			throw new JobHandlerException(e);
		}

		return statusOfJob;
	}

	@Override
	public void stopJob(String submissionId) throws JobHandlerException {
		ShellTask shellTask = tasks.get(submissionId);
		if (shellTask == null) {
			throw new JobHandlerException("Task with submissionId "+submissionId+" was not found. Something is wrong!");
		}
		try {
			shellTask.stop();
		}
		catch (Exception t) {
			throw new JobHandlerException(t);
		}
	}

	/**
	 * Checks whether directory for specified job has been generated.
	 * @param jobId identifier of the job
	 * @return information whether directory for specified jobid exists
	 */
	private boolean checkIfJobWasSubmitted(String jobId) {
		boolean wasJobSubmitted = false;

		File jobDirectory = new File(jobsDirectory, jobId);

		if(jobDirectory.exists()) {
			wasJobSubmitted = true;
		}

		return wasJobSubmitted;
	}

	/**
	 * Checks if job was finished successfully. If job was finished successfully then finished file exists
	 * in job directory.
	 *
	 * @param submissionId identifier of the job
	 * @return information whether job was finished successfully
	 */
	private boolean checkIfJobWasFinishedSuccessfully(String submissionId) {
		boolean wasJobFinishedSuccessfully = false;

		File finishedJobDirectory = new File(jobsDirectory, submissionId);
		File finishedJobFile = new File(finishedJobDirectory, AppConstants.FINISHED_FILE_NAME);

		if(finishedJobFile.exists()) {
			wasJobFinishedSuccessfully = true;
		}

		return wasJobFinishedSuccessfully;
	}

	/**
	 * Checks if job was stopped. If job was stopped then killed file exists
	 * in job directory.
	 *
	 * @param jobId identifier of the job
	 * @return information whether job was stopped
	 */
	private boolean checkIfJobWasStopped(String jobId) {
		boolean wasStopped = false;

		File stoppedJobDirectory = new File(jobsDirectory, jobId);
		File stoppedJobFile = new File(stoppedJobDirectory, AppConstants.KILLED_FILE_NAME);

		if(stoppedJobFile.exists()) {
			wasStopped = true;
		}

		return wasStopped;
	}

	@Override
	public void close() throws JobHandlerException {
		try {
			logger.info("Shutting down native job manager");
			executor.shutdownNow();
		}
		catch (Exception e) {
			throw new JobHandlerException(e);
		}
	}

	/**
	 * Clear from queue jobs submitted more than the given milliseconds ago and that are not running or queuing.
	 * @param millisecondsAgo remove anything older than this number of milliseconds ago
	 */
	public void clearQueue(long millisecondsAgo) {
		Iterator<String> it = tasks.keySet().iterator();
		long maxTime = System.currentTimeMillis() - millisecondsAgo;
		while (it.hasNext()) {
			String submissionId = it.next();
			ShellTask task = tasks.get(submissionId);
			try {
				StatusOfJob statusOfJob = getStatusOfJob(submissionId);
				if (task.getSubmissionTime() < maxTime &&
						statusOfJob != StatusOfJob.RUNNING && statusOfJob != StatusOfJob.QUEUING) {
					it.remove();
				}
			} catch (JobHandlerException e) {
				logger.warn("Could not get job status for job with submissionId '{}'. Will not remove it.", submissionId);
			}
		}
	}

	/**
	 * Gets the size of all jobs ever submitted to this job manager.
	 * @return the size
	 */
	public int getSize() {
		return tasks.size();
	}

	public void logJobHistory() {
		logger.info("A total of {} jobs are in job history", getSize());
		for (String submId : tasks.keySet()) {
			ShellTask task = tasks.get(submId);
			try {
				StatusOfJob statusOfJob = getStatusOfJob(submId);
				logger.info("Job with submission id {} has status {}. It was queuing {} s and executing {} s",
						submId, statusOfJob, task.getTimeInQueue() / 1000, task.getTimeRunning() / 1000);
			} catch (JobHandlerException e) {
				logger.warn("Can not log info for job with submission id '{}'. Error: {}", submId, e.getMessage());
			}
		}
	}
}
