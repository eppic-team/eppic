package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
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
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.UserSessionDAO;
import ch.systemsx.sybit.crkwebui.server.db.data.InputWithType;
import ch.systemsx.sybit.crkwebui.server.db.data.JobStatusDetails;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

/**
 * Implementation of JobDAO.
 * @author AS
 *
 */
public class JobDAOJpa implements JobDAO
{
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

			UserSessionDAO sessionDAO = new UserSessionDAOJpa();
			UserSessionDB session = sessionDAO.getSession(entityManager, sessionId);

			JobDB job = new JobDB();
			job.setJobId(jobId);
			job.setEmail(email);
			job.setInput(input);
			job.setIp(ip);
			job.setStatus(status.getName());
			job.setSubmissionDate(submissionDate);
			job.setInputType(inputType);
			job.setSubmissionId(submissionId);

			job.getUserSessions().add(session);

			entityManager.persist(job);
//			entityManager.flush();
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
	public void updateStatusOfJob(String jobId, String status) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();

			JobDB job = getJob(entityManager, jobId);
			job.setStatus(status);
//			entityManager.merge(job);
//			entityManager.flush();
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
	public void untieJobsFromSession(String sessionId) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			entityManager.getTransaction().begin();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<UserSessionDB> criteriaQuery = criteriaBuilder.createQuery(UserSessionDB.class);
			Root<UserSessionDB> sessionRoot = criteriaQuery.from(UserSessionDB.class);
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(UserSessionDB_.sessionId), sessionId);
			criteriaQuery.where(condition);
			criteriaQuery.select(sessionRoot);

			Query query = entityManager.createQuery(criteriaQuery);
			List<UserSessionDB> sessions = query.getResultList();

			if(sessions != null)
			{
				for(UserSessionDB session : sessions)
				{
					entityManager.remove(session);
				}
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
	public List<ProcessingInProgressData> getJobsForSession(String sessionId) throws DaoException
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
		catch(Throwable t)
		{
			t.printStackTrace();
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
				t.printStackTrace();
			}
		}
	}

	@Override
	public Long getNrOfJobsForSessionId(String sessionId) throws DaoException
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
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(UserSessionDB_.sessionId), sessionId);
			criteriaQuery.where(condition);

			Query query = entityManager.createQuery(criteriaQuery);
			Long nrOfJobs = (Long)query.getSingleResult();
			return nrOfJobs;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
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
				t.printStackTrace();
			}
		}
	}

	@Override
	public String getStatusForJob(String jobId) throws DaoException
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
			
			List<String> result = query.getResultList();

			if((result != null) && (result.size() > 0))
			{
				status = result.get(0);
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
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
				t.printStackTrace();
			}
		}

		return status;
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
			e.printStackTrace();
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
	public ProcessingInProgressData createProcessingInProgressData(JobDB job)
	{
		ProcessingInProgressData processingInProgressData = null;

		if(job != null)
		{
			processingInProgressData = new ProcessingInProgressData();
			processingInProgressData.setJobId(job.getJobId());
			processingInProgressData.setInput(job.getInput());
			processingInProgressData.setStatus(job.getStatus());
			processingInProgressData.setInputType(job.getInputType());
		}

		return processingInProgressData;
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
			t.printStackTrace();
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
				t.printStackTrace();
			}
		}

		return nrOfJobs;
	}

	@Override
	public Date getOldestJobSubmissionDateDuringLastDay(String ip) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			Date currentDate = new Date();
			long oneDay = 1 * 24 * 60 * 60 * 1000;
			Timestamp dayBeforeTimestamp = new Timestamp(currentDate.getTime() - oneDay);

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Date> criteriaQuery = criteriaBuilder.createQuery(Date.class);
			
			Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.select(criteriaBuilder.least(jobRoot.get(JobDB_.submissionDate)));
			
			Predicate ipCondition = criteriaBuilder.equal(jobRoot.get(JobDB_.ip), ip);
			Predicate submissionDateCondition = criteriaBuilder.greaterThan(jobRoot.get(JobDB_.submissionDate), dayBeforeTimestamp);
			Predicate condition = criteriaBuilder.and(ipCondition, submissionDateCondition);
			criteriaQuery.where(condition);
			Query query = entityManager.createQuery(criteriaQuery);
			
//			Query query = entityManager.createQuery("SELECT MIN(submissionDate) FROM Job WHERE ip = :ip AND submissionDate > :dayBefore", Date.class);
//			query.setParameter("ip", ip);
//			query.setParameter("dayBefore", dayBeforeTimestamp);

			Date oldestJobSubmissionDateDuringLastDay  = new Date(dayBeforeTimestamp.getTime());

			List<Date> oldestJobSubmissionDateDuringLastDayResult = query.getResultList();

			if((oldestJobSubmissionDateDuringLastDayResult != null) &&
			   (oldestJobSubmissionDateDuringLastDayResult.size() > 0))
			{
				oldestJobSubmissionDateDuringLastDay = oldestJobSubmissionDateDuringLastDayResult.get(0);
			}

			return oldestJobSubmissionDateDuringLastDay;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
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
				t.printStackTrace();
			}
		}
	}

	@Override
	public void untieSelectedJobFromSession(String sessionId, String jobToUntie) throws DaoException
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
			Path<String> jobPath = join.get(JobDB_.jobId);
			Predicate sessionCondition = criteriaBuilder.equal(sessionRoot.get(UserSessionDB_.sessionId), sessionId);
			Predicate jobCondition = criteriaBuilder.equal(jobPath, jobToUntie);
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

//				entityManager.merge(session);
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
	public InputWithType getInputWithTypeForJob(String jobId) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<JobDB> criteriaQuery = criteriaBuilder.createQuery(JobDB.class);

			Root<JobDB> sessionRoot = criteriaQuery.from(JobDB.class);
			criteriaQuery.multiselect(sessionRoot.get(JobDB_.input),
									  sessionRoot.get(JobDB_.inputType));
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(JobDB_.jobId), jobId);
			criteriaQuery.where(condition);
			
			Query query = entityManager.createQuery(criteriaQuery);
			JobDB job = (JobDB)query.getSingleResult();
			
			InputWithType inputWithType = new InputWithType(job.getInput(), 
															job.getInputType());
			return inputWithType;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
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
	public void setPdbScoreItemForJob(String jobId, PDBScoreItemDB pdbScoreItem)
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
				pdbScoreItem.setJobItem(job);
				job.setPdbScoreItem(pdbScoreItem);
				job.setStatus(StatusOfJob.FINISHED.getName());
//				entityManager.merge(job);
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
			e.printStackTrace();
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
			e.printStackTrace();
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
	
	private JobStatusDetails createJobStatusDetails(JobDB job)
	{
		JobStatusDetails jobStatusDetails = null;

		if(job != null)
		{
			jobStatusDetails = new JobStatusDetails(job.getJobId(),
													job.getStatus(),
													job.getInput(),
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
			
			Query query = entityManager.createQuery(criteriaQuery);
			job = (JobDB) query.getSingleResult();
			
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			throw new DaoException(e);
		}
		
		return job;
	}
}
