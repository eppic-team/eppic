package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import model.InterfaceItemDB;
import model.InterfaceItemDB_;
import model.PDBScoreItemDB;
import model.PDBScoreItemDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;

/**
 * Implementation of InterfaceItemDAO.
 * @author AS
 *
 */
public class InterfaceItemDAOImpl implements InterfaceItemDAO 
{
	@Override
	public List<InterfaceItem> getInterfacesWithScores(int pdbScoreUid) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		List<InterfaceItem> result = new ArrayList<InterfaceItem>();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceItemDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceItemDB.class);
			
			Root<InterfaceItemDB> interfaceItemRoot = criteriaQuery.from(InterfaceItemDB.class);
			Path<PDBScoreItemDB> pdbScoreItemDB = interfaceItemRoot.get(InterfaceItemDB_.pdbScoreItem);
			criteriaQuery.where(criteriaBuilder.equal(pdbScoreItemDB.get(PDBScoreItemDB_.uid), pdbScoreUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			List<InterfaceItemDB> interfaceItemDBs = query.getResultList();
			
			for(InterfaceItemDB interfaceItemDB : interfaceItemDBs)
			{
				interfaceItemDB.setInterfaceResidues(null);
				result.add(InterfaceItem.create(interfaceItemDB));
			}
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
		
		return result;
	}
	
	@Override
	public List<InterfaceItem> getInterfacesWithResidues(int pdbScoreUid) throws CrkWebException
	{
		EntityManager entityManager = null;
		
		List<InterfaceItem> result = new ArrayList<InterfaceItem>();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceItemDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceItemDB.class);
			
			Root<InterfaceItemDB> interfaceItemRoot = criteriaQuery.from(InterfaceItemDB.class);
			Path<PDBScoreItemDB> pdbScoreItemDB = interfaceItemRoot.get(InterfaceItemDB_.pdbScoreItem);
			criteriaQuery.where(criteriaBuilder.equal(pdbScoreItemDB.get(PDBScoreItemDB_.uid), pdbScoreUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			List<InterfaceItemDB> interfaceItemDBs = query.getResultList();
			
			for(InterfaceItemDB interfaceItemDB : interfaceItemDBs)
			{
				interfaceItemDB.setInterfaceScores(null);
				interfaceItemDB.setWarnings(null);
				result.add(InterfaceItem.create(interfaceItemDB));
			}
			
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
		
		return result;
	}
}
