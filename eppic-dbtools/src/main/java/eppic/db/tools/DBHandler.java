/**
 * 
 */
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
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import ch.systemsx.sybit.shared.model.InputType;
import ch.systemsx.sybit.shared.model.StatusOfJob;
import eppic.commons.util.DbConfigGenerator;
import eppic.model.ChainClusterDB;
import eppic.model.ChainClusterDB_;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterDB_;
import eppic.model.InterfaceDB;
import eppic.model.InterfaceDB_;
import eppic.model.JobDB;
import eppic.model.JobDB_;
import eppic.model.PdbInfoDB;
import eppic.model.PdbInfoDB_;
import eppic.model.ResidueBurialDB;
import eppic.model.ResidueBurialDB_;
import eppic.model.ResidueInfoDB;
import eppic.model.SeqClusterDB;
import eppic.model.SeqClusterDB_;

/**
 * Class to perform operations on the EPPIC database, such as adding by job,
 * removing a job or checking if a job is present in the database
 * @author biyani_n
 *
 */
public class DBHandler {
	
	public static final String PERSISTENCE_UNIT_NAME = "eppicjpa";
	public static final String DEFAULT_CONFIG_FILE_NAME = "eppic-db.properties";
	public static final File DEFAULT_CONFIG_FILE = new File(System.getProperty("user.home"), DEFAULT_CONFIG_FILE_NAME);
	
	
	@PersistenceUnit
	private EntityManagerFactory emf;
	
