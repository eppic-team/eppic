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
import model.UserSessionDB;
import model.UserSessionDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public class UserSessionDAOImpl implements UserSessionDAO 
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
			
			UserSessionDB session = getSession(entityManager, sessionId);
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
	
	public UserSessionDB getSession(EntityManager entityManager,
								String sessionId) throws CrkWebException
	{
		UserSessionDB session = null; 
		
		try
		{
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<UserSessionDB> criteriaQuery = criteriaBuilder.createQuery(UserSessionDB.class);
			Root<UserSessionDB> sessionRoot = criteriaQuery.from(UserSessionDB.class);
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(UserSessionDB_.sessionId), sessionId);
			criteriaQuery.where(condition);
			criteriaQuery.select(sessionRoot);
			
			Query query = entityManager.createQuery(criteriaQuery);
			List<UserSessionDB> sessions = query.getResultList();
			
			if((sessions != null) && (sessions.size() > 0))
			{
				session = sessions.get(0);
			}
			else
			{
				session = new UserSessionDB();
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
