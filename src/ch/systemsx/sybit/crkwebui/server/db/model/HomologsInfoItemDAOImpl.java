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

import model.HomologsInfoItemDB;
import model.HomologsInfoItemDB_;
import model.PDBScoreItemDB;
import model.PDBScoreItemDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;

public class HomologsInfoItemDAOImpl implements HomologsInfoItemDAO
{
	public List<HomologsInfoItem> getHomologsInfoItems(int pdbScoreUid) throws CrkWebException
	{
		EntityManager entityManager = null;
		List<HomologsInfoItem> result = new ArrayList<HomologsInfoItem>();
		
		try
		{
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<HomologsInfoItemDB> criteriaQuery = criteriaBuilder.createQuery(HomologsInfoItemDB.class);
			
			Root<HomologsInfoItemDB> numHomologsStringItemRoot = criteriaQuery.from(HomologsInfoItemDB.class);
			Path<PDBScoreItemDB> pdbScoreItemRoot = numHomologsStringItemRoot.get(HomologsInfoItemDB_.pdbScoreItem);
			Predicate condition = criteriaBuilder.equal(pdbScoreItemRoot.get(PDBScoreItemDB_.uid), pdbScoreUid);
			criteriaQuery.where(condition);
			criteriaQuery.multiselect(numHomologsStringItemRoot.get(HomologsInfoItemDB_.uid),
									  numHomologsStringItemRoot.get(HomologsInfoItemDB_.chains),
									  numHomologsStringItemRoot.get(HomologsInfoItemDB_.uniprotId),
									  numHomologsStringItemRoot.get(HomologsInfoItemDB_.numHomologs),
									  numHomologsStringItemRoot.get(HomologsInfoItemDB_.subInterval),
									  numHomologsStringItemRoot.get(HomologsInfoItemDB_.alignedSeq1),
									  numHomologsStringItemRoot.get(HomologsInfoItemDB_.alignedSeq2),
									  numHomologsStringItemRoot.get(HomologsInfoItemDB_.markupLine));
			
			Query query = entityManager.createQuery(criteriaQuery);
			List<HomologsInfoItemDB> numHomologsStringItemDBs = query.getResultList();
			
			for(HomologsInfoItemDB homologsInfoItemDB : numHomologsStringItemDBs)
			{
				result.add(HomologsInfoItem.create(homologsInfoItemDB));
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