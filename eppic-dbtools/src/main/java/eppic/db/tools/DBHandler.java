package eppic.db.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import eppic.model.shared.InputType;
import eppic.model.shared.StatusOfJob;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.JobDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.SeqClusterDB;

/**
 * Class to perform operations on the EPPIC database, such as adding by job,
 * removing a job or checking if a job is present in the database
 *
 * This is essentially a DAO layer. It duplicates what the {@link eppic.db.dao} package does.
 * TODO replace by new mongo DAO classes
 *
 * @author biyani_n
 *
 */
public class DBHandler {
	
	public static final String PERSISTENCE_UNIT_NAME = "eppicjpa";
	public static final String DEFAULT_CONFIG_FILE_NAME = "eppic-db.properties";
	public static final File DEFAULT_CONFIG_FILE = new File(System.getProperty("user.home"), DEFAULT_CONFIG_FILE_NAME);
	
	
	private EntityManagerFactory emf;
	
	/**
	 * Constructor.
	 * 
	 * @param dbName the database name
	 * @param userConfigFile a user supplied config file, if null the default location {@link #DEFAULT_CONFIG_FILE} will be used.
	 */
	public DBHandler(String dbName, File userConfigFile) {

	}

	public EntityManager getEntityManager(){
		//if (em==null)
		//	em = this.emf.createEntityManager();
		
		return this.emf.createEntityManager();
	}

	/**
	 * Deserialize a PdbInfoDB object given its pdbCode
	 * @param pdbCode
	 * @return
	 */
	public PdbInfoDB deserializePdb(String pdbCode) {
		EntityManager em = this.getEntityManager();
		
		return deserializePdb(em, pdbCode);
	}
	
	/**
	 * Deserialize a PdbInfoDB object given its pdbCode and and EntityManager
	 * @param em
	 * @param pdbCode
	 * @return
	 */
	private PdbInfoDB deserializePdb(EntityManager em, String pdbCode) {
//		CriteriaBuilder cbPDB = em.getCriteriaBuilder();
//
//		CriteriaQuery<PdbInfoDB> cqPDB = cbPDB.createQuery(PdbInfoDB.class);
//		Root<PdbInfoDB> rootPDB = cqPDB.from(PdbInfoDB.class);
//		cqPDB.where(cbPDB.equal(rootPDB.get(PdbInfoDB_.pdbCode), pdbCode));
//		cqPDB.select(rootPDB);
//		List<PdbInfoDB> queryPDBList = em.createQuery(cqPDB).getResultList();
//		if (queryPDBList.size()==0) return null;
//		else if (queryPDBList.size()>1) {
//			System.err.println("More than 1 PdbInfoDB returned for given PDB code: "+pdbCode);
//			return null;
//		}
//
//		// the em can't be closed here: because of LAZY fetching there can be later requests to db, should it be closed elsewhere?
//		// em.close();
//
//		return queryPDBList.get(0);
		return null;
	}

	/**
	 * Deserialize a List of PdbInfoDB objects given their pdbCodes
	 * @param pdbCodes
	 * @return
	 */
	public List<PdbInfoDB> deserializePdbList(Collection<String> pdbCodes) {
		
		List<PdbInfoDB> list = new ArrayList<PdbInfoDB>();
		

		for (String pdbCode:pdbCodes) {
			
			// as of 3.0.1, only 20 calls to deserializePdb(pdbCode) worked, then hanging forever
			// now using one em per call to see if it solves the problem - JD 2017-06-18
			EntityManager em = this.getEntityManager();
			
			PdbInfoDB pdbInfo = deserializePdb(em, pdbCode);
			if (pdbInfo!=null) {
				list.add(pdbInfo);
			}
			em.close();
		}
		
		return list;
	}

