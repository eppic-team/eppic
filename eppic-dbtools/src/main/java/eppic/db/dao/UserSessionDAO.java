package eppic.db.dao;

import javax.persistence.EntityManager;

import eppic.model.UserSessionDB;

/**
 * DAO interface to manage sessions.
 * @author AS
 *
 */
public interface UserSessionDAO 
{
	/**
	 * Adds session link to the job.
	 * @param sessionId identifier of session
	 * @param jobId identifier of job
	 * @param ip (used only to persist it upon session creation)
	 * @throws DaoException when can not insert session for job
	 */
	public void insertSessionForJob(String sessionId, String jobId, String ip) throws DaoException;
	
	/**
	 * Retrieves session object for specified session id.
	 * @param entityManager entity manager
	 * @param sessionId identifier of the session
	 * @param ip the ip address (used only to persist it upon session creation)
	 * @return session object with specified id
	 * @throws DaoException when can not retrieve session object
	 */
	public UserSessionDB getSession(EntityManager entityManager, String sessionId, String ip) throws DaoException;
}
