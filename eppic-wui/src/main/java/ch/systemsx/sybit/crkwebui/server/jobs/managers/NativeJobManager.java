package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import eppic.model.shared.StatusOfJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
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
	private int submissionId;
	private ExecutorService executor;

	/**
	 * Submission ids to tasks
 	 */
	private Map<String, ShellTask> tasks;

	private String jobsDirectory;

	/**
	 * Creates instance of native job manager.
	 * @param jobsDirectory directory where results of jobs are stored
	 * @param numWorkers the number of worker slots
	 */
	public NativeJobManager(String jobsDirectory, int numWorkers) {

		executor = Executors.newFixedThreadPool(numWorkers);
		//jobs = new HashMap<>();
		tasks = new HashMap<>();

		this.jobsDirectory = jobsDirectory;
		this.submissionId = 0;
	}

	@Override
	public String startJob(String jobId,
						   List<String> command,
						   String jobDirectory,
						   int nrOfThreadsForSubmission) throws JobHandlerException
	{
		try
		{

			//jobTemplate.setJobName(jobId);
			File stdErr = new File(jobDirectory, jobId + ".e");
			File stdOut = new File(jobDirectory, jobId + ".o");

			ShellTask shellTask = new ShellTask(command, stdOut, stdErr, jobId);
			Future<Integer> future = executor.submit(shellTask);

			submissionId++;
			//jobs.put(String.valueOf(submissionId), jobId);
			shellTask.setOutput(future);
			tasks.put(String.valueOf(submissionId), shellTask);

	      	return String.valueOf(submissionId);
		}
		catch(Throwable e)
		{
			throw new JobHandlerException(e);
		}
	}

	@Override
	public StatusOfJob getStatusOfJob(String jobId, String submissionId) throws JobHandlerException
	{
		StatusOfJob statusOfJob;

		try
		{
			Future<Integer> future = tasks.get(submissionId).getOutput();

			if (future.isCancelled()) {
				statusOfJob = StatusOfJob.STOPPED;
			} else if (future.isDone()) {

				int finishStatus = future.get();
				if (finishStatus == 0) {
					statusOfJob = StatusOfJob.FINISHED;
					// TODO do we also want to check for the finished file??
//					if(checkIfJobWasFinishedSuccessfully(jobId)) {
//						statusOfJob = StatusOfJob.FINISHED;
//					} else {
//						logger.warn("Job {} reported success but the finish file could not be found. Considering it in error state", jobId);
//						statusOfJob = StatusOfJob.ERROR;
//					}
				} else if (finishStatus == ShellTask.CANT_START_PROCESS_ERROR_CODE) {
					logger.warn("Something went wrong when starting job execution for job {}", jobId);
					statusOfJob = StatusOfJob.ERROR;
				} else if (finishStatus == ShellTask.SIGTERM_ERROR_CODE) {
					logger.warn("The job '{}' was stopped", jobId);
					statusOfJob = StatusOfJob.STOPPED;
				} else {
					logger.warn("Job {} reported non-0 exit status {}", jobId, finishStatus);
					statusOfJob = StatusOfJob.ERROR;
				}


			} else {

				ShellTask task = tasks.get(submissionId);
				if (task.isRunning()) {
					statusOfJob = StatusOfJob.RUNNING;
				} else {
					statusOfJob = StatusOfJob.QUEUING;
				}

			}

		}
		catch (Throwable e)
		{
			throw new JobHandlerException(e);
		}

		return statusOfJob;
	}

	@Override
	public void stopJob(String submissionId) throws JobHandlerException
	{
		ShellTask shellTask = tasks.get(submissionId);
		if (shellTask == null) {
			throw new JobHandlerException("Task with submissionId "+submissionId+" was not found. Something is wrong!");
		}
		try
		{
			shellTask.stop();
		}
		catch (Throwable t)
		{
			throw new JobHandlerException(t);
		}
	}

	/**
	 * Checks whether directory for specified job has been generated.
	 * @param jobId identifier of the job
	 * @return information whether directory for specified jobid exists
	 */
	private boolean checkIfJobWasSubmitted(String jobId)
	{
		boolean wasJobSubmitted = false;

		File jobDirectory = new File(jobsDirectory, jobId);

		if(jobDirectory.exists())
		{
			wasJobSubmitted = true;
		}

		return wasJobSubmitted;
	}

	/**
	 * Checks if job was finished successfully. If job was finished successfully then finished file exists
	 * in job directory.
	 *
	 * @param jobId identifier of the job
	 * @return information whether job was finished successfully
	 */
	private boolean checkIfJobWasFinishedSuccessfully(String jobId)
	{
		boolean wasJobFinishedSuccessfully = false;

		File finishedJobDirectory = new File(jobsDirectory, jobId);
		File finishedJobFile = new File(finishedJobDirectory, CrkWebServiceImpl.FINISHED_FILE_NAME);

		if(finishedJobFile.exists())
		{
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
	private boolean checkIfJobWasStopped(String jobId)
	{
		boolean wasStopped = false;

		File stoppedJobDirectory = new File(jobsDirectory, jobId);
		File stoppedJobFile = new File(stoppedJobDirectory, CrkWebServiceImpl.KILLED_FILE_NAME);

		if(stoppedJobFile.exists())
		{
			wasStopped = true;
		}

		return wasStopped;
	}

	@Override
	public void finalize() throws JobHandlerException
	{
		try
		{
			executor.shutdownNow();
		}
		catch (Throwable e)
		{
			throw new JobHandlerException(e);
		}
	}

	/**
	 * Clear from queue jobs submitted before the given date and that are not running or queuing.
	 * @param maxDateToKeep jobs before this date will be removed
	 */
	public void clearQueue(Date maxDateToKeep) {
		Iterator<String> it = tasks.keySet().iterator();
		while (it.hasNext()) {
			String submissionId = it.next();
			ShellTask task = tasks.get(submissionId);
			try {
				StatusOfJob statusOfJob = getStatusOfJob(task.getJobId(), submissionId);
				if (task.getSubmissionDate().after(maxDateToKeep) &&
						statusOfJob != StatusOfJob.RUNNING && statusOfJob != StatusOfJob.QUEUING) {
					it.remove();
				}
			} catch (JobHandlerException e) {
				logger.warn("Could not get job status for jobId '{}', submissionId '{}'. Will not remove it.",
						task.getJobId(), submissionId);
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
}
