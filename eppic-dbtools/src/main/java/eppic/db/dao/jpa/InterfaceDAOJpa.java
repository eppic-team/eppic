package eppic.db.dao.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import eppic.model.dto.Interface;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceDAO;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceClusterDB_;
import eppic.model.db.InterfaceDB;
import eppic.model.db.InterfaceDB_;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.PdbInfoDB_;

/**
 * JPA implementation of InterfaceDAO.
 * @author Adam Srebniak
 * @author Jose Duarte
 *
 */
public class InterfaceDAOJpa implements InterfaceDAO 
{
	@Override
	public List<Interface> getInterfacesForCluster(int interfaceClusterUid, boolean withScores, boolean withResidues) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			List<Interface> result = new ArrayList<>();

			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceDB.class);

			Root<InterfaceDB> interfaceItemRoot = criteriaQuery.from(InterfaceDB.class);
			Path<InterfaceClusterDB> interfaceClusterPath = interfaceItemRoot.get(InterfaceDB_.interfaceCluster);
			criteriaQuery.where(criteriaBuilder.equal(interfaceClusterPath.get(InterfaceClusterDB_.uid), interfaceClusterUid));

			TypedQuery<InterfaceDB> query = entityManager.createQuery(criteriaQuery);

			List<InterfaceDB> interfaceItemDBs = query.getResultList();

			for(InterfaceDB interfaceDB : interfaceItemDBs)
			{
				if (!withResidues) {
					interfaceDB.setResidueBurials(null);
				}
				if (!withScores) {
					interfaceDB.setInterfaceScores(null);
				}
				// TODO do we want an option to include/exclude interface warnings too? At the moment they are included
				result.add(Interface.create(interfaceDB));
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
	public List<Interface> getInterfacesForCluster(int interfaceClusterUid, Set<Integer> interfaceIds, boolean withScores, boolean withResidues) throws DaoException
	{
		EntityManager entityManager = null;

		try
		{
			List<Interface> result = new ArrayList<>();

			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceDB.class);

			Root<InterfaceDB> interfaceItemRoot = criteriaQuery.from(InterfaceDB.class);
			Path<InterfaceClusterDB> interfaceClusterPath = interfaceItemRoot.get(InterfaceDB_.interfaceCluster);
			criteriaQuery.where(criteriaBuilder.equal(interfaceClusterPath.get(InterfaceClusterDB_.uid), interfaceClusterUid));

			TypedQuery<InterfaceDB> query = entityManager.createQuery(criteriaQuery);

			List<InterfaceDB> interfaceItemDBs = query.getResultList();

			for(InterfaceDB interfaceDB : interfaceItemDBs)
			{
                if (!withResidues) {
                    interfaceDB.setResidueBurials(null);
                }
                if (!withScores) {
                    interfaceDB.setInterfaceScores(null);
                }
                // TODO do we want an option to include/exclude interface warnings too? At the moment they are included
				if(interfaceIds.contains(interfaceDB.getInterfaceId())){
					result.add(Interface.create(interfaceDB));
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

	@Override
	public Interface getInterface(int pdbInfoUid, int interfaceId, boolean withScores, boolean withResidues)throws DaoException {
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
					if (!withResidues) {
						interfaceDB.setResidueBurials(null);
					}
					if (!withScores) {
						interfaceDB.setInterfaceScores(null);
					}
					if (interfaceDB.getInterfaceId()==interfaceId) {
						result = Interface.create(interfaceDB);
					}
				}
			}

			if (result==null)
				throw new NoResultException("Could not find an interface for pdbInfo uid "+pdbInfoUid+" and interface id "+interfaceId);

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
	public List<Interface> getInterfacesByPdbUid(int pdbInfoUid, boolean withScores, boolean withResidues) throws DaoException {
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
				if (!withResidues) {
					interfaceDB.setResidueBurials(null);
				}
				if (!withScores) {
					interfaceDB.setInterfaceScores(null);
				}
				Interface iface = Interface.create(interfaceDB);
				result.add(iface);
			}

			// no interfaces is a valid situation (e.g.NMR monomers). See #164

			return result;
		} catch (Throwable e) {
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}
}