	/**
	 * Deserialize all PdbInfoDB objects belonging to given clusterId of given 
	 * clusterLevel 
	 * @param clusterId
	 * @param clusterLevel one of the available levels: 100, 95, 90, 80, 70, 60, 50, 40, 30
	 * @return
	 */
	public List<PdbInfoDB> deserializeSeqCluster(int clusterId, int clusterLevel) {
		Set<String> pdbCodes = new HashSet<String>();
		
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<SeqClusterDB> cq = cb.createQuery(SeqClusterDB.class);
		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
		
		SingularAttribute<SeqClusterDB, Integer> attribute = getSeqClusterDBAttribute(clusterLevel);
		
		cq.where(cb.equal(root.get(attribute), clusterId));
		cq.select(root);
		
		List<SeqClusterDB> results = em.createQuery(cq).getResultList();
		for (SeqClusterDB result:results) {
			pdbCodes.add(result.getPdbCode());
		}
		
		//em.close();
		
		return deserializePdbList(pdbCodes);
	}
	
	/**
	 * Gets the sequence cluster id for given pdbCode and clusterLevel
	 * @param pdbCode
	 * @param chain A chain (not necessarily the representative one
	 * @param clusterLevel
	 * @return
	 */
	public int getClusterIdForPdbCodeAndChain(String pdbCode, String chain, int clusterLevel) {
//		SingularAttribute<SeqClusterDB, Integer> attribute = getSeqClusterDBAttribute(clusterLevel);
//		if(attribute == null) {
//			System.err.println("Invalid cluster level "+clusterLevel);
//			return -1;
//		}
//
//		EntityManager em = this.getEntityManager();
//
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//
//		CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
//		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
//		Join<SeqClusterDB, ChainClusterDB> join = root.join(SeqClusterDB_.chainCluster);
//
//		cq.where(cb.equal(root.get(SeqClusterDB_.pdbCode), pdbCode), cb.or(
//				cb.equal(join.get(ChainClusterDB_.memberChains),chain),
//				cb.like(join.get(ChainClusterDB_.memberChains),chain+",%"),
//				cb.like(join.get(ChainClusterDB_.memberChains),"%,"+chain),
//				cb.like(join.get(ChainClusterDB_.memberChains),"%,"+chain+",%")
//				));
//
//		cq.select(root.get(attribute));
//
//		List<Integer> results = em.createQuery(cq).getResultList();
//
//		//em.close();
//
//		if (results.size()==0) return -1;
//		else if (results.size()>1) {
//			System.err.println("More than 1 SeqClusterDB returned for given PDB code and chain: "+pdbCode+chain);
//			return -1;
//		}
//
//		return results.get(0);
		return 0;
	}
	
	/**
	 * Gets the ChainClusters for all members of a sequence cluster
	 * @param clusterLevel
	 * @param seqClusterUid
	 * @return
	 */
	public List<ChainClusterDB> getClusterMembers(int clusterLevel, int seqClusterUid) {
//		EntityManager em = this.getEntityManager();
//
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//
//		CriteriaQuery<ChainClusterDB> cq = cb.createQuery(ChainClusterDB.class);
//		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
//
//		cq.where(cb.equal(root.get(getSeqClusterDBAttribute(clusterLevel)), seqClusterUid));
//
//		cq.select(root.get(SeqClusterDB_.chainCluster));
//
//		List<ChainClusterDB> results = em.createQuery(cq).getResultList();
//		return results;
		return null;
	}
	
