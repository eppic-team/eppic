package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public class IPForbiddenDAOImpl implements IPForbiddenDAO
{
	public boolean isIPForbidden(String ip) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			Query query = entityManager.createQuery("from IPForbidden WHERE ip = :ip", IPForbidden.class);
			query.setParameter("ip", ip);
			
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
