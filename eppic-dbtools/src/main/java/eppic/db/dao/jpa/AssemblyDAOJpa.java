package eppic.db.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.dtomodel.Assembly;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.AssemblyDAO;
import eppic.db.dao.DaoException;
import eppic.model.AssemblyDB;
import eppic.model.AssemblyDB_;
import eppic.model.PdbInfoDB;
import eppic.model.PdbInfoDB_;

public class AssemblyDAOJpa implements AssemblyDAO {

	private static final Logger logger = LoggerFactory.getLogger(AssemblyDAOJpa.class);
	
	@Override
	public List<Assembly> getAssemblies(int pdbInfoUid, boolean withScores) throws DaoException {
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

}
