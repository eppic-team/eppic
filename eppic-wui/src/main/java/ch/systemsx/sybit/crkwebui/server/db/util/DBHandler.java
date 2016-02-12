/**
 * 
 */
package ch.systemsx.sybit.crkwebui.server.db.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.Map;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import eppic.commons.util.DbConfigGenerator;

import eppic.model.PdbInfoDB;
import eppic.model.PdbInfoDB_;

/**
 * Class to perform operations on the EPPIC database, such as adding by job,
 * removing a job or checking if a job is present in the database
 * @author biyani_n
 *
 */
public class DBHandler {
	
	public static final String PERSISTENCE_UNIT_NAME = "eppicjpa";
	public static final String CONFIG_FILE_NAME = "eppic-db.properties";
	
	
	@PersistenceUnit
	private EntityManagerFactory emf;
	
	/**
	 * Constructor
	 */
	public DBHandler(String dbName) {

		File configurationFile = new File(System.getProperty("user.home"), CONFIG_FILE_NAME);
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

	protected EntityManager getEntityManager(){
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

		// the em can't be close here: because of LAZY fetching there can be later requests to db, should it be closed elsewhere? 
		//em.close();
		
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
			
			PdbInfoDB pdbInfo = deserializePdb(pdbCode);
			if (pdbInfo!=null) {
				list.add(pdbInfo);
			}
		}
		
		return list;
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
