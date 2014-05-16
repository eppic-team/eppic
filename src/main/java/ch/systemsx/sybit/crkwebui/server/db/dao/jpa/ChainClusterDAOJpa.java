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
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.ChainClusterDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import eppic.model.ChainClusterDB;
import eppic.model.ChainClusterDB_;
import eppic.model.PdbInfoDB;
import eppic.model.PdbInfoDB_;
import eppic.model.SeqClusterDB;
import eppic.model.SeqClusterDB_;

/**
 * Implementation of ChainClusterDAO.
 * @author AS
 *
 */
public class ChainClusterDAOJpa implements ChainClusterDAO
{
    @Override
    public List<ChainCluster> getChainClusters(int pdbInfoUid) throws DaoException
    {
	EntityManager entityManager = null;

	try
	{
	    List<ChainCluster> result = new ArrayList<ChainCluster>();

	    entityManager = EntityManagerHandler.getEntityManager();			
	    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

	    CriteriaQuery<ChainClusterDB> criteriaQuery = criteriaBuilder.createQuery(ChainClusterDB.class);
	    Root<ChainClusterDB> chainClusterRoot = criteriaQuery.from(ChainClusterDB.class);
	    Path<PdbInfoDB> pdbInfoPath = chainClusterRoot.get(ChainClusterDB_.pdbInfo);
	    Predicate condition = criteriaBuilder.equal(pdbInfoPath.get(PdbInfoDB_.uid), pdbInfoUid);
	    criteriaQuery.where(condition);

	    Query query = entityManager.createQuery(criteriaQuery);
	    @SuppressWarnings("unchecked")
	    List<ChainClusterDB> numHomologsStringItemDBs = query.getResultList();

	    for(ChainClusterDB homologsInfoItemDB : numHomologsStringItemDBs)
	    {
		result.add(ChainCluster.create(homologsInfoItemDB));
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
	    CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);

	    Root<ChainClusterDB> root = criteriaQuery.from(ChainClusterDB.class);
	    criteriaQuery.select(root.get(ChainClusterDB_.pdbInfo));
	    criteriaQuery.where(criteriaBuilder.equal(root.get(ChainClusterDB_.refUniProtId), uniProtId));
	    Query query = entityManager.createQuery(criteriaQuery);

	    @SuppressWarnings("unchecked")
	    List<PdbInfoDB> pdbItemDBs = query.getResultList();

	    for(PdbInfoDB pdbItemDB: pdbItemDBs){
		PDBSearchResult result = new PDBSearchResult(pdbItemDB.getUid(),
			pdbItemDB.getPdbCode(), 
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
    
    @Override
    public List<PDBSearchResult> getPdbSearchItems(String pdbCode, String repChain, int certanity)
	    throws DaoException {
	EntityManager entityManager = null;

	try
	{
	    List<PDBSearchResult> resultList = new ArrayList<PDBSearchResult>();

	    entityManager = EntityManagerHandler.getEntityManager();
	    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);
	    
	    Root<ChainClusterDB> root = criteriaQuery.from(ChainClusterDB.class);

	    SingularAttribute<SeqClusterDB, Integer> certanityColumn = getCertanityColumn(certanity);
	    long clusteId = getClusterId(pdbCode, repChain, certanityColumn);
	    
	    Subquery<Integer> chainClusterIdsQuery = criteriaQuery.subquery(Integer.class);
	    Root<SeqClusterDB> seqRoot = chainClusterIdsQuery.from(SeqClusterDB.class);
	    chainClusterIdsQuery.select(seqRoot.get(SeqClusterDB_.chainCluster).get(ChainClusterDB_.uid));
	    chainClusterIdsQuery.where(criteriaBuilder.equal(seqRoot.get(certanityColumn), clusteId));
	    
	    criteriaQuery.select(root.get(ChainClusterDB_.pdbInfo));
	    criteriaQuery.where(criteriaBuilder.in(root.get(ChainClusterDB_.uid)).value(chainClusterIdsQuery));
	    Query query = entityManager.createQuery(criteriaQuery);

	    @SuppressWarnings("unchecked")
	    List<PdbInfoDB> pdbItemDBs = query.getResultList();

	    for(PdbInfoDB pdbItemDB: pdbItemDBs){
		PDBSearchResult result = new PDBSearchResult(pdbItemDB.getUid(),
			pdbItemDB.getPdbCode(), 
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


    SingularAttribute<SeqClusterDB, Integer> getCertanityColumn(int certanity) {
	switch (certanity) {
	    case 100:
		return SeqClusterDB_.c100;
	    case 95:
		return SeqClusterDB_.c95;
	    case 90:
		return SeqClusterDB_.c90;
	    case 80:
		return SeqClusterDB_.c80;
	    case 70:
		return SeqClusterDB_.c70;
	    case 60:
		return SeqClusterDB_.c60;
	    case 50:
		return SeqClusterDB_.c50;
	    case 40:
		return SeqClusterDB_.c40;
	    case 30:
		return SeqClusterDB_.c30;
	    default:
		throw new IllegalArgumentException("Certanity should be one of the following values: 100, 95, 90, 80, 70, 60 ,50, 40 ,30");
	    }
    }


    private int getClusterId(String pdbCode, String repChain, SingularAttribute<SeqClusterDB, Integer> certanityColumn) throws DaoException {
	EntityManager entityManager = null;

	try
	{
	    entityManager = EntityManagerHandler.getEntityManager();
	    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
	    Root<SeqClusterDB> root = criteriaQuery.from(SeqClusterDB.class);
	    criteriaQuery.select(root.get(certanityColumn));
	    criteriaQuery.where(criteriaBuilder.equal(root.get(SeqClusterDB_.pdbCode), pdbCode),
		    criteriaBuilder.equal(root.get(SeqClusterDB_.repChain), repChain));
	    Query query = entityManager.createQuery(criteriaQuery);
	    
	    return (Integer)query.getSingleResult();
	    
	}catch(Throwable e)
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