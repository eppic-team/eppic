package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;
import eppic.model.shared.StatusOfJob;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

	private int submissionId;
	private ExecutorService executor;
	private Map<String, Future<Integer>> jobsStatus;
	/**
	 * Map of submission ids to job ids
	 */
	private Map<String, String> jobs;

	private String jobsDirectory;

	/**
	 * Creates instance of drmaa job manager.
	 * @param jobsDirectory directory where results of jobs are stored
	 * @throws JobManagerException when session can not be initialized
	 */
	public NativeJobManager(String jobsDirectory) throws JobManagerException
	{

		executor = Executors.newFixedThreadPool(numWorkers);
		jobs = new HashMap<>();
		jobsStatus = new HashMap<>();

		this.jobsDirectory = jobsDirectory;
		this.submissionId = 0;
	}

	@Override
	public String startJob(String javaVMExec,
			               String jobId,
						   List<String> command,
						   String jobDirectory,
						   int nrOfThreadsForSubmission) throws JobHandlerException
	{
		try
		{

			//jobTemplate.setJobName(jobId);
			File stdErr = new File(jobDirectory, jobId + ".e");
			File stdOut = new File(jobDirectory, jobId + ".o");

			List<String> cmd = new ArrayList<>();
			cmd.add(javaVMExec);
			cmd.addAll(command);

			ShellTask shellTask = new ShellTask(cmd, stdOut, stdErr);
			Future<Integer> future = executor.submit(shellTask);

			jobsStatus.put(jobId, future);

			submissionId++;
			jobs.put(String.valueOf(submissionId), jobId);

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
		StatusOfJob statusOfJob = StatusOfJob.QUEUING;

		try
		{
			Future<Integer> future = jobsStatus.get(jobId);

			if (future.isCancelled()) {
				statusOfJob = StatusOfJob.STOPPED;
			} else if (future.isDone()) {
				if(checkIfJobWasFinishedSuccessfully(jobId)) {
					statusOfJob = StatusOfJob.FINISHED;
				} else {
					statusOfJob = StatusOfJob.ERROR;
				}
			} else {
				// TODO this can be running or waiting...
				statusOfJob = StatusOfJob.RUNNING;
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
		try
		{
			if((submissionId != null) &&
				(session.getJobProgramStatus(submissionId) != Session.FAILED) &&
				(session.getJobProgramStatus(submissionId) != Session.DONE))
			{
				session.control(submissionId, Session.TERMINATE);
			}
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
}
