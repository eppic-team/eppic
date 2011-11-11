package ch.systemsx.sybit.crkwebui.server.db.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import model.JobDB;
import model.JobDB_;
import model.PDBScoreItemDB;
import model.UserSessionDB;
import model.UserSessionDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

public class JobDAOImpl implements JobDAO
{
	public void insertNewJob(String jobId, 
							 String sessionId,
							 String email, 
							 String input,
							 String ip,
							 Date submissionDate) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			
			UserSessionDAO sessionDAO = new UserSessionDAOImpl();
			UserSessionDB session = sessionDAO.getSession(entityManager, sessionId);
			
			JobDB job = new JobDB();
			job.setJobId(jobId);
			job.setEmail(email);
			job.setInput(input);
			job.setIp(ip);
			job.setStatus(StatusOfJob.RUNNING);
			job.setSubmissionDate(submissionDate);
			
			job.getUserSessions().add(session);

			entityManager.persist(job);
			entityManager.flush();
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
	
	public void updateStatusOfJob(String jobId, String status) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE jobId = :jobId");
			query.setParameter("jobId", jobId);
			JobDB job = (JobDB) query.getSingleResult();
			job.setStatus(status);
			entityManager.merge(job);
			entityManager.flush();
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
	
	public void untieJobsFromSession(String sessionId) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<UserSessionDB> criteriaQuery = criteriaBuilder.createQuery(UserSessionDB.class);
			Root<UserSessionDB> sessionRoot = criteriaQuery.from(UserSessionDB.class);
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(UserSessionDB_.sessionId), sessionId);
			criteriaQuery.where(condition);
			criteriaQuery.select(sessionRoot);
			
			Query query = entityManager.createQuery(criteriaQuery);
			List<UserSessionDB> sessions = query.getResultList();

			entityManager.getTransaction().begin();
			
			if(sessions != null)
			{
				for(UserSessionDB session : sessions)
				{
					entityManager.remove(session);
				}
			}
			
//			Query query = entityManager.createQuery("from Job WHERE sessionId = :sessionId", JobDB.class);
//			query.setParameter("sessionId", sessionId);
//			List<JobDB> jobs = query.getResultList();
//			
//			if(jobs != null)
//			{
//				for(JobDB job : jobs)
//				{
//					job.setSessionId(null);
//					entityManager.merge(job);
//					entityManager.flush();
//					entityManager.clear();
//				}
//			}
			
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
	
	public List<ProcessingInProgressData> getJobsForSession(String sessionId) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<JobDB> criteriaQuery = criteriaBuilder.createQuery(JobDB.class);
			
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			SetJoin<JobDB, UserSessionDB> join = jobRoot.join(JobDB_.userSessions);
			Path<String> sessionIdPath = join.get(UserSessionDB_.sessionId);
			Predicate condition = criteriaBuilder.equal(sessionIdPath, sessionId);
			criteriaQuery.where(condition);
			criteriaQuery.select(jobRoot);
			
			Query query = entityManager.createQuery(criteriaQuery);
//			entityManager = EntityManagerHandler.getEntityManager();
//			Query query = entityManager.createQuery("from Job WHERE sessionId = :sessionId", JobDB.class);
//			query.setParameter("sessionId", sessionId);
			List<JobDB> jobs = query.getResultList();
			 
			List<ProcessingInProgressData> processingInProgressDataList = null;
			if(jobs != null)
			{
				processingInProgressDataList = new ArrayList<ProcessingInProgressData>();
				 
				for(JobDB job : jobs)
				{
					processingInProgressDataList.add(createProcessingInProgressData(job));
				}
			}
			
			return processingInProgressDataList;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
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
	
	public Long getNrOfJobsForSessionId(String sessionId) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery  = criteriaBuilder.createQuery(Long.class);
			
			Root<UserSessionDB> sessionRoot = criteriaQuery.from(UserSessionDB.class);
			SetJoin<UserSessionDB, JobDB> join = sessionRoot.join(UserSessionDB_.jobs);
			criteriaQuery.select(criteriaBuilder.count(join));
			
			Query query = entityManager.createQuery(criteriaQuery);
			Long nrOfJobs = (Long)query.getSingleResult();
			