	/**
	 * Get a list of all interfaces which have one partner in a particular cluster.
	 * 
	 * Each interface is mapped to an integer indicating whether chain1 (1),
	 * chain 2 (2), or both (3) matches the query cluster.
	 * @param cluster Query ChainCluster
	 * @return
	 */
	public Map<InterfaceDB,Integer> getInterfacesForChainCluster(ChainClusterDB cluster) {
//		EntityManager em = this.getEntityManager();
//
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//
//		String[] chains = cluster.getMemberChains().split(",");
//		PdbInfoDB pdbInfo = cluster.getPdbInfo();
//
//		// Start with list of interfaces which match chain1
//		CriteriaQuery<InterfaceDB> cq = cb.createQuery(InterfaceDB.class);
//		Root<InterfaceDB> root = cq.from(InterfaceDB.class);
//		Join<InterfaceDB,InterfaceClusterDB> clust = root.join(InterfaceDB_.interfaceCluster);
//
//		Predicate[] chainPredicates = new Predicate[chains.length];
//		for(int i=0;i<chains.length;i++) {
//			chainPredicates[i] = cb.equal(root.get(InterfaceDB_.chain1), chains[i]);
//		}
//
//		// Same PDB and any of the chains as chain1
//		cq.where(cb.and(
//				cb.equal(clust.get(InterfaceClusterDB_.pdbInfo), pdbInfo),
//				cb.or(chainPredicates) ));
//
//		cq.select(clust.getParent());
//
//		List<InterfaceDB> results = em.createQuery(cq).getResultList();
//
//		// add to the map with "1"
//		Map<InterfaceDB, Integer> interfaces = new HashMap<>();
//		for(InterfaceDB r : results) {
//			interfaces.put(r,1);
//		}
//
//		// Now search for chain2
//
//		cq = cb.createQuery(InterfaceDB.class);
//		root = cq.from(InterfaceDB.class);
//		clust = root.join(InterfaceDB_.interfaceCluster);
//
//		chainPredicates = new Predicate[chains.length];
//		for(int i=0;i<chains.length;i++) {
//			chainPredicates[i] = cb.equal(root.get(InterfaceDB_.chain2), chains[i]);
//		}
//
//		// Same PDB and any of the chains as chain1
//		cq.where(cb.and(
//				cb.equal(clust.get(InterfaceClusterDB_.pdbInfo), pdbInfo),
//				cb.or(chainPredicates) ));
//
//		cq.select(clust.getParent());
//
//		results = em.createQuery(cq).getResultList();
//
//		// add to the map with "2"
//		for(InterfaceDB r : results) {
//			if( interfaces.containsKey(r)) {
//				interfaces.put(r,3);
//			} else {
//				interfaces.put(r,2);
//			}
//		}
//
//		return interfaces;

		return null;
	}
	
	/**
	 * Gets all sequence cluster ids in the database
	 * @param clusterLevel
	 * @return
	 */
	public Set<Integer> getAllClusterIds(int clusterLevel) {
		
//		EntityManager em = this.getEntityManager();
//
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//
//		CriteriaQuery<SeqClusterDB> cq = cb.createQuery(SeqClusterDB.class);
//		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
//
//		cq.where(cb.greaterThan(root.get(SeqClusterDB_.c100), 0));
//
//		cq.multiselect(root.get(SeqClusterDB_.c100),root.get(SeqClusterDB_.c95),root.get(SeqClusterDB_.c90),
//				root.get(SeqClusterDB_.c80),root.get(SeqClusterDB_.c70),root.get(SeqClusterDB_.c60),
//				root.get(SeqClusterDB_.c50),root.get(SeqClusterDB_.c40),root.get(SeqClusterDB_.c30));
//
//		Set<Integer> list = new TreeSet<Integer>();
//
//		List<SeqClusterDB> results = em.createQuery(cq).getResultList();
//
//		//em.close();
//
//		int clusterId = -1;
//		for (SeqClusterDB result:results) {
//			switch (clusterLevel) {
//			case 100:
//				clusterId = result.getC100();
//				break;
//			case 95:
//				clusterId = result.getC95();
//				break;
//			case 90:
//				clusterId = result.getC90();
//				break;
//			case 80:
//				clusterId = result.getC80();
//				break;
//			case 70:
//				clusterId = result.getC70();
//				break;
//			case 60:
//				clusterId = result.getC60();
//				break;
//			case 50:
//				clusterId = result.getC50();
//				break;
//			case 40:
//				clusterId = result.getC40();
//				break;
//			case 30:
//				clusterId = result.getC30();
//				break;
//
//			}
//			if (clusterId>0) list.add(clusterId);
//		}
//		return list;
		return null;
	}
	
