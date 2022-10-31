package eppic.rest.jobs;

import java.util.List;

import eppic.model.shared.StatusOfJob;

/**
 * Job manager.
 * @author AS
 *
 */
public interface JobManager {
	/**
	 * Starts new job.
	 * @param jobId identifier of the job to submit
	 * @param command command to execute
	 * @param jobDirectory base directory where results of jobs are to be stored
	 * @param nrOfThreadsForSubmission nr of threads used to run command
	 * @return submissionId
	 * @throws JobHandlerException when job can not be successfully started
	 */
	String startJob(String jobId,
					List<String> command,
					String jobDirectory,
					int nrOfThreadsForSubmission) throws JobHandlerException;

	/**
	 * Retrieves current status of specified job.
	 * @param submissionId submission identifier of the job
	 * @return status of the job
	 * @throws JobHandlerException when can not retrieve current status of the job
	 */
	StatusOfJob getStatusOfJob(String submissionId) throws JobHandlerException;

	/**
	 * Stops execution of the job.
	 * @param submissionId submission identifier of the job to stop
	 * @throws JobHandlerException when can not successfully stop the job
	 */
	void stopJob(String submissionId) throws JobHandlerException;

	/**
	 * Shutdown job manager.
	 * @throws JobHandlerException when finalization of the resources fails
	 */
	void close() throws JobHandlerException;
}
