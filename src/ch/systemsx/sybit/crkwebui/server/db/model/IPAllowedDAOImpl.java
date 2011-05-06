package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public class IPAllowedDAOImpl implements IPAllowedDAO 
{
	public int getNrOfAllowedSubmissionsForIP(String ip) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		try
		{
			int nrOfAllowedSubmissionsPerIP = 0;
			
			entityManager = EntityManagerHandler.getEntityManager();
			Query query = entityManager.createQuery("SELECT nrOfAllowedSubmission from IPAllowed WHERE ip = :ip", Integer.class);
			query.setParameter("ip", ip);
			
			List<Integer> nrOfAllowedSubmissionsPerIPResult = query.getResultList();
			
			if((nrOfAllowedSubmissionsPerIPResult != null) && (nrOfAllowedSubmissionsPerIPResult.size() > 0))
			{
				nrOfAllowedSubmissionsPerIP = nrOfAllowedSubmissionsPerIPResult.get(0);
			}
			
			return nrOfAllowedSubmissionsPerIP;
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
