package eppic.db.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.model.dto.InputWithType;
import eppic.model.dto.JobStatusDetails;
//import eppic.model.dto.ProcessingInProgressData;
import eppic.model.shared.StatusOfJob;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.JobDAO;
//import eppic.db.dao.UserSessionDAO;
import eppic.model.db.JobDB;
import eppic.model.db.JobDB_;
import eppic.model.db.PdbInfoDB;

/**
 * Implementation of JobDAO.
 * @author AS
 *
 */
public class JobDAOJpa implements JobDAO
{
	private static final Logger logger = LoggerFactory.getLogger(JobDAOJpa.class);
	
	@Override
	public void insertNewJob(String jobId,
							 String sessionId,
							 String email,
							 String input,
							 String ip,
							 Date submissionDate,
							 int inputType,
							 StatusOfJob status,
							 String submissionId) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();

//			UserSessionDAO sessionDAO = new UserSessionDAOJpa();
//			UserSessionDB session = sessionDAO.getSession(entityManager, sessionId, ip);

			JobDB job = new JobDB();
			job.setJobId(jobId);
			job.setEmail(email);
			job.setInputName(input);
			job.setIp(ip);
			job.setStatus(status.getName());
			job.setSubmissionDate(submissionDate);
			job.setInputType(inputType);
			job.setSubmissionId(submissionId);

//			job.getUserSessions().add(session);

			entityManager.persist(job);
//			entityManager.flush();
			entityManager.getTransaction().commit();
		}
		catch(Throwable e)
		{

			try
			{
				if (entityManager!=null)
					entityManager.getTransaction().rollback();
			}
			catch(Throwable t)
			{
				logger.error("Error rolling back EntityManager",t);
			}

			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}

	@Override
	public void updateStatusOfJob(String jobId, StatusOfJob status) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();

			JobDB job = getJob(entityManager, jobId);
			job.setStatus(status.getName());
