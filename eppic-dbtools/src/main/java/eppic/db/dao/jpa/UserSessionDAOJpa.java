package eppic.db.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eppic.model.db.JobDB_;
import eppic.model.db.UserSessionDB_;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.UserSessionDAO;
import eppic.model.db.JobDB;
import eppic.model.db.UserSessionDB;

/**
 * Implementation of UserSessionDAO.
 * @author AS
 */
public class UserSessionDAOJpa implements UserSessionDAO 
{
	@Override
	public void insertSessionForJob(String sessionId, String jobId, String ip) throws DaoException
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
			
			UserSessionDB session = getSession(entityManager, sessionId, ip);
			session.getJobs().add(job);
//			entityManager.merge(session);
			entityManager.getTransaction().commit();
		}
		catch(Throwable e)
		{

			if (entityManager!=null)
				entityManager.getTransaction().rollback();
			
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}
	
	@Override
	public UserSessionDB getSession(EntityManager entityManager,
									String sessionId, String ip) throws DaoException
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
			@SuppressWarnings("unchecked")
			List<UserSessionDB> sessions = query.getResultList();
			
			if((sessions != null) && (sessions.size() > 0))
			{
				session = sessions.get(0);
			}
			else
			{
				session = new UserSessionDB();
				session.setSessionId(sessionId);
				session.setTimeStamp(new Date());
				session.setIp(ip);
				entityManager.persist(session);
			}
		}
		catch(Throwable e)
		{
			throw new DaoException(e);
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
