package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.dao.ChainClusterDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import ch.systemsx.sybit.crkwebui.shared.model.SequenceClusterType;
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
public class ChainClusterDAOJpa implements ChainClusterDAO {
    
    private static final Logger log = LoggerFactory.getLogger(ChainClusterDAOJpa.class);

    
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
    public List<PDBSearchResult> getPdbSearchItems(String pdbCode, String repChain, SequenceClusterType sequenceClusterType)
	    throws DaoException {
	EntityManager entityManager = null;

	try
	{
	    List<PDBSearchResult> resultList = new ArrayList<PDBSearchResult>();

	    entityManager = EntityManagerHandler.getEntityManager();
	    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<PdbInfoDB> criteriaQuery = criteriaBuilder.createQuery(PdbInfoDB.class);
	    
	    Root<ChainClusterDB> root = criteriaQuery.from(ChainClusterDB.class);

	    SingularAttribute<SeqClusterDB, Integer> sequenceClusterTypeColumn = getSequenceClusterTypeColumn(sequenceClusterType);
	    long clusterId = getClusterId(pdbCode, repChain, sequenceClusterTypeColumn);
	    if(clusterId == 0 || clusterId == -1) {
		log.warn("clusterId is " + clusterId + " for pdbCode " + pdbCode + " repChain " + repChain + " sequenceClusterType " + sequenceClusterType);
		return null;
	    }
	    
	    Subquery<Integer> chainClusterIdsQuery = criteriaQuery.subquery(Integer.class);
	    Root<SeqClusterDB> seqRoot = chainClusterIdsQuery.from(SeqClusterDB.class);
	    chainClusterIdsQuery.select(seqRoot.get(SeqClusterDB_.chainCluster).get(ChainClusterDB_.uid));
	    chainClusterIdsQuery.where(criteriaBuilder.equal(seqRoot.get(sequenceClusterTypeColumn), clusterId));
	    
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
			pdbItemDB.getCellA(),
			pdbItemDB.getCellB(),
			pdbItemDB.getCellC(),
			pdbItemDB.getCellAlpha(),
			pdbItemDB.getCellBeta(),
			pdbItemDB.getCellGamma(),
			pdbItemDB.getResolution(), 
			pdbItemDB.getRfreeValue(), 
			pdbItemDB.getExpMethod(),
			pdbItemDB.getCrystalFormId(),
			sequenceClusterType);

		resultList.add(result);
	    }

	    return resultList;

	}
	catch(Throwable e)
	{
	    log.error("Query failed:", e);
	    return Collections.emptyList();
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


    SingularAttribute<SeqClusterDB, Integer> getSequenceClusterTypeColumn(SequenceClusterType sequenceClusterType) {
	switch (sequenceClusterType) {
	    case C100:
		return SeqClusterDB_.c100;
	    case C95:
		return SeqClusterDB_.c95;
	    case C90:
		return SeqClusterDB_.c90;
	    case C80:
		return SeqClusterDB_.c80;
	    case C70:
		return SeqClusterDB_.c70;
	    case C60:
		return SeqClusterDB_.c60;
	    case C50:
		return SeqClusterDB_.c50;
	    case C40:
		return SeqClusterDB_.c40;
	    case C30:
		return SeqClusterDB_.c30;
	    default:
		throw new IllegalArgumentException("SequenceClusterType should be one of the following values: 100, 95, 90, 80, 70, 60 ,50, 40 ,30");
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
	    
	}catch(NoResultException e) {
	    log.warn("No cluster id.");
	    return -1;
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