	private SingularAttribute<SeqClusterDB, Integer> getSeqClusterDBAttribute(int clusterLevel) {
//		SingularAttribute<SeqClusterDB, Integer> attribute = null;
//
//		switch (clusterLevel) {
//		case 100:
//			attribute = SeqClusterDB_.c100;
//			break;
//		case 95:
//			attribute = SeqClusterDB_.c95;
//			break;
//		case 90:
//			attribute = SeqClusterDB_.c90;
//			break;
//		case 80:
//			attribute = SeqClusterDB_.c80;
//			break;
//		case 70:
//			attribute = SeqClusterDB_.c70;
//			break;
//		case 60:
//			attribute = SeqClusterDB_.c60;
//			break;
//		case 50:
//			attribute = SeqClusterDB_.c50;
//			break;
//		case 40:
//			attribute = SeqClusterDB_.c40;
//			break;
//		case 30:
//			attribute = SeqClusterDB_.c30;
//			break;
//
//		}
//		return attribute;
		return null;
	}
	
	/**
	 * Gets a Map (chainCluster_uids to ChainClusterDB) of all chains in database
	 * that have non-null pdbCode and hasUniProtRef==true,
	 * only the pdbCode, repChain, pdbAlignedSeq, msaAlignedSeq and refAlignedSeq fields are read.
	 * @return
	 */
	public Map<Integer, ChainClusterDB> getAllChainsWithRef() {
		
//		EntityManager em = this.getEntityManager();
//
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//
//		CriteriaQuery<ChainClusterDB> cq = cb.createQuery(ChainClusterDB.class);
//		Root<ChainClusterDB> root = cq.from(ChainClusterDB.class);
//
//		cq.where(cb.isNotNull(root.get(ChainClusterDB_.pdbCode)));
//		cq.where(cb.isTrue(root.get(ChainClusterDB_.hasUniProtRef)));
//
//		cq.multiselect(
//				root.get(ChainClusterDB_.uid),
//				root.get(ChainClusterDB_.pdbCode),
//				root.get(ChainClusterDB_.repChain),
//				root.get(ChainClusterDB_.pdbAlignedSeq),
//				root.get(ChainClusterDB_.msaAlignedSeq),
//				root.get(ChainClusterDB_.refAlignedSeq));
//
//		List<ChainClusterDB> results = em.createQuery(cq).getResultList();
//
//		//em.close();
//
//		Map<Integer, ChainClusterDB> map = new TreeMap<Integer, ChainClusterDB>();
//
//		for (ChainClusterDB result:results) {
//			int uid = result.getUid();
//			map.put(uid, result);
//		}
//		return map;
		return null;
	}
	
	/**
	 * Returns true if SeqCluster table is empty, false otherwise.
	 * @return
	 */
	public boolean checkSeqClusterEmpty() {
//		EntityManager em = this.getEntityManager();
//
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//
//		CriteriaQuery<SeqClusterDB> cq = cb.createQuery(SeqClusterDB.class);
//		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
//
//		cq.multiselect(root.get(SeqClusterDB_.c100),root.get(SeqClusterDB_.c95),root.get(SeqClusterDB_.c90),
//				root.get(SeqClusterDB_.c80),root.get(SeqClusterDB_.c70),root.get(SeqClusterDB_.c60),
//				root.get(SeqClusterDB_.c50),root.get(SeqClusterDB_.c40),root.get(SeqClusterDB_.c30));
//
//		List<SeqClusterDB> results = em.createQuery(cq).getResultList();
//
//		//em.close();
//
//		int size = results.size();
//
//		if(size==0)
//			return true;
//
//		return false;
		return false;

	}
	
	/**
	 * Persists the given SeqClusterDB
	 * @param seqCluster
	 */
	public void persistSeqCluster(SeqClusterDB seqCluster) {
		EntityManager em = this.getEntityManager();
				
		em.getTransaction().begin();
		em.persist(seqCluster);
		em.getTransaction().commit();
		em.close();
	}

}
