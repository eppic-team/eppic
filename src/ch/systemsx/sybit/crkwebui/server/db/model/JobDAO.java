package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.Date;
import java.util.List;

import model.JobDB;
import model.PDBScoreItemDB;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

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
	 * @throws CrkWebException
	 */
	public void insertNewJob(String jobId, 
							 String sessionId,
							 String email, 
							 String input,
							 String ip,
							 Date submissionDate,
							 int inputType) throws CrkWebException;
	
	/**
	 * Updates status of selected job.
	 * @param jobId identifier of the job
	 * @param status status of the job
	 * @throws CrkWebException
	 */
	public void updateStatusOfJob(String jobId, String status) throws CrkWebException;
	
	/**
	 * Removes links between jobs and selected session.
	 * @param sessionId identifier of the session
	 * @throws CrkWebException
	 */
	public void untieJobsFromSession(String sessionId) throws CrkWebException;
	
	/**
	 * Retrieves list of jobs for selected session.
	 * @param sessionId identifier of the session
	 * @return list of jobs for selected session
	 * @throws CrkWebException
	 */
	public List<ProcessingInProgressData> getJobsForSession(String sessionId) throws CrkWebException;
	
	/**
	 * Retrieves number of jobs for selected session.
	 * @param sessionId identifier of the session
	 * @return number of jobs for selected session
	 * @throws CrkWebException
	 */
	public Long getNrOfJobsForSessionId(String sessionId) throws CrkWebException;
	
	/**
	 * Retrieves status of the selected job.
	 * @param jobId identifier of the job
	 * @return status of selected job
	 * @throws CrkWebException
	 */
	public String getStatusForJob(String jobId) throws CrkWebException;
	
	/**
	 * Retrieves type of the input for specified job identifier.
	 * @param jobId identifier of the job
	 * @return type of the input for specified job
	 * @throws CrkWebException
	 */
	public int getInputTypeForJob(String jobId) throws CrkWebException;
	 
	/**
	 * Creates processing in progress instance.
	 * @param job job data
	 * @return processing in progress instance
	 */
	public ProcessingInProgressData createProcessingInProgressData(JobDB job);
	
	/**
	 * Retrieves number of jobs for specified ip address during last day.
	 * @param ip ip address
	 * @return number of jobs for specified ip address during last day
	 * @throws CrkWebException
	 */
	public Long getNrOfJobsForIPDuringLastDay(String ip) throws CrkWebException;
	
	/**
	 * Retrieves oldest job submission date during the last day for specified ip address.
	 * @param ip ip address
	 * @return oldest job submission date during the last day for specified ip address
	 * @throws CrkWebException
	 */
	public Date getOldestJobSubmissionDateDuringLastDay(String ip) throws CrkWebException;

	/**
	 * Removes session - job link for specified job and session.
	 * @param sessionId identifier of the session
	 * @param jobToUntie identifier of the job to untie
	 * @throws CrkWebException
	 */
	public void untieSelectedJobFromSession(String sessionId, String jobToUntie) throws CrkWebException;

	/**
	 * Retrieves input(pdb code/name) for specified job.
	 * @param jobId identifier of the job
	 * @return input(pdb code/file name)
	 * @throws CrkWebException
	 */
	public String getInputForJob(String jobId) throws CrkWebException;

	/**
	 * Sets pdb score item for specified job.
	 * @param jobId identifier of the job
	 * @param pdbScoreItem pdb score item
	 * @throws CrkWebException
	 */
	public void setPdbScoreItemForJob(String jobId, PDBScoreItemDB pdbScoreItem) throws CrkWebException;
	
}
