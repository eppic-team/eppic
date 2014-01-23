package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

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
import ch.systemsx.sybit.crkwebui.server.db.dao.UserSessionDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;

/**
 * Implementation of UserSessionDAO.
 * @author AS
 */
public class UserSessionDAOJpa implements UserSessionDAO 
{
	@Override
	public void insertSessionForJob(String sessionId, String jobId) throws DaoException
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
//			entityManager.merge(session);
			entityManager.getTransaction().commit();
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
				t.printStackTrace();
			}
			
			
			throw new DaoException(e);
		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	@Override
	public UserSessionDB getSession(EntityManager entityManager,
									String sessionId) throws DaoException
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
		}
		catch(Throwable e)
		{
			e.printStackTrace();
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
