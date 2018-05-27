package eppic.db.dao.jpa;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eppic.server.model.DataDownloadTracking;
import eppic.server.model.DataDownloadTracking_;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.DataDownloadTrackingDAO;

public class DataDownloadTrackingDAOJpa implements DataDownloadTrackingDAO {

	@Override
	public void insertNewIP(String ip, Date downloadDate) throws DaoException {
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();

			DataDownloadTracking downloadIP = new DataDownloadTracking(ip, downloadDate);

			entityManager.persist(downloadIP);
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
	public Long getNrOfDownloadsForIPDuringLastDay(String ip) throws DaoException {
		EntityManager entityManager = null;

		long nrOfDownloads = 0;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			Date currentDate = new Date();
			long oneDay = 1 * 24 * 60 * 60 * 1000;
			Timestamp dayBeforeTimestamp = new Timestamp(currentDate.getTime() - oneDay);

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<DataDownloadTracking> sessionRoot = criteriaQuery.from(DataDownloadTracking.class);
			criteriaQuery.select(criteriaBuilder.count(sessionRoot));
			Predicate ipCondition = criteriaBuilder.equal(sessionRoot.get(DataDownloadTracking_.ip), ip);
			Predicate dateCondition =  criteriaBuilder.greaterThan(sessionRoot.get(DataDownloadTracking_.downloadDate), dayBeforeTimestamp);
			Predicate condition = criteriaBuilder.and(ipCondition, dateCondition);
			criteriaQuery.where(condition);
			Query query = entityManager.createQuery(criteriaQuery);
			
			nrOfDownloads = (Long) query.getSingleResult();
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

		return nrOfDownloads;
	}

	@Override
	public Date getOldestJobDownloadDateDuringLastDay(String ip)
			throws DaoException {
		EntityManager entityManager = null;

		try
		{
			entityManager = EntityManagerHandler.getEntityManager();

			Date currentDate = new Date();
			long oneDay = 1 * 24 * 60 * 60 * 1000;
			Timestamp dayBeforeTimestamp = new Timestamp(currentDate.getTime() - oneDay);

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Date> criteriaQuery = criteriaBuilder.createQuery(Date.class);
			
			Root<DataDownloadTracking> jobRoot = criteriaQuery.from(DataDownloadTracking.class);
			criteriaQuery.select(criteriaBuilder.least(jobRoot.get(DataDownloadTracking_.downloadDate)));
			
			Predicate ipCondition = criteriaBuilder.equal(jobRoot.get(DataDownloadTracking_.ip), ip);
			Predicate dateCondition = criteriaBuilder.greaterThan(jobRoot.get(DataDownloadTracking_.downloadDate), dayBeforeTimestamp);
			Predicate condition = criteriaBuilder.and(ipCondition, dateCondition);
			criteriaQuery.where(condition);
			Query query = entityManager.createQuery(criteriaQuery);

			Date oldestJobSubmissionDateDuringLastDay  = new Date(dayBeforeTimestamp.getTime());

			@SuppressWarnings("unchecked")
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

}
