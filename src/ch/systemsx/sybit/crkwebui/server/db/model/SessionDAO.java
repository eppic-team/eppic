package ch.systemsx.sybit.crkwebui.server.db.model;

import javax.persistence.EntityManager;

import model.SessionDB;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public interface SessionDAO 
{
	public void insertSessionForJob(String sessionId, String jobId) throws CrkWebException;
	
	public SessionDB getSession(EntityManager entityManager, String sessionId) throws CrkWebException;
}
