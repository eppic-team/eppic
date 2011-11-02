package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import model.NumHomologsStringItemDB;
import model.NumHomologsStringItemDB_;
import model.PDBScoreItemDB;
import model.PDBScoreItemDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.NumHomologsStringItem;

public class NumHomologsStringsDAOImpl implements NumHomologsStringsDAO
{
	public List<NumHomologsStringItem> getNumHomologsStrings(int pdbScoreUid) throws CrkWebException
	{
		EntityManager entityManager = null;
		List<NumHomologsStringItem> result = new ArrayList<NumHomologsStringItem>();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<NumHomologsStringItemDB> criteriaQuery = criteriaBuilder.createQuery(NumHomologsStringItemDB.class);
			
			Root<NumHomologsStringItemDB> numHomologsStringItemRoot = criteriaQuery.from(NumHomologsStringItemDB.class);
			Path<PDBScoreItemDB> pdbScoreItemRoot = numHomologsStringItemRoot.get(NumHomologsStringItemDB_.pdbScoreItem);
			Predicate condition = criteriaBuilder.equal(pdbScoreItemRoot.get(PDBScoreItemDB_.uid), pdbScoreUid);
			criteriaQuery.where(condition);
			criteriaQuery.multiselect(numHomologsStringItemRoot.get(NumHomologsStringItemDB_.uid),
									  numHomologsStringItemRoot.get(NumHomologsStringItemDB_.text));
			
			Query query = entityManager.createQuery(criteriaQuery);
			List<NumHomologsStringItemDB> numHomologsStringItemDBs = query.getResultList();
			
			for(NumHomologsStringItemDB numHomologsStringItemDB : numHomologsStringItemDBs)
			{
				result.add(NumHomologsStringItem.create(numHomologsStringItemDB));
			}
			
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
}