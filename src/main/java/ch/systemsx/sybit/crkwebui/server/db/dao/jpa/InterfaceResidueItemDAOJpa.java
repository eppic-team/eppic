package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import model.InterfaceItemDB;
import model.InterfaceItemDB_;
import model.InterfaceResidueItemDB;
import model.InterfaceResidueItemDB_;
import model.JobDB;
import model.JobDB_;
import model.PDBScoreItemDB;
import model.PDBScoreItemDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceResidueItemDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;

/**
 * Implementation of InterfaceResidueItemDAO.
 * @author AS
 *
 */
public class InterfaceResidueItemDAOJpa implements InterfaceResidueItemDAO 
{
	@Override
	public List<InterfaceResidueItem> getResiduesForInterface(int interfaceUid) throws DaoException
	{
		EntityManager entityManager = null;
		
		List<InterfaceResidueItem> result = new ArrayList<InterfaceResidueItem>();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceResidueItemDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceResidueItemDB.class);
			
			Root<InterfaceResidueItemDB> interfaceResidueItemRoot = criteriaQuery.from(InterfaceResidueItemDB.class);
			Path<InterfaceItemDB> interfaceItem = interfaceResidueItemRoot.get(InterfaceResidueItemDB_.interfaceItem);
			criteriaQuery.where(criteriaBuilder.equal(interfaceItem.get(InterfaceItemDB_.uid), interfaceUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			List<InterfaceResidueItemDB> interfaceResidueItemDBs = query.getResultList();
			
			for(InterfaceResidueItemDB interfaceResidueItemDB : interfaceResidueItemDBs)
			{
				result.add(InterfaceResidueItem.create(interfaceResidueItemDB));
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
		
		return result;
	}
	
	@Override
	public InterfaceResiduesItemsList getResiduesForAllInterfaces(String jobId) throws DaoException
	{
		EntityManager entityManager = null;
		
		InterfaceResiduesItemsList residuesForInterfaces = new InterfaceResiduesItemsList();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceResidueItemDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceResidueItemDB.class);
			
			Root<InterfaceResidueItemDB> interfaceResidueItemRoot = criteriaQuery.from(InterfaceResidueItemDB.class);
			Path<InterfaceItemDB> interfaceItem = interfaceResidueItemRoot.get(InterfaceResidueItemDB_.interfaceItem);
			Path<PDBScoreItemDB> pdbScoreItem = interfaceItem.get(InterfaceItemDB_.pdbScoreItem);
			Path<JobDB> jobItem = pdbScoreItem.get(PDBScoreItemDB_.jobItem);
			criteriaQuery.where(criteriaBuilder.equal(jobItem.get(JobDB_.jobId), jobId));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			List<InterfaceResidueItemDB> interfaceResidueItemDBs = query.getResultList();
			
			for(InterfaceResidueItemDB interfaceResidueItemDB : interfaceResidueItemDBs)
			{
				if(residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getId()) == null)
				{
					HashMap<Integer, List<InterfaceResidueItem>> structures = new HashMap<Integer, List<InterfaceResidueItem>>();
					List<InterfaceResidueItem> firstStructureResidues = new ArrayList<InterfaceResidueItem>();
					List<InterfaceResidueItem> secondStructureResidues = new ArrayList<InterfaceResidueItem>();
					structures.put(1, firstStructureResidues);
					structures.put(2, secondStructureResidues);
					
					residuesForInterfaces.put(new Integer(interfaceResidueItemDB.getInterfaceItem().getId()), 
											  structures);
					
					
				}
				
				InterfaceResidueItem interfaceResidueItem = InterfaceResidueItem.create(interfaceResidueItemDB);
				
				if(interfaceResidueItem.getStructure() == 1)
				{
					residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getId()).get(1).add(interfaceResidueItem);
				}
				else if(interfaceResidueItem.getStructure() == 2)
				{
					residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getId()).get(2).add(interfaceResidueItem);
				}
				
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
		
		return residuesForInterfaces;
	}
}
