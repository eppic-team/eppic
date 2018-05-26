package eppic.rest.dao.jpa;

import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import eppic.model.*;
import eppic.rest.dao.DaoException;
import eppic.rest.dao.InterfaceDAO;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

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
				interfaceItemDB.setResidueBurials(null);
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
	public List<Interface> getInterfacesWithResidues(int interfaceClusterUid, List<Integer> interfaceIds)throws DaoException
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

			TypedQuery<InterfaceDB> query = entityManager.createQuery(criteriaQuery);

			List<InterfaceDB> interfaceItemDBs = query.getResultList();

			for(InterfaceDB interfaceItemDB : interfaceItemDBs)
			{
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

			TypedQuery<InterfaceDB> query = entityManager.createQuery(criteriaQuery);

			List<InterfaceDB> interfaceItemDBs = query.getResultList();

			for(InterfaceDB interfaceItemDB : interfaceItemDBs)
			{
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

	@Override
	public Interface getInterfaceWithResidues(int pdbInfoUid, int interfaceId)throws DaoException {
		EntityManager entityManager = null;

		try
		{
			Interface result = null;

			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceClusterDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceClusterDB.class);

			Root<InterfaceClusterDB> interfaceClusterRoot = criteriaQuery.from(InterfaceClusterDB.class);
			Path<PdbInfoDB> pdbInfoDB = interfaceClusterRoot.get(InterfaceClusterDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));

			TypedQuery<InterfaceClusterDB> query = entityManager.createQuery(criteriaQuery);

			List<InterfaceClusterDB> interfaceClusterDBs = query.getResultList();

			for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs) {

				for (InterfaceDB interfaceDB:interfaceClusterDB.getInterfaces()) {
					if (interfaceDB.getInterfaceId()==interfaceId) {
						result = Interface.create(interfaceDB);
					}
				}
			}

			if (result==null) 
				throw new DaoException("Could not find an interface for pdbInfo uid "+pdbInfoUid+" and interface id "+interfaceId);

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
	public List<Interface> getAllInterfaces(int pdbInfoUid) throws DaoException {
		EntityManager entityManager = null;

		try
		{

			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceDB.class);

			Root<InterfaceDB> interfaceRoot = criteriaQuery.from(InterfaceDB.class);
			Join<InterfaceDB,InterfaceClusterDB> clusterJoin = interfaceRoot.join(InterfaceDB_.interfaceCluster);
			Join<InterfaceClusterDB,PdbInfoDB> pdbInfoJoin = clusterJoin.join(InterfaceClusterDB_.pdbInfo);

			criteriaQuery.where(criteriaBuilder.equal(pdbInfoJoin.get(PdbInfoDB_.uid), pdbInfoUid));

			TypedQuery<InterfaceDB> query = entityManager.createQuery(criteriaQuery);

			List<InterfaceDB> interfaceDBs = query.getResultList();

			List<Interface> result = new ArrayList<Interface>(interfaceDBs.size());

			for(InterfaceDB interfaceDB : interfaceDBs) {
				Interface iface = Interface.create(interfaceDB);
				result.add(iface);
			}

			// no interfaces is a valid situation (e.g.NMR monomers). See #164
			//if (result.isEmpty()) 
			//	throw new DaoException("Could not find any interfaces for pdbInfo uid "+pdbInfoUid);

			return result;
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
