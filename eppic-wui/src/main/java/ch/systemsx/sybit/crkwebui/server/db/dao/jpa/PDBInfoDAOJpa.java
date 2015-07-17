package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eppic.model.JobDB_;
import eppic.model.PdbInfoDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.model.JobDB;
import eppic.model.PdbInfoDB;

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
	public PdbInfo getPDBInfo(String jobId) throws DaoException
	{
		System.out.println("PDBInfoDAOJpa.java: calling getPDBInfo().....");
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
									  pdbScoreItemRoot.get(PdbInfoDB_.runParameters));
			
			Query query = entityManager.createQuery(criteriaQuery);
			PdbInfoDB pdbScoreItemDB = (PdbInfoDB)query.getSingleResult();
			
			result = PdbInfo.create(pdbScoreItemDB);
			result.setJobId(jobId);
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
		
		return result;
	}
	
	@Override
	public void insertPDBInfo(PdbInfoDB pdbScoreItem) throws DaoException
	{
		System.out.println("PDBInfoDAOJpa.java: calling insertPDBInfo().....");
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
			e.printStackTrace();
			
			try
			{
				entityManager.getTransaction().rollback();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			
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
