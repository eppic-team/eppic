package ch.systemsx.sybit.crkwebui.server.db.model;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import model.JobDB;
import model.JobDB_;
import model.PDBScoreItemDB;
import model.PDBScoreItemDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

public class PDBScoreDAOImpl implements PDBScoreDAO 
{
	
	public PDBScoreDAOImpl() 
	{
		
	}

	public PDBScoreItem getPDBScore(String jobId) throws CrkWebException
	{
		EntityManager entityManager = null;
		PDBScoreItem result = null;
	
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PDBScoreItemDB> criteriaQuery = criteriaBuilder.createQuery(PDBScoreItemDB.class);
			
			Root<PDBScoreItemDB> pdbScoreItemRoot = criteriaQuery.from(PDBScoreItemDB.class);
			Path<JobDB> jobItemPath = pdbScoreItemRoot.get(PDBScoreItemDB_.jobItem);
			Predicate condition = criteriaBuilder.equal(jobItemPath.get(JobDB_.jobId), jobId);
			criteriaQuery.where(condition);
			criteriaQuery.multiselect(pdbScoreItemRoot.get(PDBScoreItemDB_.uid),
									  pdbScoreItemRoot.get(PDBScoreItemDB_.jobItem),
									  pdbScoreItemRoot.get(PDBScoreItemDB_.pdbName),
									  pdbScoreItemRoot.get(PDBScoreItemDB_.title),
									  pdbScoreItemRoot.get(PDBScoreItemDB_.spaceGroup),
									  pdbScoreItemRoot.get(PDBScoreItemDB_.runParameters));
			
			Query query = entityManager.createQuery(criteriaQuery);
			PDBScoreItemDB pdbScoreItemDB = (PDBScoreItemDB)query.getSingleResult();
			
			result = PDBScoreItem.create(pdbScoreItemDB);
			result.setJobId(jobId);
			
	//			Query query = entityManager.createQuery("from PDBScore WHERE jobId = :jobId", PDBScoreItem.class);
	//			query.setParameter("jobId", jobId);
	//			PDBScoreItem pdbScoreItem = (PDBScoreItem) query.getSingleResult();
	//			return pdbScoreItem;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			throw new CrkWebException(e);
		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				
			}
		}
		
		return result;
	}
	
	public void insertPDBScore(PDBScoreItemDB pdbScoreItem) throws CrkWebException
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
			e.printStackTrace();
			
			try
			{
				entityManager.getTransaction().rollback();
			}
			catch(Throwable t)
			{
				
			}
			
			throw new CrkWebException(e);
		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				
			}
		}
	}
}
