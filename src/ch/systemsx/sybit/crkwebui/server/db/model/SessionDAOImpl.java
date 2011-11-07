package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import model.JobDB;
import model.JobDB_;
import model.SessionDB;
import model.SessionDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public class SessionDAOImpl implements SessionDAO 
{
	public void insertSessionForJob(String sessionId, String jobId) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<JobDB> criteriaQuery = criteriaBuilder.createQuery(JobDB.class);
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			Predicate condition = criteriaBuilder.equal(jobRoot.get(JobDB_.jobId), jobId);
			criteriaQuery.where(condition);
			criteriaQuery.select(jobRoot);
			
			Query query = entityManager.createQuery(criteriaQuery);
			JobDB job = (JobDB)query.getSingleResult();
			
			SessionDB session = getSession(entityManager, sessionId);
			session.getJobs().add(job);
			entityManager.merge(session);
			entityManager.getTransaction().commit();
			
//			Query query = entityManager.createQuery("from Job WHERE jobId = :jobId");
//			query.setParameter("jobId", jobId);
//			JobDB job = (JobDB) query.getSingleResult();
//			
//			if(job != null)
//			{
//				job.setSessionId(sessionId);
//			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			
			try
			{
				entityManager.getTransaction().rollback();
			}
			catch(Throwable t)
			{
				
			}
			
			
			throw new CrkWebException(e);
		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				
			}
		}
	}
	
	public SessionDB getSession(EntityManager entityManager,
								String sessionId) throws CrkWebException
	{
		SessionDB session = null; 
		
		try
		{
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<SessionDB> criteriaQuery = criteriaBuilder.createQuery(SessionDB.class);
			Root<SessionDB> sessionRoot = criteriaQuery.from(SessionDB.class);
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(SessionDB_.sessionId), sessionId);
			criteriaQuery.where(condition);
			criteriaQuery.select(sessionRoot);
			
			Query query = entityManager.createQuery(criteriaQuery);
			List<SessionDB> sessions = query.getResultList();
			
			if((sessions != null) && (sessions.size() > 0))
			{
				session = sessions.get(0);
			}
			else
			{
				session = new SessionDB();
				session.setSessionId(sessionId);
				entityManager.persist(session);
			}
			
//			Query query = entityManager.createQuery("from Job WHERE jobId = :jobId");
//			query.setParameter("jobId", jobId);
//			JobDB job = (JobDB) query.getSingleResult();
//			
//			if(job != null)
//			{
//				job.setSessionId(sessionId);
//			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			
//			try
//			{
//				entityManager.getTransaction().rollback();
//			}
//			catch(Throwable t)
//			{
//				
//			}
			
			throw new CrkWebException(e);
		}
		finally
		{
//			try
//			{
//				entityManager.close();
//			}
//			catch(Throwable t)
//			{
//				
//			}
		}
		
		return session;
	}
}
