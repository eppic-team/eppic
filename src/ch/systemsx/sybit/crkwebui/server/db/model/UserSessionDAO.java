package ch.systemsx.sybit.crkwebui.server.db.model;

import javax.persistence.EntityManager;

import model.UserSessionDB;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public interface UserSessionDAO 
{
	public void insertSessionForJob(String sessionId, String jobId) throws CrkWebException;
	
	public UserSessionDB getSession(EntityManager entityManager, String sessionId) throws CrkWebException;
}
