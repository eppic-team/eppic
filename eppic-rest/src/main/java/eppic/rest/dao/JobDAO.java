package eppic.rest.dao;

import ch.systemsx.sybit.shared.model.StatusOfJob;
import eppic.model.JobDB;


/**
 * DAO interface for Job item.
 * @author AS
 *
 */
public interface JobDAO
{

	/**
	 * Retrieves status of the selected job.
	 * @param jobId identifier of the job
	 * @return status of selected job
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public StatusOfJob getStatusForJob(String jobId) throws DaoException;

	/**
	 * Retrieves type of the input for specified job identifier.
	 * @param jobId identifier of the job
	 * @return type of the input for specified job
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public int getInputTypeForJob(String jobId) throws DaoException;

	/**
	 * Retrieves input with type(pdb code/name) for specified job.
	 * @param jobId identifier of the job
	 * @return input with type(pdb code/file name)
	 * @throws DaoException when can not retrieve information from data storage
	 */
	//public InputWithType getInputWithTypeForJob(String jobId) throws DaoException;

	/**
	 * Retrieves submission id for selected job id.
	 * @param jobId identifier of the job
	 * @return submission id
	 * @throws DaoException when can not retrieve submission id
	 */
	public String getSubmissionIdForJobId(String jobId) throws DaoException;

	/**
	 * Fetch a Job from the database
	 * @param jobId identifier of the job
	 * @return
	 * @throws DaoException when can not find the job
	 */
	public JobDB getJob(String jobId) throws DaoException;
}