//			entityManager.merge(job);
//			entityManager.flush();
			entityManager.getTransaction().commit();
		}
		catch(Throwable e)
		{
			try
			{
				if (entityManager!=null)
					entityManager.getTransaction().rollback();
			}
			catch(Throwable t)
			{
				logger.error("Error rolling back EntityManager",t);
			}

			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}

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
			throw new DaoException(t);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
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
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}

	@Override
	public Long getNrOfJobsForIPDuringLastDay(String ip) throws DaoException
	{
		EntityManager entityManager = null;

		long nrOfJobs = 0;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			Date currentDate = new Date();
			long oneDay = 1 * 24 * 60 * 60 * 1000;
			Timestamp dayBeforeTimestamp = new Timestamp(currentDate.getTime() - oneDay);

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<JobDB> sessionRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.select(criteriaBuilder.count(sessionRoot));
			Predicate ipCondition = criteriaBuilder.equal(sessionRoot.get(JobDB_.ip), ip);
			Predicate submissionDateCondition =  criteriaBuilder.greaterThan(sessionRoot.get(JobDB_.submissionDate), dayBeforeTimestamp);
			Predicate condition = criteriaBuilder.and(ipCondition, submissionDateCondition);
			criteriaQuery.where(condition);
			Query query = entityManager.createQuery(criteriaQuery);
			
//			Query query = entityManager.createQuery("SELECT count(jobId) FROM Job WHERE ip = :ip AND submissionDate > :dayBefore", Long.class);
//			query.setParameter("ip", ip);
//			query.setParameter("dayBefore", dayBeforeTimestamp);
			nrOfJobs = (Long) query.getSingleResult();
		}
		catch(Throwable t)
		{
			throw new DaoException(t);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}

		return nrOfJobs;
	}

	@Override
	public InputWithType getInputWithTypeForJob(String jobId) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<JobDB> criteriaQuery = criteriaBuilder.createQuery(JobDB.class);

			Root<JobDB> sessionRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.multiselect(sessionRoot.get(JobDB_.inputName),
									  sessionRoot.get(JobDB_.inputType));
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(JobDB_.jobId), jobId);
			criteriaQuery.where(condition);
			
			Query query = entityManager.createQuery(criteriaQuery);
			JobDB job = (JobDB)query.getSingleResult();
			
			InputWithType inputWithType = new InputWithType(job.getInputName(), 
															job.getInputType());
			return inputWithType;
		}
		catch(Throwable e)
		{
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}

	@Override
	public void setPdbScoreItemForJob(String jobId, PdbInfoDB pdbScoreItem)
			throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			entityManager.persist(pdbScoreItem);

			JobDB job = getJob(entityManager, jobId);

			if(job != null)
			{
				pdbScoreItem.setJob(job);
				job.setPdbInfo(pdbScoreItem);
				job.setStatus(StatusOfJob.FINISHED.getName());
//				entityManager.merge(job);
			}

			entityManager.getTransaction().commit();
		}
		catch(Throwable e)
		{

			try
			{
				if (entityManager!=null)
					entityManager.getTransaction().rollback();
			}
			catch(Throwable t)
			{
				logger.error("Error rolling back EntityManager",t);
			}

			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}

	@Override
	public List<JobStatusDetails> getListOfUnfinishedJobs() throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			
			CriteriaQuery<JobDB> criteriaQuery = criteriaBuilder.createQuery(JobDB.class);
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			Predicate queuingStatusCondition = criteriaBuilder.equal(jobRoot.get(JobDB_.status), StatusOfJob.QUEUING.getName());
			Predicate runningStatusCondition = criteriaBuilder.equal(jobRoot.get(JobDB_.status), StatusOfJob.RUNNING.getName());
			Predicate waitingStatusCondition = criteriaBuilder.equal(jobRoot.get(JobDB_.status), StatusOfJob.WAITING.getName());
			Predicate condition = criteriaBuilder.or(queuingStatusCondition, runningStatusCondition, waitingStatusCondition);
			criteriaQuery.where(condition);
			criteriaQuery.select(jobRoot);

			Query query = entityManager.createQuery(criteriaQuery);
			@SuppressWarnings("unchecked")
			List<JobDB> jobs = query.getResultList();

			List<JobStatusDetails> processingInProgressDataList = null;
			if(jobs != null)
			{
				processingInProgressDataList = new ArrayList<JobStatusDetails>();

				for(JobDB job : jobs)
				{
					processingInProgressDataList.add(createJobStatusDetails(job));
				}
			}

			return processingInProgressDataList;
		}
		catch(Throwable e)
		{
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
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
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}
	
	private JobStatusDetails createJobStatusDetails(JobDB job)
	{
		JobStatusDetails jobStatusDetails = null;

		if(job != null)
		{
			jobStatusDetails = new JobStatusDetails(job.getJobId(),
													job.getStatus(),
													job.getInputName(),
													job.getEmail(),
													job.getSubmissionId());
		}

		return jobStatusDetails;
	}
	
	private JobDB getJob(EntityManager entityManager,
			 			 String jobId) throws DaoException
	{
		JobDB job = null; 
		
		try
		{
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<JobDB> criteriaQuery = criteriaBuilder.createQuery(JobDB.class);
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.where(criteriaBuilder.equal(jobRoot.get(JobDB_.jobId), jobId));
			criteriaQuery.select(jobRoot);
			
			TypedQuery<JobDB> query = entityManager.createQuery(criteriaQuery);
			job = query.getSingleResult();
			
		}
		catch(Throwable e)
		{
			throw new DaoException(e);
		}
		
		return job;
	}

	@Override
	public JobDB getJob(String jobId) throws DaoException {
		return getJob(EntityManagerHandler.getEntityManager(),jobId);
	}

	@Override
	public boolean isJobsEmpty() {

		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<JobDB> criteriaQuery = criteriaBuilder.createQuery(JobDB.class);
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.select(jobRoot);

			List<JobDB> jobs = entityManager.createQuery(criteriaQuery)
					.setFirstResult(0) // offset
					.setMaxResults(1) // limit
					.getResultList();

			if (jobs == null)
				return true;

			return jobs.size() == 0;
		}
		catch(Throwable e)
		{
			return true;
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}

	}
}
