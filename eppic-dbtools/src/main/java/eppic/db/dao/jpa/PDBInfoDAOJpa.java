package eppic.db.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eppic.model.db.*;
import eppic.model.dto.PdbInfo;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;

import java.util.List;

/**
 * Implementation of PDBInfoDAO.
 * @author AS
 *
 */
public class PDBInfoDAOJpa implements PDBInfoDAO 
{
	
	public PDBInfoDAOJpa() 
	{
		
	}

	@Override
	public PdbInfo getPDBInfo(String jobId) throws DaoException {
		EntityManager entityManager = null;
		PdbInfo result = null;

		try
		{
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

			result = PdbInfo.create(pdbScoreItemDB);
			result.setJobId(jobId);

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

		return result;
	}

	@Override
	public PdbInfo getPDBInfo(String jobId, boolean withChainClustersAndResidues) throws DaoException {

		EntityManager entityManager = null;
		PdbInfo result = null;

		try {
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);

			Root<PdbInfoDB> pdbScoreItemRoot = criteriaQuery.from(PdbInfoDB.class);
			Path<JobDB> jobItemPath = pdbScoreItemRoot.get(PdbInfoDB_.job);
			Predicate condition = criteriaBuilder.equal(jobItemPath.get(JobDB_.jobId), jobId);
			criteriaQuery.where(condition);

			TypedQuery<PdbInfoDB> query = entityManager.createQuery(criteriaQuery);

			List<PdbInfoDB> pdbInfoDBs = query.getResultList();

			if (pdbInfoDBs.size()>1) {
				throw new DaoException("More than 1 PdbInfoDB object found for jobId " + jobId);
			}

			PdbInfoDB pdbInfoDB = pdbInfoDBs.get(0);

			if (!withChainClustersAndResidues) {
				pdbInfoDB.setChainClusters(null);
			}

			result = PdbInfo.create(pdbInfoDB);


		} catch (Throwable e) {
			throw new DaoException(e);
		} finally {
			if (entityManager!=null)
				entityManager.close();
		}

		return result;
	}
	
	@Override
	public void insertPDBInfo(PdbInfoDB pdbScoreItem) throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			entityManager.getTransaction().begin();
			entityManager.persist(pdbScoreItem);
			entityManager.getTransaction().commit();
		}
		catch(Throwable e)
		{

			if (entityManager!=null)
				entityManager.getTransaction().rollback();

			
			throw new DaoException(e);
		}
		finally
		{
			if (entityManager!=null)
				entityManager.close();
		}
	}
}
