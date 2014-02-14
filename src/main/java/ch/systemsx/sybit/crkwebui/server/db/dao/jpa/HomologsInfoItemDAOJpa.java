package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eppic.model.HomologsInfoItemDB_;
import eppic.model.PDBScoreItemDB_;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.HomologsInfoItemDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import eppic.model.HomologsInfoItemDB;
import eppic.model.PDBScoreItemDB;

/**
 * Implementation of HomologsInfoItemDAO.
 * @author AS
 *
 */
public class HomologsInfoItemDAOJpa implements HomologsInfoItemDAO
{
	@Override
	public List<HomologsInfoItem> getHomologsInfoItems(int pdbScoreUid) throws DaoException
	{
		EntityManager entityManager = null;
		
		try
		{
			List<HomologsInfoItem> result = new ArrayList<HomologsInfoItem>();
			
			entityManager = EntityManagerHandler.getEntityManager();			
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			
			CriteriaQuery<HomologsInfoItemDB> criteriaQuery = criteriaBuilder.createQuery(HomologsInfoItemDB.class);
			Root<HomologsInfoItemDB> numHomologsStringItemRoot = criteriaQuery.from(HomologsInfoItemDB.class);
			Path<PDBScoreItemDB> pdbScoreItemRoot = numHomologsStringItemRoot.get(HomologsInfoItemDB_.pdbScoreItem);
			Predicate condition = criteriaBuilder.equal(pdbScoreItemRoot.get(PDBScoreItemDB_.uid), pdbScoreUid);
			criteriaQuery.where(condition);
			
			Query query = entityManager.createQuery(criteriaQuery);
			@SuppressWarnings("unchecked")
			List<HomologsInfoItemDB> numHomologsStringItemDBs = query.getResultList();
			
			for(HomologsInfoItemDB homologsInfoItemDB : numHomologsStringItemDBs)
			{
				result.add(HomologsInfoItem.create(homologsInfoItemDB));
			}
			
			return result;
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
	}

	@Override
	public List<PDBSearchResult> getPdbSearchItemsForUniProt(String uniProtId)
			throws DaoException {
		EntityManager entityManager = null;
	
		try
		{
			List<PDBSearchResult> resultList = new ArrayList<PDBSearchResult>();
			
			entityManager = EntityManagerHandler.getEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PDBScoreItemDB> criteriaQuery = criteriaBuilder.createQuery(PDBScoreItemDB.class);
			
			Root<HomologsInfoItemDB> root = criteriaQuery.from(HomologsInfoItemDB.class);
			criteriaQuery.select(root.get(HomologsInfoItemDB_.pdbScoreItem));
			criteriaQuery.where(criteriaBuilder.equal(root.get(HomologsInfoItemDB_.uniprotId), uniProtId));
			Query query = entityManager.createQuery(criteriaQuery);
			
			@SuppressWarnings("unchecked")
			List<PDBScoreItemDB> pdbItemDBs = query.getResultList();
			
			for(PDBScoreItemDB pdbItemDB: pdbItemDBs){
				PDBSearchResult result = new PDBSearchResult(pdbItemDB.getPdbName(), 
															pdbItemDB.getTitle(), 
															pdbItemDB.getReleaseDate(), 
															pdbItemDB.getSpaceGroup(), 
															pdbItemDB.getResolution(), 
															pdbItemDB.getRfreeValue(), 
															pdbItemDB.getExpMethod());
				
				resultList.add(result);
			}
			
			return resultList;
			
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
		
	}
}