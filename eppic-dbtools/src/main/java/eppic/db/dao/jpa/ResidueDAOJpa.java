package eppic.db.dao.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.ResidueDAO;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceClusterDB_;
import eppic.model.db.InterfaceDB_;
import eppic.model.db.JobDB_;
import eppic.model.db.PdbInfoDB_;
import eppic.model.db.ResidueBurialDB_;
import eppic.model.dto.Residue;
import eppic.model.dto.ResiduesList;
import eppic.model.db.InterfaceDB;
import eppic.model.db.ResidueBurialDB;
import eppic.model.db.JobDB;
import eppic.model.db.PdbInfoDB;

/**
 * Implementation of ResidueDAO.
 * @author AS
 *
 */
public class ResidueDAOJpa implements ResidueDAO 
{
	
	private static final Logger logger = LoggerFactory.getLogger(ResidueDAOJpa.class);
	
	
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
			Path<InterfaceDB> interfaceItem = residueRoot.get(ResidueBurialDB_.interfaceItem);
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
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
		
		logger.debug("Got {} residues for interface uid {}", result.size(), interfaceUid);
		
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
			Path<InterfaceDB> interfaceItem = interfaceResidueItemRoot.get(ResidueBurialDB_.interfaceItem);
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
				
				if(residue.getSide() == false)
				{
					residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getInterfaceId()).get(1).add(residue);
				}
				else if(residue.getSide() == true)
				{
					residuesForInterfaces.get(interfaceResidueItemDB.getInterfaceItem().getInterfaceId()).get(2).add(residue);
				}
				
			}
		}
		catch(Throwable e)
		{
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
		
		logger.debug("Got residues for {} interfaces belonging to job id {}", residuesForInterfaces.size() , jobId);
		
		return residuesForInterfaces;
	}
}