//			Query query = entityManager.createQuery("SELECT count(jobId) FROM Job WHERE sessionId = :sessionId", Long.class);
//			query.setParameter("sessionId", sessionId);
//			Long nrOfJobs = (Long) query.getSingleResult();
			return nrOfJobs;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
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
	
	public String getStatusForJob(String jobId) throws PersistenceException
	{
		EntityManager entityManager = EntityManagerHandler.getEntityManager();
		Query query = entityManager.createQuery("SELECT status FROM Job WHERE jobId = :jobId", String.class);
		query.setParameter("jobId", jobId);
		String status = null;
		List<String> result = query.getResultList();
		
		if((result != null) && (result.size() > 0))
		{
			status = result.get(0);
		}
		
		entityManager.close();
		 
		return status;
	}
	 
	public ProcessingInProgressData createProcessingInProgressData(JobDB job)
	{
		ProcessingInProgressData processingInProgressData = null;
			 
		if(job != null)
		{
			processingInProgressData = new ProcessingInProgressData();
			processingInProgressData.setJobId(job.getJobId());
			processingInProgressData.setInput(job.getInput());
			processingInProgressData.setStatus(job.getStatus());
		}
		 
		return processingInProgressData;
	}

	@Override
	public Long getNrOfJobsForIPDuringLastDay(String ip) throws CrkWebException 
	{
		EntityManager entityManager = null;
		
		long nrOfJobs = 0;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			Date currentDate = new Date();
			long oneDay = 1 * 24 * 60 * 60 * 1000;
			Timestamp dayBeforeTimestamp = new Timestamp(currentDate.getTime() - oneDay);
			
			Query query = entityManager.createQuery("SELECT count(jobId) FROM Job WHERE ip = :ip AND submissionDate > :dayBefore", Long.class);
			query.setParameter("ip", ip);
			query.setParameter("dayBefore", dayBeforeTimestamp);
			nrOfJobs = (Long) query.getSingleResult();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
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
		
		return nrOfJobs;
	}
	
	public Date getOldestJobSubmissionDateDuringLastDay(String ip) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			Date currentDate = new Date();
			long oneDay = 1 * 24 * 60 * 60 * 1000;
			Timestamp dayBeforeTimestamp = new Timestamp(currentDate.getTime() - oneDay);
			
			Query query = entityManager.createQuery("SELECT MIN(submissionDate) FROM Job WHERE ip = :ip AND submissionDate > :dayBefore", Date.class);
			query.setParameter("ip", ip);
			query.setParameter("dayBefore", dayBeforeTimestamp);
			
			Date oldestJobSubmissionDateDuringLastDay  = new Date(dayBeforeTimestamp.getTime());
			
			List<Date> oldestJobSubmissionDateDuringLastDayResult = query.getResultList();
			
			if((oldestJobSubmissionDateDuringLastDayResult != null) &&
			   (oldestJobSubmissionDateDuringLastDayResult.size() > 0))
			{
				oldestJobSubmissionDateDuringLastDay = oldestJobSubmissionDateDuringLastDayResult.get(0);
			}
			
			return oldestJobSubmissionDateDuringLastDay;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
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

	@Override
	public void untieSelectedJobFromSession(String sessionId, String jobToUntie) throws CrkWebException 
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<UserSessionDB> criteriaQuery = criteriaBuilder.createQuery(UserSessionDB.class);
			
			Root<UserSessionDB> sessionRoot = criteriaQuery.from(UserSessionDB.class);
			SetJoin<UserSessionDB, JobDB> join = sessionRoot.join(UserSessionDB_.jobs);
			Path<String> jobId = join.get(JobDB_.jobId);
			Predicate sessionCondition = criteriaBuilder.equal(sessionRoot.get(UserSessionDB_.sessionId), sessionId);
			Predicate jobCondition = criteriaBuilder.equal(jobId, jobToUntie);
			Predicate condition = criteriaBuilder.and(sessionCondition, jobCondition);
			criteriaQuery.select(sessionRoot);
			criteriaQuery.where(condition);
			
			Query query = entityManager.createQuery(criteriaQuery);
			List<UserSessionDB> sessionResult = query.getResultList();

			if((sessionResult != null) && (sessionResult.size() > 0))
			{
				UserSessionDB session = sessionResult.get(0);
				
				JobDB jobToRemove = null;
				
				Set<JobDB> jobs = session.getJobs();
				Iterator<JobDB> iterator = jobs.iterator();
				
				while((iterator.hasNext()) && (jobToRemove == null))
				{
					JobDB job = iterator.next();
					if(job.getJobId().equals(jobToUntie))
					{
						jobToRemove = job;
					}
				}
				
				if(jobToRemove != null)
				{
					session.getJobs().remove(jobToRemove);
				}
				
				entityManager.merge(session);
			}
			
//			Query query = entityManager.createQuery("from Job WHERE jobId=:jobId", JobDB.class);
//			query.setParameter("jobId", jobToUntie);
//			List<JobDB> jobs = query.getResultList();
//			
//			if(jobs != null)
//			{
//				for(JobDB job : jobs)
//				{
//					job.setSessionId(null);
//					entityManager.merge(job);
//					entityManager.flush();
//					entityManager.clear();
//				}
//			}
			
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
	
	public String getInputForJob(String jobId) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			Query query = entityManager.createQuery("SELECT input FROM Job WHERE jobId = :jobId", String.class);
			query.setParameter("jobId", jobId);
			String input = (String)query.getSingleResult();
			return input;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
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

	public void setPdbScoreItemForJob(String jobId, PDBScoreItemDB pdbScoreItem)
			throws CrkWebException 
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE jobId=:jobId", JobDB.class);
			query.setParameter("jobId", jobId);
			JobDB job = (JobDB)query.getSingleResult();
			
			if(job != null)
			{
				pdbScoreItem.setJobItem(job);
				job.setPdbScoreItem(pdbScoreItem);
				job.setStatus(StatusOfJob.FINISHED);
				entityManager.merge(job);
			}
			
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
}
