package eppic;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.jpautils.DbConfigGenerator;
import org.biojava.nbio.structure.EntityInfo;
import org.biojava.nbio.structure.EntityType;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.blast.BlastException;
import eppic.commons.sequence.Sequence;
import eppic.commons.sequence.SiftsConnection;
import eppic.commons.sequence.UniProtConnection;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;


public class ChainEvolContextList implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ChainEvolContextList.class);
	
	private Structure pdb;
	
	/**
	 * A map of sequence identifier (usually representative chain identifier) to its corresponding ChainEvolContext
	 */
	private TreeMap<String, ChainEvolContext> cecs; // one per representative chain
	
	private String uniprotVer;
	private int minNumSeqs;
	private int maxNumSeqs;
	private double queryCovCutoff;
	private double homSoftIdCutoff;
	private double homHardIdCutoff;
	
	private boolean useLocalUniprot;
	private transient UniProtConnection uniprotJapiConn;

	private transient SiftsConnection siftsConn;


	public ChainEvolContextList(Structure pdb, EppicParams params) throws SQLException {
		this.pdb = pdb;

		this.cecs = new TreeMap<String, ChainEvolContext>();

		if (!params.isNoBlast()) {
			// if we fail to read a version, it will stay null. Should we rather throw exception?
			this.uniprotVer = HomologList.readUniprotVer(params.getBlastDbDir());
			LOGGER.info("Using UniProt version "+uniprotVer+" for blasting");
		}

		this.useLocalUniprot = params.isUseLocalUniProtInfo();

		for (EntityInfo chainCluster:pdb.getEntityInfos()) {

			if (chainCluster.getType() == EntityType.POLYMER) {

				// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26
				if (chainCluster.getChains().isEmpty()) continue;

				ChainEvolContext cec = new ChainEvolContext(this, chainCluster);

				cecs.put(cec.getSequenceId(), cec);
			}
		}
		
	}
	
	public ChainEvolContextList(List<Sequence> sequences, EppicParams params) throws SQLException {
		this.pdb = null;
		
		this.cecs = new TreeMap<String, ChainEvolContext>();

		if (!params.isNoBlast()) {
			// if we fail to read a version, it will stay null. Should we rather throw exception?
			this.uniprotVer = HomologList.readUniprotVer(params.getBlastDbDir());
			LOGGER.info("Using UniProt version "+uniprotVer+" for blasting");
		}

        this.useLocalUniprot = params.isUseLocalUniProtInfo();
		
		for (Sequence sequence: sequences) {
						
			ChainEvolContext cec = new ChainEvolContext(this, sequence.getSeq(), sequence.getName());
			
			cecs.put(sequence.getName(), cec);
		}
		
	}
	
	public void addChainEvolContext(String representativeChain, ChainEvolContext cec) {
		this.cecs.put(representativeChain, cec);
	}
	
	/**
	 * Gets the ChainEvolContext corresponding to the given PDB chain code (can be 
	 * any PDB chain code, representative or not)
	 * @param pdbChainCode
	 * @return
	 */
	public ChainEvolContext getChainEvolContext(String pdbChainCode) {
		EntityInfo compound = pdb.getPolyChainByPDB(pdbChainCode).getEntityInfo();
		return cecs.get( compound.getRepresentative().getName() );
	}
	
	/**
	 * Gets the Collection of all ChainEvolContext (sorted alphabetically by representative PDB chain code) 
	 * @return
	 */
	public Collection<ChainEvolContext> getAllChainEvolContext() {
		return cecs.values();
	}
	
	public Structure getPdb() {
		return this.pdb;
	}
	
	public String getUniprotVer() {
		return this.uniprotVer;
	}
	
	public int getMinNumSeqs() {
		return minNumSeqs;
	}
	
	public int getMaxNumSeqs() {
		return maxNumSeqs;
	}
	
	public double getQueryCovCutoff() {
		return queryCovCutoff;
	}
	
	public double getHomSoftIdCutoff() {
		return homSoftIdCutoff;
	}
	
	public double getHomHardIdCutoff() {
		return homHardIdCutoff;
	}
	
	/**
	 * Returns the number of representative chains or -1 if not set 
	 * @return
	 */
	public int size() {
		if (cecs==null) return -1;
		return cecs.size();
	}
	
	public boolean isUseLocalUniprot() {
		return useLocalUniprot;
	}

	public UniProtConnection getUniProtJapiConnection() {
		return uniprotJapiConn;
	}
	
	public SiftsConnection getSiftsConn(String siftsLocation) throws IOException {
		// we store the sifts connection here in order not to reparse the file for every chain
		if (this.siftsConn==null) {
			this.siftsConn = new SiftsConnection(siftsLocation);
		}
		return this.siftsConn;
	}
	
	public void retrieveQueryData(EppicParams params) throws EppicException {
		params.getProgressLog().println("Finding query's UniProt mappings through SIFTS or blasting");
		params.getProgressLog().print("chains: ");
		
		openConnections(params);
		
		for (ChainEvolContext chainEvCont:cecs.values()) {
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			try {
				chainEvCont.retrieveQueryData(params);

			} catch (BlastException e) {
				throw new EppicException(e,"Couldn't run blast to retrieve query's UniProt mapping: "+e.getMessage(),true);
			} catch (IOException e) {
				throw new EppicException(e,"Problems while retrieving query data: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new EppicException(e,"Thread interrupted while running blast for retrieving query data: "+e.getMessage(),true);
			} 
			catch (Exception e) { // for any kind of exceptions thrown while connecting through uniprot JAPI
				String msg = null;
				if (useLocalUniprot) {
					// we don't want to catch unexpected exceptions in this case, only the JAPI case is problematic
					throw e;
				} else {
					msg = "Problems while retrieving query data through UniProt JAPI. Make sure you have the latest UniProtJAPI jar, or otherwise that the UniProt server is up\n"+e.getMessage();
				}
				throw new EppicException(e, msg, true);
			}
		}
		
		closeConnections();
		
		params.getProgressLog().println();
	}
	
	public void retrieveHomologs(EppicParams params) throws EppicException {
		
		// 1) we find homologs by blasting
		blastForHomologs(params);

		// 2) we remove the identicals to the query (removal base on blast id/coverage results, no further calculations needed)
		filterIdenticalsToQuery(params);
		
		// 3) we then retrieve the uniprot kb data: sequences, tax ids and taxons for all homologs above the given hard cutoffs thresholds
		// we won't need data for any other homolog
		retrieveHomologsData(params);
		
		// 4) and now we apply the identity (soft/hard) cutoffs, which also does redundancy reduction in each iteration
		applyIdentityCutoff(params);

		// then we filter optionally for domain of life (needs the taxon data above)
		if (params.isFilterByDomain()) filterToSameDomainOfLife();

	}
	
	private void blastForHomologs(EppicParams params) throws EppicException {
		params.getProgressLog().println("Blasting for homologs");
		params.getProgressLog().print("chains: ");

		openConnections(params);

		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
						
			try {
				chainEvCont.blastForHomologs(params);

			} catch (BlastException e) {
				throw new EppicException(e,"Couldn't run blast to retrieve homologs: "+e.getMessage() ,true);
			} catch (IOException|DaoException e) {
				throw new EppicException(e,"Problem while performing sequence search for sequence homologs: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new EppicException(e,"Thread interrupted while blasting for sequence homologs: "+e.getMessage(),true);
			}

		}

		closeConnections();

		params.getProgressLog().println();
	}
	
	private void retrieveHomologsData(EppicParams params) throws EppicException {
		params.getProgressLog().println("Retrieving UniProtKB data");
		params.getProgressLog().print("chains: ");
		
		openConnections(params);	
		
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			
			// we first apply the hard identity cutoff because that's the maximum list we can ever have after applying filters, i.e. that's all data we need
			chainEvCont.applyHardIdentityCutoff(params.getHomHardIdCutoff(), params.getQueryCoverageCutoff());
			
			try {
				chainEvCont.retrieveHomologsData();
			} catch (IOException e) {
				throw new EppicException(e, "Problems while retrieving homologs data: "+e.getMessage(),true);
			} catch (ServiceException e) {
				throw new EppicException(e, "Problems while retrieving homologs data from UniProt JAPI: "+e.getMessage(), true);
			} catch (Exception e) { // for any kind of exceptions thrown while connecting through uniprot JAPI
				String msg = null;
				if (useLocalUniprot) {
					msg = "Problems while retrieving homologs data from UniProt local database. Error "+e.getMessage();
				} else {
					msg = "Problems while retrieving homologs data through UniProt JAPI. Are UniProt servers down?. Error: "+e.getMessage();
				}
				throw new EppicException(e, msg, true);
			}

		}	
		
		closeConnections();
		
		params.getProgressLog().println();
	}
	
	private void applyIdentityCutoff(EppicParams params) throws EppicException {
		
		this.minNumSeqs = params.getMinNumSeqs();
		this.maxNumSeqs = params.getMaxNumSeqs();
		this.queryCovCutoff = params.getQueryCoverageCutoff();
		this.homSoftIdCutoff = params.getHomSoftIdCutoff();
		this.homHardIdCutoff = params.getHomHardIdCutoff();
		
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			try {
				
				// applies the identity cutoffs iteratively and performs the redundancy reduction procedure 
				chainEvCont.applyIdentityCutoff(params);


			} catch (IOException e) {
				throw new EppicException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			} catch (InterruptedException e) {
				throw new EppicException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			} catch (BlastException e) {
				throw new EppicException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			}

			
		}
	}
	
	private void filterToSameDomainOfLife() {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			chainEvCont.filterToSameDomainOfLife();
		}
	}
	
	private void filterIdenticalsToQuery(EppicParams params) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			// remove hits totally identical to query (based on the blast id and query coverage numbers)
			chainEvCont.removeIdenticalToQuery(params.getMinQueryCovForIdenticalsRemoval());
		}
	}
	
	public void align(EppicParams params) throws EppicException {
		
		String alignProgram = "clustalo"; 
		params.getProgressLog().println("Aligning protein sequences with "+ alignProgram);
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			try {
				chainEvCont.align(params);
			} catch (IOException e) {
				throw new EppicException(e, "Problems while running "+alignProgram+" to align protein sequences: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new EppicException(e, "Thread interrupted while running "+alignProgram+" to align protein sequences: "+e.getMessage(),true);
			} 

		}
		params.getProgressLog().println();
	}
	
	public void computeEntropies(EppicParams params) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			chainEvCont.computeEntropies(params.getAlphabet());
		}
	}
	
	/**
	 * Returns the Domain of Life (was known as Kingdom) of the entry.
	 * If different chains have different Domain of Life assignments then a warning is logged and null returned.
	 * If no query match is present for any entry a null is returned.
	 * @return
	 */
	public String getDomainOfLife() {
		String dol = null;

		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			if (chainEvCont.getQuery().hasTaxons()) {
				if (dol==null) {
					// we take as dol the first one available
					dol = chainEvCont.getQuery().getFirstTaxon();
				} else {					
					if (!chainEvCont.getQuery().getFirstTaxon().equals(dol)) {
						LOGGER.warn("Different domain of lifes for different chains of the same PDB. " +
							"First chain is "+dol+" while chain "+chainEvCont.getRepresentativeChainCode()+" is "+chainEvCont.getQuery().getFirstTaxon());
						dol = null;
						break;
					}					 
				}				
			}
		}
		return dol;
	}
	
	public void openConnections(EppicParams params) throws EppicException {
		if (params.getDbConfigFile()!=null) {
			// init jpa db connection so that querying cache works in chainEvCont.blastForHomologs(params)
			// and so that uniprot data can be retrieved in retrieveQueryData and retrieveHomologsData
			try {

				if (EntityManagerHandler.getEntityManagerFactory() == null) {
					LOGGER.info("Initialising JPA connection to for sequence search data and uniprot data.");
					Map<String, String> props = DbConfigGenerator.createDatabaseProperties(params.getDbConfigFile());
					EntityManagerHandler.initFactory(props);
				}

			} catch (IOException e) {
				throw new EppicException(e, "Could not init db JPA config from file " + params.getDbConfigFile() + ": " +e.getMessage(), true);
			}
		} else {
			this.uniprotJapiConn = new UniProtConnection();
			LOGGER.info("Opening remote UniProt JAPI connection to retrieve UniProtKB data");
		}
	}
	
	public void closeConnections() {
		if (!useLocalUniprot) {
			this.uniprotJapiConn.close();
			LOGGER.info("Connection to UniProt JAPI closed");
		}
		// else { // the JPA connection can't really be closed, nothing to do
	}
}
