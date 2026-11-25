package eppic.rest.jobs;

import com.mongodb.client.MongoDatabase;
import eppic.model.shared.StatusOfJob;
import eppic.rest.commons.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Native java implementation of JobManager. Jobs run as processes in this replica. If a {@link JobStatusStore}
 * is given, every status change is recorded there and refreshed with a periodic heartbeat, so that other
 * replicas can report the status of jobs they don't run, and can tell when the replica running a job died.
 * @author Jose Duarte
 */
public class NativeJobManager implements JobManager
{

	private static final Logger logger = LoggerFactory.getLogger(NativeJobManager.class);

	/**
	 * How often the heartbeat of this replica's unfinished jobs is written to the job status store
	 */
	static final long HEARTBEAT_INTERVAL_MS = 30 * 1000;
	/**
	 * A job that is processing according to the job status store, but whose heartbeat is older than this,
	 * is considered dead (the replica that ran it is gone) and reported as error
	 */
	static final long HEARTBEAT_STALE_MS = 3 * 60 * 1000;
	/**
	 * Time to wait on close for running tasks to record their final status
	 */
	private static final long CLOSE_TIMEOUT_MS = 15 * 1000;

	private final ExecutorService executor;
	private final ScheduledExecutorService heartbeatScheduler;

	/**
	 * Submission ids to tasks
 	 */
	private final Map<String, ShellTask> tasks;

	private final String jobsDirectory;

	private final JobStatusStore jobStatusStore;

	/**
	 * Creates instance of native job manager that doesn't record statuses in a shared store.
	 * @param jobsDirectory directory where results of jobs are stored
	 * @param numWorkers the number of worker slots
	 */
	public NativeJobManager(String jobsDirectory, int numWorkers) {
		this(jobsDirectory, numWorkers, null);
	}

	/**
	 * Creates instance of native job manager.
	 * @param jobsDirectory directory where results of jobs are stored
	 * @param numWorkers the number of worker slots
	 * @param jobStatusStore the store shared by all replicas to record job statuses in, if null statuses are
	 *                       only known to this instance
	 */
	public NativeJobManager(String jobsDirectory, int numWorkers, JobStatusStore jobStatusStore) {

		executor = Executors.newFixedThreadPool(numWorkers);
		tasks = new ConcurrentHashMap<>();

		this.jobsDirectory = jobsDirectory;
		this.jobStatusStore = jobStatusStore;

		checkJobsDirWritable();

		if (jobStatusStore != null) {
			heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "job-status-heartbeat");
				t.setDaemon(true);
				return t;
			});
			heartbeatScheduler.scheduleAtFixedRate(this::writeHeartbeat, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
		} else {
			heartbeatScheduler = null;
		}
	}

	private void writeHeartbeat() {
		List<String> unfinished = tasks.entrySet().stream()
				.filter(e -> !e.getValue().getOutput().isDone())
				.map(Map.Entry::getKey)
				.toList();
		try {
			jobStatusStore.heartbeat(unfinished);
		} catch (Exception e) {
			// catching everything: an exception would cancel the scheduled heartbeats
			logger.error("Could not write heartbeat for {} unfinished jobs. Error: {}", unfinished.size(), e.getMessage());
		}
	}

	private void checkJobsDirWritable() {
		File probeFile = new File(jobsDirectory, "051ea675-8cc6-4ad6-be18-6b1ae5753fce");
		boolean success;
		try {
			logger.info("Probing if jobs dir '{}' is writable by writing probe file '{}'", jobsDirectory, probeFile);
			probeFile.delete();
			success = probeFile.createNewFile();
		} catch (IOException e) {
			success = false;
		} finally {
			probeFile.delete();
		}
		if (!success)
			logger.error("Could not write probe file '{}' to job dir '{}'. The JobManager won't be able to write tasks outputs", probeFile, jobsDirectory);

		// can we create a dir?
		logger.info("Probing if we can create dirs under jobs dir '{}' by writing probe dir '{}'", jobsDirectory, probeFile);
		success = probeFile.mkdirs();
		if (!success)
			logger.error("Could not write probe dir '{}' under job dir '{}'. The JobManager won't be able to write tasks outputs", probeFile, jobsDirectory);
		else
			probeFile.delete();
	}

	@Override
	public String startJob(String submissionId,
						   List<String> command,
						   File jobDirectory,
						   int nrOfThreadsForSubmission,
						   MongoDatabase mongoDb,
						   EmailData emailData) throws JobHandlerException {
		try {

			if (tasks.containsKey(submissionId)) {
				throw new JobHandlerException("Submission id " +submissionId+ " has been already used in NativeJobManager");
			}

			logger.info("About to submit shell task with submissionId '{}', in jobDirectory '{}' for command: '{}'",
					submissionId, jobDirectory, command);
			ShellTask shellTask = new ShellTask(command, jobDirectory, submissionId, mongoDb, emailData, jobStatusStore);

			// recorded before the task can run, so that a later RUNNING status can't be overwritten.
			// If it can't be recorded, other replicas couldn't report the job: better to reject it now
			if (jobStatusStore != null) {
				jobStatusStore.setStatus(submissionId, StatusOfJob.QUEUING);
			}

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
				// task==null : job is not run by this instance of NativeJobManager: it is run by another replica,
				// or by a previous instance (before a restart). The shared job status store knows about it
				JobStatusStore.JobStatusRecord jobStatusRecord = jobStatusStore == null ? null : jobStatusStore.get(submissionId);
				if (jobStatusRecord != null) {
					statusOfJob = statusFromRecord(submissionId, jobStatusRecord);
				}
				// no record: a job submitted before the store existed (or without one). We still try to see if we
				// can find it in file system, in case the process kept running and could finish
				else if(checkIfJobWasFinishedSuccessfully(submissionId)) {
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

					// the process started at some point: running, or past it and writing results to db
					if (task.getExecutionTime() > 0) {
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

	/**
	 * Gets the status of a job run by another replica from its record in the job status store. A job that is
	 * processing but whose heartbeat is stale is reported as error: the replica running it is gone.
	 */
	static StatusOfJob statusFromRecord(String submissionId, JobStatusStore.JobStatusRecord jobStatusRecord) {
		StatusOfJob status = jobStatusRecord.status();
		boolean processing = status == StatusOfJob.QUEUING || status == StatusOfJob.RUNNING || status == StatusOfJob.WAITING;
		if (processing) {
			long heartbeatAge = jobStatusRecord.heartbeatAt() == null ?
					Long.MAX_VALUE : System.currentTimeMillis() - jobStatusRecord.heartbeatAt().getTime();
			if (heartbeatAge > HEARTBEAT_STALE_MS) {
				logger.warn("Job '{}' is {} in job status store, but its last heartbeat from host '{}' was {} s ago. Considering it ERROR",
						submissionId, status, jobStatusRecord.host(), heartbeatAge / 1000);
				return StatusOfJob.ERROR;
			}
		}
		return status;
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
			if (heartbeatScheduler != null) {
				heartbeatScheduler.shutdownNow();
			}
			// interrupts the running tasks: they kill their processes and record themselves as error
			executor.shutdownNow();
			if (!executor.awaitTermination(CLOSE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
				logger.warn("Some running tasks did not terminate within {} s", CLOSE_TIMEOUT_MS / 1000);
			}
			// the queued tasks will never run
			for (ShellTask task : tasks.values()) {
				if (!task.isStarted() && !task.getOutput().isDone()) {
					task.abortBeforeStart();
				}
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new JobHandlerException(e);
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
