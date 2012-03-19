package ch.systemsx.sybit.crkwebui.server.db.model;

import javax.persistence.EntityManager;

import model.UserSessionDB;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

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
	 * @throws CrkWebException
	 */
	public void insertSessionForJob(String sessionId, String jobId) throws CrkWebException;
	
	/**
	 * Retrieves session object for specified session id.
	 * @param entityManager entity manager
	 * @param sessionId identifier of the session
	 * @return session object with specified id
	 * @throws CrkWebException
	 */
	public UserSessionDB getSession(EntityManager entityManager, String sessionId) throws CrkWebException;
}
