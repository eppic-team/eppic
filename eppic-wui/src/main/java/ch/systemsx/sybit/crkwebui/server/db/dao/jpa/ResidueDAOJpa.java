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

import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterDB_;
import eppic.model.InterfaceDB_;
import eppic.model.ResidueDB_;
import eppic.model.JobDB_;
import eppic.model.PdbInfoDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.ResidueDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.Residue;
import ch.systemsx.sybit.crkwebui.shared.model.ResiduesList;
import eppic.model.InterfaceDB;
import eppic.model.ResidueBurialDB;
import eppic.model.JobDB;
import eppic.model.PdbInfoDB;

/**
 * Implementation of ResidueDAO.
 * @author AS
 *
 */
public class ResidueDAOJpa implements ResidueDAO 
{
	@Override
	public List<Residue> getResiduesForInterface(int interfaceUid) throws DaoException
	{
		EntityManager entityManager = null;
		
		List<Residue> result = new ArrayList<Residue>();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<ResidueBurialDB> criteriaQuery = criteriaBuilder.createQuery(ResidueBurialDB.class);
			
			Root<ResidueBurialDB> residueRoot = criteriaQuery.from(ResidueBurialDB.class);
			Path<InterfaceDB> interfaceItem = residueRoot.get(ResidueDB_.interfaceItem);
			criteriaQuery.where(criteriaBuilder.equal(interfaceItem.get(InterfaceDB_.uid), interfaceUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<ResidueBurialDB> interfaceResidueItemDBs = query.getResultList();
			
			for(ResidueBurialDB interfaceResidueItemDB : interfaceResidueItemDBs)
			{
				result.add(Residue.create(interfaceResidueItemDB));
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
	public ResiduesList getResiduesForAllInterfaces(String jobId) throws DaoException
	{
		EntityManager entityManager = null;
		
		ResiduesList residuesForInterfaces = new ResiduesList();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<ResidueBurialDB> criteriaQuery = criteriaBuilder.createQuery(ResidueBurialDB.class);
			
			Root<ResidueBurialDB> interfaceResidueItemRoot = criteriaQuery.from(ResidueBurialDB.class);
			Path<InterfaceDB> interfaceItem = interfaceResidueItemRoot.get(ResidueDB_.interfaceItem);
			Path<InterfaceClusterDB> interfaceClusterItem = interfaceItem.get(InterfaceDB_.interfaceCluster);
			Path<PdbInfoDB> pdbScoreItem = interfaceClusterItem.get(InterfaceClusterDB_.pdbInfo);
			Path<JobDB> jobItem = pdbScoreItem.get(PdbInfoDB_.job);
			criteriaQuery.where(criteriaBuilder.equal(jobItem.get(JobDB_.jobId), jobId));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<ResidueBurialDB> interfaceResidueItemDBs = query.getResultList();
			
			for(ResidueBurialDB interfaceResidueItemDB : interfaceResidueItemDBs)
			{
				if(residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getInterfaceId()) == null)
				{
					HashMap<Integer, List<Residue>> structures = new HashMap<Integer, List<Residue>>();
					List<Residue> firstStructureResidues = new ArrayList<Residue>();
					List<Residue> secondStructureResidues = new ArrayList<Residue>();
					structures.put(1, firstStructureResidues);
					structures.put(2, secondStructureResidues);
					
					residuesForInterfaces.put(new Integer(interfaceResidueItemDB.getInterfaceItem().getInterfaceId()), 
											  structures);
					
					
				}
				
				Residue residue = Residue.create(interfaceResidueItemDB);
				
				if(residue.getSide() == 1)
				{
					residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getInterfaceId()).get(1).add(residue);
				}
				else if(residue.getSide() == 2)
				{
					residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getInterfaceId()).get(2).add(residue);
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
