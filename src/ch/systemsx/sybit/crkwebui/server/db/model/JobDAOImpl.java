package ch.systemsx.sybit.crkwebui.server.db.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

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
			Job job = new Job();
			job.setJobId(jobId);
			job.setEmail(email);
			job.setSessionId(sessionId);
			job.setInput(input);
			job.setIp(ip);
			job.setStatus(StatusOfJob.RUNNING);
			job.setSubmissionDate(submissionDate);
			
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
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
	
	public void updateSessionIdForSelectedJob(String sessionId, String jobId) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE jobId = :jobId");
			query.setParameter("jobId", jobId);
			Job job = (Job) query.getSingleResult();
			
			if(job != null)
			{
				job.setSessionId(sessionId);
			}
			
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
	
	public void updateStatusOfJob(String jobId, String status) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE jobId = :jobId");
			query.setParameter("jobId", jobId);
			Job job = (Job) query.getSingleResult();
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
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE sessionId = :sessionId", Job.class);
			query.setParameter("sessionId", sessionId);
			List<Job> jobs = query.getResultList();
			
			if(jobs != null)
			{
				for(Job job : jobs)
				{
					job.setSessionId(null);
					entityManager.merge(job);
					entityManager.flush();
					entityManager.clear();
				}
			}
			
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
	
	public List<ProcessingInProgressData> getJobsForSession(String sessionId) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			Query query = entityManager.createQuery("from Job WHERE sessionId = :sessionId", Job.class);
			query.setParameter("sessionId", sessionId);
			List<Job> jobs = query.getResultList();
			 
			List<ProcessingInProgressData> processingInProgressDataList = null;
			if(jobs != null)
			{
				processingInProgressDataList = new ArrayList<ProcessingInProgressData>();
				 
				for(Job job : jobs)
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
			Query query = entityManager.createQuery("SELECT count(jobId) FROM Job WHERE sessionId = :sessionId", Long.class);
			query.setParameter("sessionId", sessionId);
			Long nrOfJobs = (Long) query.getSingleResult();
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
		entityManager.getTransaction().begin();
		Query query = entityManager.createQuery("SELECT status FROM Job WHERE jobId = :jobId", String.class);
		query.setParameter("jobId", jobId);
		String status = null;
		Object result = query.getSingleResult();
		
		if(result != null)
		{
			status = (String)result;
		}
		
		entityManager.flush();
		entityManager.getTransaction().commit();
		entityManager.close();
		 
		return status;
	}
	 
	public ProcessingInProgressData createProcessingInProgressData(Job job)
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
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			Date currentDate = new Date();
			long oneDay = 1 * 24 * 60 * 60 * 1000;
			Timestamp dayBeforeTimestamp = new Timestamp(currentDate.getTime() - oneDay);
			
			Query query = entityManager.createQuery("SELECT count(jobId) FROM Job WHERE ip = :ip AND submissionDate > :dayBefore", Long.class);
			query.setParameter("ip", ip);
			query.setParameter("dayBefore", dayBeforeTimestamp);
			Long nrOfJobs = (Long) query.getSingleResult();
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
	public void untieSelectedJobFromSession(String jobToUntie) throws CrkWebException 
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE jobId=:jobId", Job.class);
			query.setParameter("jobId", jobToUntie);
			List<Job> jobs = query.getResultList();
			
			if(jobs != null)
			{
				for(Job job : jobs)
				{
					job.setSessionId(null);
					entityManager.merge(job);
					entityManager.flush();
					entityManager.clear();
				}
			}
			
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
}
