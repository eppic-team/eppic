package eppic.db.dao;

import java.util.Date;
import java.util.List;

import eppic.model.dto.InputWithType;
import eppic.model.dto.JobStatusDetails;
//import eppic.model.dto.ProcessingInProgressData;
import eppic.model.shared.StatusOfJob;
import eppic.model.db.JobDB;
import eppic.model.db.PdbInfoDB;

/**
 * DAO interface for Job item.
 * @author AS
 *
 */
public interface JobDAO
{
	/**
	 * Persists job item.
	 * @param jobId identifier of the job
	 * @param sessionId identifier of the session
	 * @param email user email address
	 * @param input pdb code or name of submitted file
	 * @param ip ip address
	 * @param submissionDate date when job was submitted
	 * @param inputType type of the input(pdb code/file)
	 * @param status status of the job
	 * @throws DaoException when can not retrieve information from data storage
	 */
	void insertNewJob(String jobId,
							 String sessionId,
							 String email,
							 String input,
							 String ip,
							 Date submissionDate,
							 int inputType,
							 StatusOfJob status,
							 String submissionId) throws DaoException;

	/**
	 * Updates status of selected job.
	 * @param jobId identifier of the job
	 * @param stopped status of the job
	 * @throws DaoException when can not update information in data storage
	 */
	void updateStatusOfJob(String jobId, StatusOfJob stopped) throws DaoException;

	/**
	 * Removes links between jobs and selected session.
	 * @param sessionId identifier of the session
	 * @throws DaoException when can not update information in data storage
	 */
	void untieJobsFromSession(String sessionId) throws DaoException;

//	/**
//	 * Retrieves list of jobs for selected session.
//	 * @param sessionId identifier of the session
//	 * @return list of jobs for selected session
//	 * @throws DaoException when can not retrieve information from data storage
//	 */
//	List<ProcessingInProgressData> getJobsForSession(String sessionId) throws DaoException;

	/**
	 * Retrieves number of jobs for selected session.
	 * @param sessionId identifier of the session
	 * @return number of jobs for selected session
	 * @throws DaoException when can not retrieve information from data storage
	 */
	Long getNrOfJobsForSessionId(String sessionId) throws DaoException;

	/**
	 * Retrieves status of the selected job.
	 * @param jobId identifier of the job
	 * @return status of selected job
	 * @throws DaoException when can not retrieve information from data storage
	 */
	StatusOfJob getStatusForJob(String jobId) throws DaoException;

	/**
	 * Retrieves type of the input for specified job identifier.
	 * @param jobId identifier of the job
	 * @return type of the input for specified job
	 * @throws DaoException when can not retrieve information from data storage
	 */
	int getInputTypeForJob(String jobId) throws DaoException;

//	/**
//	 * Creates processing in progress instance.
//	 * @param job job data
//	 * @return processing in progress instance
//	 */
//	ProcessingInProgressData createProcessingInProgressData(JobDB job);

	/**
	 * Retrieves number of jobs for specified ip address during last day.
	 * @param ip ip address
	 * @return number of jobs for specified ip address during last day
	 * @throws DaoException when can not retrieve information from data storage
	 */
	Long getNrOfJobsForIPDuringLastDay(String ip) throws DaoException;

	/**
	 * Removes session - job link for specified job and session.
	 * @param sessionId identifier of the session
	 * @param jobToUntie identifier of the job to untie
	 * @throws DaoException when can not retrieve information from data storage
	 */
	void untieSelectedJobFromSession(String sessionId, String jobToUntie) throws DaoException;

	/**
	 * Retrieves input with type(pdb code/name) for specified job.
	 * @param jobId identifier of the job
	 * @return input with type(pdb code/file name)
	 * @throws DaoException when can not retrieve information from data storage
	 */
	InputWithType getInputWithTypeForJob(String jobId) throws DaoException;

	/**
	 * Sets pdb score item for specified job.
	 * @param jobId identifier of the job
	 * @param pdbScoreItem pdb score item
	 * @throws DaoException when can not retrieve information from data storage
	 */
	void setPdbScoreItemForJob(String jobId, PdbInfoDB pdbScoreItem) throws DaoException;

	/**
	 * Retrieves list of jobs which are not finished yet.
	 * @return list of jobs which have not been finished yet
	 * @throws DaoException when can not retrieve list of unfinished jobs
	 */
	List<JobStatusDetails> getListOfUnfinishedJobs() throws DaoException;
	
	/**
	 * Retrieves submission id for selected job id.
	 * @param jobId identifier of the job
	 * @return submission id
	 * @throws DaoException when can not retrieve submission id
	 */
	String getSubmissionIdForJobId(String jobId) throws DaoException;

	/**
	 * Fetch a Job from the database
	 * @param jobId identifier of the job
	 * @return the job object
	 * @throws DaoException when can not find the job
	 */
	JobDB getJob(String jobId) throws DaoException;

	/**
	 * Returns true if there are no jobs at all or table is missing, false otherwise.
	 * @return true if no jobs, false otherwise
	 */
	boolean isJobsEmpty();
}
