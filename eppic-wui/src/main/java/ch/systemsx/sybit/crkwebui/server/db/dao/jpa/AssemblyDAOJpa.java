package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.AssemblyDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import eppic.model.AssemblyDB;
import eppic.model.AssemblyDB_;
import eppic.model.PdbInfoDB;
import eppic.model.PdbInfoDB_;

public class AssemblyDAOJpa implements AssemblyDAO {

	private static final Logger logger = LoggerFactory.getLogger(AssemblyDAOJpa.class);
	
	@Override
	public List<Assembly> getAssemblies(int pdbInfoUid) throws DaoException {
		EntityManager entityManager = null;

		try
		{
			List<Assembly> result = new ArrayList<Assembly>();

			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<AssemblyDB> criteriaQuery = criteriaBuilder.createQuery(AssemblyDB.class);

			Root<AssemblyDB> root = criteriaQuery.from(AssemblyDB.class);
			Path<PdbInfoDB> pdbInfoDB = root.get(AssemblyDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));
			
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<AssemblyDB> assemblyDBs = query.getResultList();
			
			for(AssemblyDB assemblyDB : assemblyDBs) {
				if (assemblyDB.getAssemblyScores()!=null)
					logger.debug("Number of assembly scores for assembly {}: {}", assemblyDB.getId(), assemblyDB.getAssemblyScores().size());
				else 
					logger.debug("Assembly scores is null for assembly {}", assemblyDB.getId());
				if (assemblyDB.getAssemblyContents()!=null)
					logger.debug("Number of assembly contents for assembly {}: {}", assemblyDB.getId(), assemblyDB.getAssemblyContents().size());
				else 
					logger.debug("Assembly contents is null for assembly {}", assemblyDB.getId());
				
				
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
