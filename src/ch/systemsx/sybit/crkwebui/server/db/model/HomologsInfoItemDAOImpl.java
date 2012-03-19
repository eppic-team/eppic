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

/**
 * Implementation of HomologsInfoItemDAO.
 * @author AS
 *
 */
public class HomologsInfoItemDAOImpl implements HomologsInfoItemDAO
{
	@Override
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
			
			Query query = entityManager.createQuery(criteriaQuery);
			List<HomologsInfoItemDB> numHomologsStringItemDBs = query.getResultList();
			
			for(HomologsInfoItemDB homologsInfoItemDB : numHomologsStringItemDBs)
			{
				result.add(HomologsInfoItem.create(homologsInfoItemDB));
			}
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