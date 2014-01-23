package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.IPForbiddenDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.IPForbidden;
import ch.systemsx.sybit.crkwebui.server.db.model.IPForbidden_;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;

/**
 * Implementation of IPForbiddenDAO.
 * @author AS
 *
 */
public class IPForbiddenDAOJpa implements IPForbiddenDAO
{
	@Override
	public boolean isIPForbidden(String ip) throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
			Root<IPForbidden> sessionRoot = criteriaQuery.from(IPForbidden.class);
			criteriaQuery.select(sessionRoot.get(IPForbidden_.ip));
			Predicate condition = criteriaBuilder.equal(sessionRoot.get(IPForbidden_.ip), ip);
			criteriaQuery.where(condition);
			Query query = entityManager.createQuery(criteriaQuery);
//			Query query = entityManager.createQuery("from IPForbidden WHERE ip = :ip", IPForbidden.class);
//			query.setParameter("ip", ip);
			
			boolean isIPForbidden = false;
			
			List<IPForbidden> ipForbidden = query.getResultList();
			
			if((ipForbidden != null) && (ipForbidden.size() > 0))
			{
				isIPForbidden = true;
			}
			
			return isIPForbidden;
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
}
