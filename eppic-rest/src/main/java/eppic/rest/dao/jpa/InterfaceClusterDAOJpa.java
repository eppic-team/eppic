package eppic.rest.dao.jpa;

import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterDB_;
import eppic.model.PdbInfoDB;
import eppic.model.PdbInfoDB_;
import eppic.rest.dao.DaoException;
import eppic.rest.dao.InterfaceClusterDAO;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class InterfaceClusterDAOJpa implements InterfaceClusterDAO
{

	@Override
	public List<InterfaceCluster> getInterfaceClustersWithoutInterfaces(int pdbInfoUid)	throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			List<InterfaceCluster> result = new ArrayList<InterfaceCluster>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceClusterDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceClusterDB.class);
			
			Root<InterfaceClusterDB> interfaceClusterRoot = criteriaQuery.from(InterfaceClusterDB.class);
			Path<PdbInfoDB> pdbInfoDB = interfaceClusterRoot.get(InterfaceClusterDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<InterfaceClusterDB> interfaceClusterDBs = query.getResultList();
			
			for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs)
			{
				interfaceClusterDB.setInterfaces(null);
				result.add(InterfaceCluster.create(interfaceClusterDB));
			}
			
			return result;
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

	@Override
	public List<InterfaceCluster> getInterfaceClustersWithoutInterfaces(int pdbInfoUid, List<Integer> interfaceClusterIds) 
			throws DaoException {
		
		EntityManager entityManager = null;
		
		try
		{
			List<InterfaceCluster> result = new ArrayList<InterfaceCluster>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceClusterDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceClusterDB.class);
			
			Root<InterfaceClusterDB> interfaceClusterRoot = criteriaQuery.from(InterfaceClusterDB.class);
			Path<PdbInfoDB> pdbInfoDB = interfaceClusterRoot.get(InterfaceClusterDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<InterfaceClusterDB> interfaceClusterDBs = query.getResultList();
			
			for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs) {
				interfaceClusterDB.setInterfaces(null);
				if (interfaceClusterIds.contains(interfaceClusterDB.getClusterId())) { 
					result.add(InterfaceCluster.create(interfaceClusterDB));
				}
			}
			
			return result;
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
