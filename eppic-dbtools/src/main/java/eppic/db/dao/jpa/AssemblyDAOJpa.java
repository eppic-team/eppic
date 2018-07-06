package eppic.db.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import eppic.model.db.*;
import eppic.model.dto.Assembly;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.AssemblyDAO;
import eppic.db.dao.DaoException;

public class AssemblyDAOJpa implements AssemblyDAO {

	@Override
	public List<Assembly> getAssemblies(int pdbInfoUid, boolean withScores, boolean withGraph) throws DaoException {
		EntityManager entityManager = null;

		try
		{
			List<Assembly> result = new ArrayList<>();

			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<AssemblyDB> criteriaQuery = criteriaBuilder.createQuery(AssemblyDB.class);

			Root<AssemblyDB> root = criteriaQuery.from(AssemblyDB.class);
			Path<PdbInfoDB> pdbInfoDB = root.get(AssemblyDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));
			
			TypedQuery<AssemblyDB> query = entityManager.createQuery(criteriaQuery);
			
			List<AssemblyDB> assemblyDBs = query.getResultList();
			
			for(AssemblyDB assemblyDB : assemblyDBs) {
				if (!withScores) {
					assemblyDB.setAssemblyScores(null);
				}

				if (!withGraph) {
					assemblyDB.setGraphNodes(null);
					assemblyDB.setGraphEdges(null);
				}
				
				result.add(Assembly.create(assemblyDB));
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
	public Assembly getAssembly(int pdbInfoUid, int pdbAssemblyId, boolean withGraph) throws DaoException {
		EntityManager entityManager = null;

		try
		{

			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<AssemblyDB> criteriaQuery = criteriaBuilder.createQuery(AssemblyDB.class);

			Root<AssemblyDB> root = criteriaQuery.from(AssemblyDB.class);
			Path<PdbInfoDB> pdbInfoDB = root.get(AssemblyDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));

			TypedQuery<AssemblyDB> query = entityManager.createQuery(criteriaQuery);

			List<AssemblyDB> assemblyDBs = query.getResultList();

			String strPdbId = "pdb" + pdbAssemblyId;
			for(AssemblyDB assemblyDB : assemblyDBs) {
				for (AssemblyScoreDB asdb : assemblyDB.getAssemblyScores()) {
					if (asdb.getMethod().equals(strPdbId) && asdb.getCallName().equals("bio")) {

						if (!withGraph) {
							assemblyDB.setGraphNodes(null);
							assemblyDB.setGraphEdges(null);
						}

						return Assembly.create(assemblyDB);
					}
				}

			}
			// not found
			return null;
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
