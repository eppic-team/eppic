package eppic.rest.dao.jpa;

import eppic.model.JobDB;
import eppic.model.JobDB_;
import eppic.model.PdbInfoDB;
import eppic.model.PdbInfoDB_;
import eppic.rest.dao.DaoException;
import eppic.rest.dao.PDBInfoDAO;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

/**
 * JPA implementation of PDBInfoDAO.
 * @author Jose Duarte
 *
 */
public class PDBInfoDAOJpa implements PDBInfoDAO {
	
	public PDBInfoDAOJpa() 
	{
		
	}

	@Override
	public PdbInfoDB getPDBInfoShallow(String jobId) throws DaoException
	{
		EntityManager entityManager = null;

		try {
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);
			
			Root<PdbInfoDB> pdbScoreItemRoot = criteriaQuery.from(PdbInfoDB.class);
			Path<JobDB> jobItemPath = pdbScoreItemRoot.get(PdbInfoDB_.job);
			Predicate condition = criteriaBuilder.equal(jobItemPath.get(JobDB_.jobId), jobId);
			criteriaQuery.where(condition);
			criteriaQuery.multiselect(pdbScoreItemRoot.get(PdbInfoDB_.uid),
									  pdbScoreItemRoot.get(PdbInfoDB_.job),
									  pdbScoreItemRoot.get(PdbInfoDB_.pdbCode),
									  pdbScoreItemRoot.get(PdbInfoDB_.title),
									  pdbScoreItemRoot.get(PdbInfoDB_.spaceGroup),
									  pdbScoreItemRoot.get(PdbInfoDB_.expMethod),
									  pdbScoreItemRoot.get(PdbInfoDB_.resolution),
									  pdbScoreItemRoot.get(PdbInfoDB_.rfreeValue),
									  pdbScoreItemRoot.get(PdbInfoDB_.cellA),
									  pdbScoreItemRoot.get(PdbInfoDB_.cellB),
									  pdbScoreItemRoot.get(PdbInfoDB_.cellC),
									  pdbScoreItemRoot.get(PdbInfoDB_.cellAlpha),
									  pdbScoreItemRoot.get(PdbInfoDB_.cellBeta),
									  pdbScoreItemRoot.get(PdbInfoDB_.cellGamma),
									  pdbScoreItemRoot.get(PdbInfoDB_.crystalFormId),
									  pdbScoreItemRoot.get(PdbInfoDB_.runParameters),
									  pdbScoreItemRoot.get(PdbInfoDB_.nonStandardSg),
									  pdbScoreItemRoot.get(PdbInfoDB_.nonStandardCoordFrameConvention),
									  pdbScoreItemRoot.get(PdbInfoDB_.exhaustiveAssemblyEnumeration));
			
			TypedQuery<PdbInfoDB> query = entityManager.createQuery(criteriaQuery);
			PdbInfoDB pdbScoreItemDB = query.getSingleResult();

			return pdbScoreItemDB;
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
	public PdbInfoDB getPDBInfo(String jobId) throws DaoException {
		EntityManager entityManager = null;

		try {
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);

			Root<PdbInfoDB> rootPDB = criteriaQuery.from(PdbInfoDB.class);
			Path<JobDB> jobItemPath = rootPDB.get(PdbInfoDB_.job);
			Predicate condition = criteriaBuilder.equal(jobItemPath.get(JobDB_.jobId), jobId);
			criteriaQuery.where(condition);
			TypedQuery<PdbInfoDB> query = entityManager.createQuery(criteriaQuery);
			PdbInfoDB pdbInfoDB = query.getSingleResult();
			return pdbInfoDB;

		} catch(Throwable e) {
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}

	@Override
	public PdbInfoDB getPDBInfoByPdbId(String pdbId) throws DaoException {
		EntityManager entityManager = null;

		try {
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);

			Root<PdbInfoDB> rootPDB = criteriaQuery.from(PdbInfoDB.class);

			criteriaQuery.where(criteriaBuilder.equal(rootPDB.get(PdbInfoDB_.pdbCode), pdbId));
			criteriaQuery.select(rootPDB);

			TypedQuery<PdbInfoDB> query = entityManager.createQuery(criteriaQuery);
			PdbInfoDB pdbInfoDB = query.getSingleResult();
			return pdbInfoDB;

		} catch(Throwable e) {
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}
}
