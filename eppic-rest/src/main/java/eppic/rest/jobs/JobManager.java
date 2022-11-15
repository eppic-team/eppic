package eppic.rest.jobs;

import java.io.File;
import java.util.List;

import com.mongodb.client.MongoDatabase;
import eppic.model.shared.StatusOfJob;

/**
 * Job manager.
 * @author AS
 *
 */
public interface JobManager {
	/**
	 * Starts new job.
	 * @param submissionId identifier of the job to submit, jobs with submissionIds already seen by manager are rejected
	 * @param command command to execute
	 * @param jobDirectory base directory where results of jobs are to be stored
	 * @param baseNameForOutput the base name for files produced by EPPIC CLI
	 * @param nrOfThreadsForSubmission nr of threads used to run command
	 * @param mongoDb Mongo db to write job's output data to
	 * @param email email address to notify when job finishes
	 * @return submissionId
	 * @throws JobHandlerException when job can not be successfully started, or when submissionId not unique
	 */
	String startJob(String submissionId,
					List<String> command,
					File jobDirectory,
					String baseNameForOutput,
					int nrOfThreadsForSubmission,
					MongoDatabase mongoDb,
					String email) throws JobHandlerException;

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
