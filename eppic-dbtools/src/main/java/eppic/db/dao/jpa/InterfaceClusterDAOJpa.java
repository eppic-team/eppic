package eppic.db.dao.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import eppic.model.dto.InterfaceCluster;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceClusterDAO;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceClusterDB_;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.PdbInfoDB_;

public class InterfaceClusterDAOJpa implements InterfaceClusterDAO
{

	@Override
	public List<InterfaceCluster> getInterfaceClusters(int pdbInfoUid, boolean withScores, boolean withInterfaces)	throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			List<InterfaceCluster> result = new ArrayList<>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceClusterDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceClusterDB.class);
			
			Root<InterfaceClusterDB> interfaceClusterRoot = criteriaQuery.from(InterfaceClusterDB.class);
			Path<PdbInfoDB> pdbInfoDB = interfaceClusterRoot.get(InterfaceClusterDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));
			
			TypedQuery<InterfaceClusterDB> query = entityManager.createQuery(criteriaQuery);
			
			List<InterfaceClusterDB> interfaceClusterDBs = query.getResultList();
			
			for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs)
			{
				if (!withScores) {
					interfaceClusterDB.setInterfaceClusterScores(null);
				}
				if (!withInterfaces) {
					interfaceClusterDB.setInterfaces(null);
				}
				result.add(InterfaceCluster.create(interfaceClusterDB));
			}
			
			return result;
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
	}

	@Override
	public List<InterfaceCluster> getInterfaceClusters(int pdbInfoUid, Set<Integer> interfaceClusterIds, boolean withScores, boolean withInterfaces)
			throws DaoException {
		
		EntityManager entityManager = null;
		
		try
		{
			List<InterfaceCluster> result = new ArrayList<>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceClusterDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceClusterDB.class);
			
			Root<InterfaceClusterDB> interfaceClusterRoot = criteriaQuery.from(InterfaceClusterDB.class);
			Path<PdbInfoDB> pdbInfoDB = interfaceClusterRoot.get(InterfaceClusterDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));

			TypedQuery<InterfaceClusterDB> query = entityManager.createQuery(criteriaQuery);
			
			List<InterfaceClusterDB> interfaceClusterDBs = query.getResultList();
			
			for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs) {
				if (!withScores) {
					interfaceClusterDB.setInterfaceClusterScores(null);
				}
				if (!withInterfaces) {
					interfaceClusterDB.setInterfaces(null);
				}
				if (interfaceClusterIds.contains(interfaceClusterDB.getClusterId())) { 
					result.add(InterfaceCluster.create(interfaceClusterDB));
				}
			}
			
			return result;
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
	}
}
