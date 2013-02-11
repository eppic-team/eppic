package ch.systemsx.sybit.crkwebui.server.jobs.managers.commons;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

/**
 * Job manager.
 * @author AS
 *
 */
public interface JobManager
{
	/**
	 * Starts new job.
	 * @param jobId identifier of the job to submit
	 * @param command command to execute
	 * @param jobDirectory directory where results of the job are to be stored
	 * @param nrOfThreadsForSubmission nr of threads used to run command
	 * @return submissionId
	 * @throws JobHandlerException when job can not be successfully started
	 */
	public String startJob(String jobId, 
						   List<String> command, 
						   String jobDirectory, 
						   int nrOfThreadsForSubmission) throws JobHandlerException;

	/**
	 * Retrieves current status of specified job.
	 * @param jobId identifier of the job
	 * @param submissionId submission identifier of the job
	 * @return status of the job
	 * @throws JobHandlerException when can not retrieve current status of the job
	 */
	public StatusOfJob getStatusOfJob(String jobId, String submissionId) throws JobHandlerException;

	/**
	 * Stops execution of the job.
	 * @param submissionId submission identifier of the job to stop
	 * @throws JobHandlerException when can not successfully stop the job
	 */
	public void stopJob(String submissionId) throws JobHandlerException;

	/**
	 * Shutdown job manager.
	 * @throws JobHandlerException when finalization of the resources fails
	 */
	public void finalize() throws JobHandlerException;
}
