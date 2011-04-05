package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

public class JobDAO 
{
	public void insertNewJob(String jobId, 
							 String sessionId,
							 String email, 
							 String input,
							 String ip) throws CrkWebException
	{
		try
		{
			Job job = new Job();
			job.setJobId(jobId);
			job.setEmail(email);
			job.setSessionId(sessionId);
			job.setInput(input);
			job.setIp(ip);
			job.setStatus(StatusOfJob.RUNNING);
			
			EntityManager entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			entityManager.persist(job);
			entityManager.flush();
			entityManager.getTransaction().commit();
			entityManager.close();
		}
		catch(Exception e)
		{
			throw new CrkWebException(e);
		}
	}
	
	public void updateSessionIdForSelectedJob(String sessionId, String jobId) throws CrkWebException
	{
		try
		{
			EntityManager entityManager = EntityManagerHandler.getEntityManager();
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
			entityManager.close();
		}
		catch(Exception e)
		{
			throw new CrkWebException(e);
		}
	}
	
	public void updateStatusOfJob(String jobId, String status) throws CrkWebException
	{
		try
		{
			EntityManager entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE jobId = :jobId");
			query.setParameter("jobId", jobId);
			Job job = (Job) query.getSingleResult();
			job.setStatus(status);
			entityManager.merge(job);
			entityManager.flush();
			entityManager.getTransaction().commit();
			entityManager.close();
		}
		catch(Exception e)
		{
			throw new CrkWebException(e);
		}
	}
	
	public void untieJobsFromSession(String sessionId) throws CrkWebException
	{
		try
		{
			EntityManager entityManager = EntityManagerHandler.getEntityManager();
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
			entityManager.close();
		}
		catch(Exception e)
		{
			throw new CrkWebException(e);
		}
	}
	
	public List<ProcessingInProgressData> getJobsForSession(String sessionId) throws CrkWebException
	{
		try
		{
			EntityManager entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("from Job WHERE sessionId = :sessionId", Job.class);
			query.setParameter("sessionId", sessionId);
			List<Job> jobs = query.getResultList();
			entityManager.flush();
			entityManager.getTransaction().commit();
			entityManager.close();
			 
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
		catch(Exception e)
		{
			throw new CrkWebException(e);
		}
	}
	
	public Long getNrOfJobsForSessionId(String sessionId) throws CrkWebException
	{
		try
		{
			EntityManager entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			Query query = entityManager.createQuery("SELECT count(jobId) FROM Job WHERE sessionId = :sessionId", Long.class);
			query.setParameter("sessionId", sessionId);
			Long nrOfJobs = (Long) query.getSingleResult();
			entityManager.flush();
			entityManager.getTransaction().commit();
			entityManager.close();
			 
			return nrOfJobs;
		}
		catch(Exception e)
		{
			throw new CrkWebException(e);
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
}
