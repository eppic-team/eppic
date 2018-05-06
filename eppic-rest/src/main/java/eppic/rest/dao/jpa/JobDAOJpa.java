package eppic.rest.dao.jpa;

import ch.systemsx.sybit.shared.model.StatusOfJob;
import eppic.model.*;
import eppic.rest.dao.DaoException;
import eppic.rest.dao.JobDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * Implementation of JobDAO.
 * @author AS
 *
 */
public class JobDAOJpa implements JobDAO
{
	private static final Logger logger = LoggerFactory.getLogger(JobDAOJpa.class);

	@Override
	public StatusOfJob getStatusForJob(String jobId) throws DaoException
	{
		String status = null;
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
			
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.select(jobRoot.get(JobDB_.status));
			criteriaQuery.where(criteriaBuilder.equal(jobRoot.get(JobDB_.jobId), jobId));
			Query query = entityManager.createQuery(criteriaQuery);
			
//			Query query = entityManager.createQuery("SELECT status FROM Job WHERE jobId = :jobId", String.class);
//			query.setParameter("jobId", jobId);
			
			@SuppressWarnings("unchecked")
			List<String> result = query.getResultList();

			if((result != null) && (result.size() > 0))
			{
				status = result.get(0);
			}
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage(),t);
			throw new DaoException(t);
		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				logger.error("Error closing EntityManager",t);
			}
		}

		return StatusOfJob.getByName(status);
	}

	@Override
	public int getInputTypeForJob(String jobId) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
			
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.select(jobRoot.get(JobDB_.inputType));
			criteriaQuery.where(criteriaBuilder.equal(jobRoot.get(JobDB_.jobId), jobId));
			Query query = entityManager.createQuery(criteriaQuery);

//			Query query = entityManager.createQuery("SELECT inputType FROM Job WHERE jobId = :jobId", Integer.class);
//			query.setParameter("jobId", jobId);
			int input = (Integer)query.getSingleResult();
			return input;
		}
		catch(Throwable e)
		{
			logger.error(e.getMessage(),e);
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
				logger.error("Error closing EntityManager",t);
			}
		}
	}

	@Override
	public String getSubmissionIdForJobId(String jobId) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

			CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.select(jobRoot.get(JobDB_.submissionId));
			criteriaQuery.where(criteriaBuilder.equal(jobRoot.get(JobDB_.jobId), jobId));
			Query query = entityManager.createQuery(criteriaQuery);
			
//			Query query = entityManager.createQuery("SELECT submissionId FROM Job WHERE jobId = :jobId", String.class);
//			query.setParameter("jobId", jobId);
			String submissionId = (String)query.getSingleResult();
			return submissionId;
		}
		catch(Throwable e)
		{
			logger.error(e.getMessage(),e);
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
				logger.error("Error closing EntityManager",t);
			}
		}
	}
	


	@Override
	public JobDB getJob(String jobId) throws DaoException {
		// TODO implement
		//return getJob(EntityManagerHandler.getEntityManager(),jobId);
		return null;
	}
}
