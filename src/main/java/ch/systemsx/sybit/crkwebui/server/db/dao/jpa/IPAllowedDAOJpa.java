package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.IPAllowedDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.IPAllowed;
import ch.systemsx.sybit.crkwebui.server.db.model.IPAllowed_;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;

/**
 * Implementation of IPAllowedDAO.
 * @author AS
 *
 */
public class IPAllowedDAOJpa implements IPAllowedDAO 
{
	@Override
	public int getNrOfAllowedSubmissionsForIP(String ip) throws DaoException
	{
		EntityManager entityManager = null;

		int nrOfAllowedSubmissionsPerIP = 0;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
			Root<IPAllowed> sessionRoot = criteriaQuery.from(IPAllowed.class);
			criteriaQuery.select(sessionRoot.get(IPAllowed_.nrOfAllowedSubmission));
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(IPAllowed_.ip), ip);
			criteriaQuery.where(condition);
			Query query = entityManager.createQuery(criteriaQuery);
			
//			Query query = entityManager.createQuery("SELECT nrOfAllowedSubmission from IPAllowed WHERE ip = :ip", Integer.class);
//			query.setParameter("ip", ip);
			
			List<Integer> nrOfAllowedSubmissionsPerIPResult = query.getResultList();
			
			if((nrOfAllowedSubmissionsPerIPResult != null) && (nrOfAllowedSubmissionsPerIPResult.size() > 0))
			{
				nrOfAllowedSubmissionsPerIP = nrOfAllowedSubmissionsPerIPResult.get(0);
			}
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
		
		return nrOfAllowedSubmissionsPerIP;
	}
}
