package eppic.rest.dao;

import eppic.model.JobDB;


/**
 * DAO interface for Job item.
 * @author AS
 *
 */
public interface JobDAO {



	/**
	 * Fetch a Job from the database
	 * @param jobId identifier of the job
	 * @return
	 * @throws DaoException when can not find the job
	 */
	JobDB getJob(String jobId) throws DaoException;
}
