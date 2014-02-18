package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.ArrayList;
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
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import eppic.model.InterfaceDB;

/**
 * Implementation of InterfaceDAO.
 * @author AS
 *
 */
public class InterfaceDAOJpa implements InterfaceDAO 
{
	@Override
	public List<Interface> getInterfacesWithScores(int interfaceClusterUid) throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			List<Interface> result = new ArrayList<Interface>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceDB.class);
			
			Root<InterfaceDB> interfaceItemRoot = criteriaQuery.from(InterfaceDB.class);
			Path<InterfaceClusterDB> interfaceClusterPath = interfaceItemRoot.get(InterfaceDB_.interfaceCluster);
			criteriaQuery.where(criteriaBuilder.equal(interfaceClusterPath.get(InterfaceClusterDB_.uid), interfaceClusterUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<InterfaceDB> interfaceItemDBs = query.getResultList();
			
			for(InterfaceDB interfaceItemDB : interfaceItemDBs)
			{
				interfaceItemDB.setResidues(null);
				result.add(Interface.create(interfaceItemDB));
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
	public List<Interface> getInterfacesWithScores(int interfaceClusterUid, List<Integer> interfaceIds)throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			List<Interface> result = new ArrayList<Interface>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceDB.class);
			
			Root<InterfaceDB> interfaceItemRoot = criteriaQuery.from(InterfaceDB.class);
			Path<InterfaceClusterDB> interfaceClusterPath = interfaceItemRoot.get(InterfaceDB_.interfaceCluster);
			criteriaQuery.where(criteriaBuilder.equal(interfaceClusterPath.get(InterfaceClusterDB_.uid), interfaceClusterUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<InterfaceDB> interfaceItemDBs = query.getResultList();
			
			for(InterfaceDB interfaceItemDB : interfaceItemDBs)
			{
				interfaceItemDB.setResidues(null);
				if(interfaceIds.contains(interfaceItemDB.getInterfaceId())){
					result.add(Interface.create(interfaceItemDB));
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
	
	@Override
	public List<Interface> getInterfacesWithResidues(int interfaceClusterUid) throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			List<Interface> result = new ArrayList<Interface>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceDB.class);
			
			Root<InterfaceDB> interfaceItemRoot = criteriaQuery.from(InterfaceDB.class);
			Path<InterfaceClusterDB> interfaceClusterPath = interfaceItemRoot.get(InterfaceDB_.interfaceCluster);
			criteriaQuery.where(criteriaBuilder.equal(interfaceClusterPath.get(InterfaceClusterDB_.uid), interfaceClusterUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<InterfaceDB> interfaceItemDBs = query.getResultList();
			
			for(InterfaceDB interfaceItemDB : interfaceItemDBs)
			{
				interfaceItemDB.setInterfaceScores(null);
				interfaceItemDB.setInterfaceWarnings(null);
				result.add(Interface.create(interfaceItemDB));
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
