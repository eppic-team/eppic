package ch.systemsx.sybit.crkwebui.server.db.dao;

import javax.persistence.EntityManager;

import model.UserSessionDB;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;

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
	 * @throws DaoException when can not insert session for job
	 */
	public void insertSessionForJob(String sessionId, String jobId) throws DaoException;
	
	/**
	 * Retrieves session object for specified session id.
	 * @param entityManager entity manager
	 * @param sessionId identifier of the session
	 * @return session object with specified id
	 * @throws DaoException when can not retrieve session object
	 */
	public UserSessionDB getSession(EntityManager entityManager, String sessionId) throws DaoException;
}