	/**
	 * Constructor.
	 * 
	 * @param dbName the database name
	 * @param userConfigFile a user supplied config file, if null the default location {@value #DEFAULT_CONFIG_FILE} will be used.
	 */
	public DBHandler(String dbName, File userConfigFile) {
		
		File configurationFile = null;
		
		if (userConfigFile!=null) {			
			configurationFile = userConfigFile;
		} else {
			// default location: config file read from user's home directory
			configurationFile = DEFAULT_CONFIG_FILE;
		}
		
		System.out.println("Configuration will be loaded from file " + configurationFile.toString());
		
		
		Map<String, String> properties = null;
		try {
			properties = DbConfigGenerator.createDatabaseProperties(configurationFile, dbName);
			
			System.out.println("Using database "+dbName+", jdbc url is: "+properties.get("javax.persistence.jdbc.url")); 
			
		} catch (IOException e) {
			System.err.println("Problems while reading the configuration file "+configurationFile+". Error: "+e.getMessage());
			System.exit(1);
		}
		
		try {
			this.emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);

		} catch (PersistenceException e){	
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println("Error initializing Entity Manager Factory");
			System.err.println("Please check that the database '"+dbName+"' is really present and that the login parameters are correct in file "+configurationFile);
			System.exit(1);
		}
	}

	public EntityManager getEntityManager(){
		//if (em==null)
		//	em = this.emf.createEntityManager();
		
		return this.emf.createEntityManager();
	}

	
	/**
	 * Persists the given PdbInfoDB, using an empty place-holder job
	 * @param pdbInfo
	 * 
	 */
	public void persistFinishedJob(EntityManager em, PdbInfoDB pdbInfo){
		
		em.getTransaction().begin(); 
		
		String pdbCode = pdbInfo.getPdbCode();

		JobDB job = new JobDB();
		job.setJobId(pdbCode);
		job.setEmail(null);
		job.setInputName(pdbCode);
		job.setIp("localhost");
		job.setStatus(StatusOfJob.FINISHED.getName());
		job.setSubmissionDate(new Date());
		job.setInputType(InputType.PDBCODE.getIndex());
		job.setSubmissionId("-1");

		pdbInfo.setJob(job);
		job.setPdbInfo(pdbInfo);
				
		em.persist(job);
		
		em.getTransaction().commit();
		em.clear(); 
	}
	
	/**
	 * Persists a JobDB with given pdbCode, assigning error status to it
	 * @param pdbCode
	 */
	public void persistErrorJob(EntityManager em, String pdbCode){

		em.getTransaction().begin(); 
		
		JobDB job = new JobDB();
		job.setJobId(pdbCode);
		job.setEmail(null);
		job.setInputName(pdbCode);
		job.setIp("localhost");
		job.setStatus(StatusOfJob.ERROR.getName());
		job.setSubmissionDate(new Date());
		job.setInputType(InputType.PDBCODE.getIndex());
		job.setSubmissionId("-1");
		em.persist(job);
		
		em.getTransaction().commit();
		em.clear();   
		
	}
	
	/**
	 * Removes entry from the DataBase for the given jobID
	 * @param jobID
	 * @return true if removed; false if not
	 */
	public boolean removeJob(String jobID){
		EntityManager entityManager = this.getEntityManager();

		CriteriaBuilder cbJob = entityManager.getCriteriaBuilder();

		CriteriaQuery<JobDB> cqJob = cbJob.createQuery(JobDB.class);
		Root<JobDB> rootJob = cqJob.from(JobDB.class);
		cqJob.where(cbJob.equal(rootJob.get(JobDB_.jobId), jobID));
		cqJob.select(rootJob);
		List<JobDB> queryJobList = entityManager.createQuery(cqJob).getResultList();
		int querySize = queryJobList.size();
		
		if(querySize>0){
			entityManager.getTransaction().begin();
			for(JobDB job:queryJobList){
				try{
					// Remove the PdbScore entries related to the job
					CriteriaBuilder cbPDB = entityManager.getCriteriaBuilder();

					CriteriaQuery<PdbInfoDB> cqPDB = cbPDB.createQuery(PdbInfoDB.class);
					Root<PdbInfoDB> rootPDB = cqPDB.from(PdbInfoDB.class);
					cqPDB.where(cbPDB.equal(rootPDB.get(PdbInfoDB_.job), job));
					cqPDB.select(rootPDB);
					List<PdbInfoDB> queryPDBList = entityManager.createQuery(cqPDB).getResultList();
					
					if( queryPDBList != null) for(PdbInfoDB itemPDB : queryPDBList) entityManager.remove(itemPDB);
				
					// Remove Job
					entityManager.remove(job);
					
				}
				catch (Throwable e){
					System.err.println("Error in Removing Job from DB: "+e.getMessage());
					//e.printStackTrace();
				}
			}
			entityManager.getTransaction().commit();
			entityManager.close();
			return true;
		}
		else {
			entityManager.close();
			return false;
		}	
		
	}
	
	/**
	 * Checks if a non-error entry with the given jobID exists in the DataBase
	 * Note this returns true whenever the query returns at least 1 record, i.e. it doesn't guarantee
	 * that the entry is complete with data present in all cascading tables
	 * @param jobID
	 * @return true if present; false if not
	 */
	public boolean checkJobExist(String jobID){
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cbJob = em.getCriteriaBuilder();

		CriteriaQuery<JobDB> cqJob = cbJob.createQuery(JobDB.class);
		Root<JobDB> rootJob = cqJob.from(JobDB.class);
		cqJob.where(cbJob.equal(rootJob.get(JobDB_.jobId), jobID));
		//cqJob.select(rootJob);
		
		// in order to make the query light-weight we only take these 2 fields for which there is 
		// a corresponding constructor in JobDB
		// with the simple 'select' above the query is a monster with many joins (following the full hierarchy of Job, PdbInfo etc) 
		cqJob.multiselect(rootJob.get(JobDB_.inputName), rootJob.get(JobDB_.inputType), rootJob.get(JobDB_.status));
		List<JobDB> queryJobList = em.createQuery(cqJob).getResultList();
		int querySize = queryJobList.size();
		
		if(querySize>0 && !queryJobList.get(0).getStatus().equals("Error")) {
			return true;
		}
		
		return false;		
	}
	
	/**
	 * Checks if entries with the given jobIds exist in the DataBase
	 * Note this returns true whenever the query returns at least 1 record, i.e. it doesn't guarantee
	 * that the entries are complete with data present in all cascading tables
	 * @param jobIds
	 * @return true if present; false if not
	 */
	public boolean checkJobsExist(List<String> jobIds){
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<JobDB> cq = cb.createQuery(JobDB.class);
		Root<JobDB> rootJob = cq.from(JobDB.class);
		
		Predicate predicate = cb.conjunction();
		for (String jobId:jobIds) {
		    Predicate newPredicate = cb.equal(rootJob.get(JobDB_.jobId), jobId);
		    predicate = cb.and(predicate, newPredicate);
		}
		cq.where(predicate);
		
		
		// in order to make the query light-weight we only take these 2 fields for which there is 
		// a corresponding constructor in JobDB
		// with the simple 'select' above the query is a monster with many joins (following the full hierarchy of Job, PdbInfo etc) 
		cq.multiselect(rootJob.get(JobDB_.inputName), rootJob.get(JobDB_.inputType));
		List<JobDB> queryJobList = em.createQuery(cq).getResultList();
		int querySize = queryJobList.size();
		
		em.close();
		
		if(querySize>0) return true;
		else return false;
		
	}
	
	/**
	 * Copies a job from this database to another database
	 * @param dbhCopier DBHandler object to copy to
	 * @param jobDir the directory where the webui.dat files for the job resides
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * 
	 */
	public void copytoDB(DBHandler dbhCopier, File jobDir) throws IOException, ClassNotFoundException{
		EntityManager orig = this.getEntityManager();
		EntityManager copier = dbhCopier.getEntityManager();
		
		//Get the job from the database
		CriteriaBuilder cbJob = orig.getCriteriaBuilder();

		CriteriaQuery<JobDB> cqJob = cbJob.createQuery(JobDB.class);
		Root<JobDB> rootJob = cqJob.from(JobDB.class);
		cqJob.where(cbJob.equal(rootJob.get(JobDB_.jobId), jobDir.getName()));
		cqJob.select(rootJob);
		JobDB queryJobOrig = orig.createQuery(cqJob).getSingleResult();
				
		//Create a new Job which is to be copied
		JobDB queryJob = new JobDB(queryJobOrig.getInputName(), queryJobOrig.getInputType());
		queryJob.setEmail(queryJobOrig.getEmail());
		queryJob.setIp(queryJobOrig.getIp());
		queryJob.setJobId(queryJobOrig.getJobId());
		queryJob.setStatus(queryJobOrig.getStatus());
		queryJob.setSubmissionDate(queryJobOrig.getSubmissionDate());
		queryJob.setSubmissionId(queryJobOrig.getSubmissionId());
		
		
		//Check if there is any PdbInfo entry 
		CriteriaBuilder cbPDB = orig.getCriteriaBuilder();
		CriteriaQuery<PdbInfoDB> cqPDB = cbPDB.createQuery(PdbInfoDB.class);
		Root<PdbInfoDB> rootPDB = cqPDB.from(PdbInfoDB.class);
		cqPDB.where(cbPDB.equal(rootPDB.get(PdbInfoDB_.job), queryJobOrig));
		cqPDB.select(rootPDB);
		int pdbScoreNum = orig.createQuery(cqPDB).getResultList().size();
		
		orig.close();		
		
		// Add Job to Copier database
		copier.getTransaction().begin();
		
		if( pdbScoreNum >= 1) {
			PdbInfoDB queryPDB = this.readFromSerializedFile(jobDir);
			copier.persist(queryPDB);
						
			queryPDB.setJob(queryJob);
			queryJob.setPdbInfo(queryPDB);
			copier.persist(queryJob);
		}
		else copier.persist(queryJob);
		
		copier.getTransaction().commit();	
		copier.close();
		
	}
	
	/**
	 * Returns a list of user-jobs from the database
	 * @param 
	 * @return 
	 */
	public List<String> getUserJobList(){
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
		criteriaQuery.select(jobRoot.get(JobDB_.jobId));
		Predicate condition = criteriaBuilder.notEqual(jobRoot.get(JobDB_.submissionId), -1);
		criteriaQuery.where(condition);

		List<String> queryJobList = entityManager.createQuery(criteriaQuery).getResultList();
		
		entityManager.close();
		
		return queryJobList;
		
	}
	
	/**
	 * Returns a list of user-jobs from the database newer than the specified date
	 * @param afterDate
	 * @return 
	 */
	public List<String> getUserJobList(Date afterDate){
		EntityManager entityManager = this.getEntityManager();
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
		criteriaQuery.select(jobRoot.get(JobDB_.jobId));
		Predicate idCondition = criteriaBuilder.notEqual(jobRoot.get(JobDB_.submissionId), -1);
		Predicate dateCondition = criteriaBuilder.greaterThanOrEqualTo(jobRoot.get(JobDB_.submissionDate), afterDate);
		Predicate condition = criteriaBuilder.and(idCondition, dateCondition);
		criteriaQuery.where(condition);

		List<String> queryJobList = entityManager.createQuery(criteriaQuery).getResultList();
		
		entityManager.close();
		
		return queryJobList;
		
	}
	
	/**
	 * Returns a PdbInfoDB object after reading it from the serialized .webui.dat file
	 * @param jobDir directory containing the webui.dat file
	 * @return 
	 */
	private PdbInfoDB readFromSerializedFile(File jobDir) throws IOException, ClassNotFoundException{		
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<JobDB> jobRoot = criteriaQuery.from(JobDB.class);
		criteriaQuery.select(jobRoot.get(JobDB_.inputName));
		Predicate condition = criteriaBuilder.equal(jobRoot.get(JobDB_.jobId), jobDir.getName());
		criteriaQuery.where(condition);

		String pdbID = em.createQuery(criteriaQuery).getSingleResult();
		int lastPeriodPos = pdbID.lastIndexOf('.');
		if (lastPeriodPos >= 0)
	    {
			pdbID = pdbID.substring(0, lastPeriodPos);
	    }
		
		File webuiFile = new File(jobDir, pdbID + ".webui.dat");
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(webuiFile));
		PdbInfoDB pdbScoreItem = (PdbInfoDB)in.readObject();
		in.close();
		
		em.close();
		
		return pdbScoreItem;
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
	public PdbInfoDB deserializePdb(EntityManager em, String pdbCode) {
		CriteriaBuilder cbPDB = em.getCriteriaBuilder();

		CriteriaQuery<PdbInfoDB> cqPDB = cbPDB.createQuery(PdbInfoDB.class);
		Root<PdbInfoDB> rootPDB = cqPDB.from(PdbInfoDB.class);
		cqPDB.where(cbPDB.equal(rootPDB.get(PdbInfoDB_.pdbCode), pdbCode));
		cqPDB.select(rootPDB);
		List<PdbInfoDB> queryPDBList = em.createQuery(cqPDB).getResultList();
		if (queryPDBList.size()==0) return null;
		else if (queryPDBList.size()>1) {
			System.err.println("More than 1 PdbInfoDB returned for given PDB code: "+pdbCode);
			return null;
		}

		// the em can't be closed here: because of LAZY fetching there can be later requests to db, should it be closed elsewhere? 
		// em.close();
		
		return queryPDBList.get(0);
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
	 * @param repChain
	 * @param clusterLevel
	 * @return
	 */
	public int getClusterIdForPdbCode(String pdbCode, String repChain, int clusterLevel) {
		
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<SeqClusterDB> cq = cb.createQuery(SeqClusterDB.class);
		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
		
		cq.where(cb.equal(root.get(SeqClusterDB_.pdbCode), pdbCode), cb.equal(root.get(SeqClusterDB_.repChain),repChain));
		cq.select(root);
		
		List<SeqClusterDB> results = em.createQuery(cq).getResultList();
		
		//em.close();
		
		if (results.size()==0) return -1;
		else if (results.size()>1) {
			System.err.println("More than 1 SeqClusterDB returned for given PDB code and chain: "+pdbCode+repChain);
			return -1;
		}
		
		switch (clusterLevel) {
		case 100:
			return results.get(0).getC100();
		case 95:
			return results.get(0).getC95();
		case 90:
			return results.get(0).getC90();
		case 80:
			return results.get(0).getC80();
		case 70:
			return results.get(0).getC70();
		case 60:
			return results.get(0).getC60();
		case 50:
			return results.get(0).getC50();
		case 40:
			return results.get(0).getC40();
		case 30:
			return results.get(0).getC30();

		}
		
		return -1;
	}
	/**
	 * Gets the sequence cluster id for given pdbCode and clusterLevel
	 * @param pdbCode
	 * @param chain A chain (not necessarily the representative one
	 * @param clusterLevel
	 * @return
	 */
	public int getClusterIdForPdbCodeAndChain(String pdbCode, String chain, int clusterLevel) {
		SingularAttribute<SeqClusterDB, Integer> attribute = getSeqClusterDBAttribute(clusterLevel);
		if(attribute == null) {
			System.err.println("Invalid cluster level "+clusterLevel);
			return -1;
		}
		
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
		Join<SeqClusterDB, ChainClusterDB> join = root.join(SeqClusterDB_.chainCluster);
		
		cq.where(cb.equal(root.get(SeqClusterDB_.pdbCode), pdbCode), cb.or(
				cb.equal(join.get(ChainClusterDB_.memberChains),chain),
				cb.like(join.get(ChainClusterDB_.memberChains),chain+",%"),
				cb.like(join.get(ChainClusterDB_.memberChains),"%,"+chain),
				cb.like(join.get(ChainClusterDB_.memberChains),"%,"+chain+",%")
				));

		cq.select(root.get(attribute));
		
		List<Integer> results = em.createQuery(cq).getResultList();
		
		//em.close();
		
		if (results.size()==0) return -1;
		else if (results.size()>1) {
			System.err.println("More than 1 SeqClusterDB returned for given PDB code and chain: "+pdbCode+chain);
			return -1;
		}

		return results.get(0);
	}
	
	/**
	 * Gets the ChainClusters for all members of a sequence cluster
	 * @param clusterLevel
	 * @param seqClusterUid
	 * @return
	 */
	public List<ChainClusterDB> getClusterMembers(int clusterLevel, int seqClusterUid) {
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<ChainClusterDB> cq = cb.createQuery(ChainClusterDB.class);
		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);

		cq.where(cb.equal(root.get(getSeqClusterDBAttribute(clusterLevel)), seqClusterUid));

		cq.select(root.get(SeqClusterDB_.chainCluster));
		
		List<ChainClusterDB> results = em.createQuery(cq).getResultList();
		return results;
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
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		String[] chains = cluster.getMemberChains().split(",");
		PdbInfoDB pdbInfo = cluster.getPdbInfo();

		// Start with list of interfaces which match chain1
		CriteriaQuery<InterfaceDB> cq = cb.createQuery(InterfaceDB.class);
		Root<InterfaceDB> root = cq.from(InterfaceDB.class);
		Join<InterfaceDB,InterfaceClusterDB> clust = root.join(InterfaceDB_.interfaceCluster);

		Predicate[] chainPredicates = new Predicate[chains.length];
		for(int i=0;i<chains.length;i++) {
			chainPredicates[i] = cb.equal(root.get(InterfaceDB_.chain1), chains[i]);
		}
		
		// Same PDB and any of the chains as chain1
		cq.where(cb.and(
				cb.equal(clust.get(InterfaceClusterDB_.pdbInfo), pdbInfo),
				cb.or(chainPredicates) ));
		
		cq.select(clust.getParent());
		
		List<InterfaceDB> results = em.createQuery(cq).getResultList();
		
		// add to the map with "1"
		Map<InterfaceDB, Integer> interfaces = new HashMap<>();
		for(InterfaceDB r : results) {
			interfaces.put(r,1);
		}
		
		// Now search for chain2

		cq = cb.createQuery(InterfaceDB.class);
		root = cq.from(InterfaceDB.class);
		clust = root.join(InterfaceDB_.interfaceCluster);

		chainPredicates = new Predicate[chains.length];
		for(int i=0;i<chains.length;i++) {
			chainPredicates[i] = cb.equal(root.get(InterfaceDB_.chain2), chains[i]);
		}
		
		// Same PDB and any of the chains as chain1
		cq.where(cb.and(
				cb.equal(clust.get(InterfaceClusterDB_.pdbInfo), pdbInfo),
				cb.or(chainPredicates) ));
		
		cq.select(clust.getParent());
		
		results = em.createQuery(cq).getResultList();

		// add to the map with "2"
		for(InterfaceDB r : results) {
			if( interfaces.containsKey(r)) {
				interfaces.put(r,3);
			} else {
				interfaces.put(r,2);
			}
		}
		
		return interfaces;

	}
	
	/**
	 * Gets all sequence cluster ids in the database
	 * @param clusterLevel
	 * @return
	 */
	public Set<Integer> getAllClusterIds(int clusterLevel) {
		
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<SeqClusterDB> cq = cb.createQuery(SeqClusterDB.class);
		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);

		cq.where(cb.greaterThan(root.get(SeqClusterDB_.c100), 0)); 

		cq.multiselect(root.get(SeqClusterDB_.c100),root.get(SeqClusterDB_.c95),root.get(SeqClusterDB_.c90),
				root.get(SeqClusterDB_.c80),root.get(SeqClusterDB_.c70),root.get(SeqClusterDB_.c60),
				root.get(SeqClusterDB_.c50),root.get(SeqClusterDB_.c40),root.get(SeqClusterDB_.c30));

		Set<Integer> list = new TreeSet<Integer>();

		List<SeqClusterDB> results = em.createQuery(cq).getResultList();

		//em.close();
		
		int clusterId = -1;
		for (SeqClusterDB result:results) {
			switch (clusterLevel) {
			case 100:
				clusterId = result.getC100();
				break;
			case 95:
				clusterId = result.getC95();
				break;
			case 90:
				clusterId = result.getC90();
				break;
			case 80:
				clusterId = result.getC80();
				break;
			case 70:
				clusterId = result.getC70();
				break;
			case 60:
				clusterId = result.getC60();
				break;
			case 50:				
				clusterId = result.getC50();
				break;
			case 40:
				clusterId = result.getC40();
				break;
			case 30:
				clusterId = result.getC30();
				break;

			}
			if (clusterId>0) list.add(clusterId);
		}
		return list;
	}
	
	private SingularAttribute<SeqClusterDB, Integer> getSeqClusterDBAttribute(int clusterLevel) {
		SingularAttribute<SeqClusterDB, Integer> attribute = null;
		
		switch (clusterLevel) {
		case 100:
			attribute = SeqClusterDB_.c100;
			break;
		case 95:
			attribute = SeqClusterDB_.c95;
			break;
		case 90:
			attribute = SeqClusterDB_.c90;
			break;
		case 80:
			attribute = SeqClusterDB_.c80;
			break;
		case 70:
			attribute = SeqClusterDB_.c70;
			break;
		case 60:
			attribute = SeqClusterDB_.c60;
			break;
		case 50:
			attribute = SeqClusterDB_.c50;
			break;
		case 40:
			attribute = SeqClusterDB_.c40;
			break;
		case 30:
			attribute = SeqClusterDB_.c30;
			break;
			
		}
		return attribute;
	}
	
	/**
	 * Gets a Map (chainCluster_uids to ChainClusterDB) of all chains in database
	 * that have non-null pdbCode and hasUniProtRef==true,
	 * only the pdbCode, repChain, pdbAlignedSeq, msaAlignedSeq and refAlignedSeq fields are read.
	 * @return
	 */
	public Map<Integer, ChainClusterDB> getAllChainsWithRef() {
		
		EntityManager em = this.getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<ChainClusterDB> cq = cb.createQuery(ChainClusterDB.class);
		Root<ChainClusterDB> root = cq.from(ChainClusterDB.class);

		cq.where(cb.isNotNull(root.get(ChainClusterDB_.pdbCode)));
		cq.where(cb.isTrue(root.get(ChainClusterDB_.hasUniProtRef)));

		cq.multiselect(
				root.get(ChainClusterDB_.uid),
				root.get(ChainClusterDB_.pdbCode),
				root.get(ChainClusterDB_.repChain),
				root.get(ChainClusterDB_.pdbAlignedSeq),
				root.get(ChainClusterDB_.msaAlignedSeq),
				root.get(ChainClusterDB_.refAlignedSeq));

		List<ChainClusterDB> results = em.createQuery(cq).getResultList();
		
		//em.close();
		
		Map<Integer, ChainClusterDB> map = new TreeMap<Integer, ChainClusterDB>();
		
		for (ChainClusterDB result:results) {
			int uid = result.getUid();
			map.put(uid, result);
		}
		return map;
	}
	
	/**
	 * Returns true if SeqCluster table is empty, false otherwise.
	 * @return
	 */
	public boolean checkSeqClusterEmpty() {
		EntityManager em = this.getEntityManager();

		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<SeqClusterDB> cq = cb.createQuery(SeqClusterDB.class);
		Root<SeqClusterDB> root = cq.from(SeqClusterDB.class);
		
		cq.multiselect(root.get(SeqClusterDB_.c100),root.get(SeqClusterDB_.c95),root.get(SeqClusterDB_.c90),
				root.get(SeqClusterDB_.c80),root.get(SeqClusterDB_.c70),root.get(SeqClusterDB_.c60),
				root.get(SeqClusterDB_.c50),root.get(SeqClusterDB_.c40),root.get(SeqClusterDB_.c30));

		List<SeqClusterDB> results = em.createQuery(cq).getResultList();
		
		//em.close();
		
		int size = results.size();
		
		if(size==0) 
			return true;
	
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

	public List<ResidueInfoDB> getResiduesForInterface(InterfaceDB face, boolean side, short region) {
		EntityManager em = this.getEntityManager();

		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<ResidueInfoDB> cq = cb.createQuery(ResidueInfoDB.class);
		Root<ResidueBurialDB> root = cq.from(ResidueBurialDB.class);

		cq.where(cb.and(
				cb.equal(root.get(ResidueBurialDB_.interfaceItem),face),
				cb.equal(root.get(ResidueBurialDB_.side), side),
				cb.greaterThanOrEqualTo(root.get(ResidueBurialDB_.region), region)));

		cq.select(root.get(ResidueBurialDB_.residueInfo));

		List<ResidueInfoDB> results = em.createQuery(cq).getResultList();

		return results;
	}

	public List<String> getAllPdbCodes() {
		EntityManager em = this.getEntityManager();
	
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<PdbInfoDB> cq = cb.createQuery(PdbInfoDB.class);
		Root<PdbInfoDB> root = cq.from(PdbInfoDB.class);

		cq.where(cb.isNotNull(root.get(PdbInfoDB_.pdbCode)));
		
		//cq.multiselect(root.get(PdbInfoDB_.pdbCode));
		
		List<PdbInfoDB> results = em.createQuery(cq).getResultList();
		
		List<String> pdbCodes = new ArrayList<String>();
		
		for (PdbInfoDB result:results) {
			pdbCodes.add(result.getPdbCode());
		}
		
		return pdbCodes;
	}
}
