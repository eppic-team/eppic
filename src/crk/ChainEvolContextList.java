package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.connections.UniProtConnection;
import owl.core.connections.UniprotLocalConnection;
import owl.core.runners.TcoffeeException;
import owl.core.runners.blast.BlastException;
import owl.core.sequence.HomologList;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;


public class ChainEvolContextList implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ChainEvolContextList.class);
	
	private TreeMap<String, ChainEvolContext> cecs; // one per representative chain
	private HashMap<String,String> chain2repChain; // pdb chain codes to pdb chain codes of representative chain
	
	private String pdbName;
	
	private String uniprotVer;
	private int minNumSeqs;
	private int maxNumSeqs;
	private double queryCovCutoff;
	private double homSoftIdCutoff;
	private double homHardIdCutoff;
	
	private boolean useLocalUniprot;
	private transient UniProtConnection uniprotJapiConn;
	private transient UniprotLocalConnection uniprotLocalConn;

	
	public ChainEvolContextList(PdbAsymUnit pdb, CRKParams params) throws SQLException {
		this.cecs = new TreeMap<String, ChainEvolContext>();
		this.chain2repChain = pdb.getChain2repChainMap();
		this.pdbName = params.getJobName();
		// if we fail to read a version, it will stay null. Should we rather throw exception?
		this.uniprotVer = HomologList.readUniprotVer(params.getBlastDbDir());
		LOGGER.info("Using UniProt version "+uniprotVer+" for blasting");
		
		if (params.getLocalUniprotDbName()!=null) {
			this.useLocalUniprot = true;
			this.uniprotLocalConn = new UniprotLocalConnection(params.getLocalUniprotDbName(),params.getLocalTaxonomyDbName());
			LOGGER.info("Using local UniProt connection to retrieve UniProtKB data. Local databases: "+params.getLocalUniprotDbName()+" and "+params.getLocalTaxonomyDbName());
		} else {
			this.useLocalUniprot = false;
			this.uniprotJapiConn = new UniProtConnection();
			LOGGER.info("Using remote UniProt JAPI connection to retrieve UniProtKB data");
		}
		
		for (String representativeChain:pdb.getAllRepChains()) {
						
			PdbChain chain = pdb.getChain(representativeChain);
			
			if (!chain.getSequence().isProtein()) {
				LOGGER.warn("Representative chain "+representativeChain+" does not seem to be a protein chain. Won't analyse it.");
				continue;
			}

			ChainEvolContext cec = new ChainEvolContext(this, chain.getSequenceMSEtoMET(), representativeChain, pdb.getPdbCode(), params);
			cec.setSeqIdenticalChainsStr(pdb.getSeqIdenticalGroupString(representativeChain));
			
			cecs.put(representativeChain, cec);
		}
		
	}
	
	public void addChainEvolContext(String representativeChain, ChainEvolContext cec) {
		this.cecs.put(representativeChain, cec);
	}
	
	/**
	 * Gets the ChainEvolContext corresponding to the given PDB chain code (can be 
	 * any chain code, representative or not)
	 * @param pdbChainCode
	 * @return
	 */
	public ChainEvolContext getChainEvolContext(String pdbChainCode) {
		return cecs.get(chain2repChain.get(pdbChainCode));
	}
	
	/**
	 * Gets the Collection of all ChainEvolContext (sorted alphabetically by representative pdb chain code) 
	 * @return
	 */
	public Collection<ChainEvolContext> getAllChainEvolContext() {
		return cecs.values();
	}
	
	public String getPdbName() {
		return pdbName;
	}
	
	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
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
	
	public boolean isUseLocalUniprot() {
		return useLocalUniprot;
	}
	
	public UniprotLocalConnection getUniProtLocalConnection() {
		return uniprotLocalConn;
	}
	
	public UniProtConnection getUniProtJapiConnection() {
		return uniprotJapiConn;
	}
	
	public void retrieveQueryData(CRKParams params) throws CRKException {
		params.getProgressLog().println("Finding query's uniprot mappings through SIFTS or blasting");
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			try {
				chainEvCont.retrieveQueryData(params);

			} catch (BlastException e) {
				throw new CRKException(e,"Couldn't run blast to retrieve query's uniprot mapping: "+e.getMessage(),true);
			} catch (IOException e) {
				throw new CRKException(e,"Problems while retrieving query data: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e,"Thread interrupted while running blast for retrieving query data: "+e.getMessage(),true);
			} catch (Exception e) { // for any kind of exceptions thrown while connecting through uniprot JAPI
				throw new CRKException(e,"Problems while retrieving query data through Uniprot JAPI. Is Uniprot server down?\n"+e.getMessage(),true);
			}
		}
		params.getProgressLog().println();
	}
	
	public void retrieveHomologs(CRKParams params) throws CRKException {
		
		// 1) we find homologs by blasting
		blastForHomologs(params);

		// 2) we remove the identicals to the query (removal base on blast id/coverage results, no further calculations needed)
		filterIdenticalsToQuery(params);
		
		// 3) we then retrieve the uniprot kb data: sequences, tax ids and taxons for all homologs above the given hard cutoffs thresholds
		// we won't need data for any other homolog
		retrieveHomologsData(params);
		
		// now that we have all data we don't need the connections anymore
		closeConnections();
		
		// 4) and now we apply the identity (soft/hard) cutoffs, which also does redundancy reduction in each iteration
		applyIdentityCutoff(params);

		// then we filter optionally for domain of life (needs the taxon data above)
		if (params.isFilterByDomain()) filterToSameDomainOfLife();

	}
	
	private void blastForHomologs(CRKParams params) throws CRKException {
		params.getProgressLog().println("Blasting for homologs");
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			
			File blastCacheFile = null;
			if (params.getBlastCacheDir()!=null) {
				blastCacheFile = new File(params.getBlastCacheDir(),chainEvCont.getQuery().getUniId()+".blast.xml"); 
			}
			try {
				chainEvCont.blastForHomologs(params, blastCacheFile);

			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (BlastException e) {
				throw new CRKException(e,"Couldn't run blast to retrieve homologs: "+e.getMessage() ,true);
			} catch (IOException e) {
				throw new CRKException(e,"Problem while blasting for sequence homologs: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e,"Thread interrupted while blasting for sequence homologs: "+e.getMessage(),true);
			}

		}
		params.getProgressLog().println();
	}
	
	private void retrieveHomologsData(CRKParams params) throws CRKException {
		params.getProgressLog().println("Retrieving UniprotKB data");
		params.getProgressLog().print("chains: ");
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
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (IOException e) {
				String errmsg = "Problem while retrieving homologs data through Uniprot JAPI";
				throw new CRKException(e, errmsg+": "+e.getMessage(),true);
			} catch (SQLException e) {
				throw new CRKException(e, "Problem while retrieving homologs data from Uniprot local database: "+e.getMessage(), true);
			} catch (Exception e) { // for any kind of exceptions thrown while connecting through uniprot JAPI
				throw new CRKException(e, "Problems while retrieving homologs data through Uniprot JAPI. Is Uniprot server down?\n"+e.getMessage(),true);
			}

		}		
		params.getProgressLog().println();
	}
	
	private void applyIdentityCutoff(CRKParams params) throws CRKException {
		
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
				throw new CRKException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			} catch (InterruptedException e) {
				throw new CRKException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
			} catch (BlastException e) {
				throw new CRKException(e, "Problems while running blastclust for redundancy reduction of homologs: "+e.getMessage(), true);
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
	
	private void filterIdenticalsToQuery(CRKParams params) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			// remove hits totally identical to query (based on the blast id and query coverage numbers)
			chainEvCont.removeIdenticalToQuery(params.getMinQueryCovForIdenticalsRemoval());
		}
	}
	
	public void align(CRKParams params) throws CRKException {
		params.getProgressLog().println("Aligning protein sequences with t_coffee");
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			try {
				chainEvCont.align(params);
			} catch (TcoffeeException e) {
				throw new CRKException(e, "Couldn't run t_coffee to align protein sequences: "+e.getMessage(), true);
			} catch (IOException e) {
				throw new CRKException(e, "Problems while running t_coffee to align protein sequences: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e, "Thread interrupted while running t_coffee to align protein sequences: "+e.getMessage(),true);
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Uniprot versions mismatch while trying to read cached alignment: "+e.getMessage(),true);
			}

		}
		params.getProgressLog().println();
	}
	
	public void writeSeqInfoToFiles(CRKParams params) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			File outFile = null;
			try {
				// writing homolog sequences to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".fa");
				chainEvCont.writeHomologSeqsToFile(outFile, params);

				// printing summary to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".log");
				PrintStream log = new PrintStream(outFile);
				chainEvCont.printSummary(log);
				log.close();
				// writing the alignment to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".aln");
				chainEvCont.writeAlignmentToFile(outFile);

			} catch(FileNotFoundException e){
				LOGGER.error("Couldn't write file "+outFile);
				LOGGER.error(e.getMessage());
			}
		}
	}
	
	public void computeEntropies(CRKParams params, PdbAsymUnit pdb) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			chainEvCont.computeEntropies(params.getReducedAlphabet());

			File outFile = null;
			try {
				// writing the conservation scores (entropies/kaks) log file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+CRKParams.ENTROPIES_FILE_SUFFIX);
				PrintStream conservScoLog = new PrintStream(outFile);
				chainEvCont.printConservationScores(conservScoLog, ScoringType.ENTROPY, pdb);
				conservScoLog.close();
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not write the scores log file "+outFile);
				LOGGER.error(e.getMessage());
			}
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
		int i = 0;
		for (ChainEvolContext chainEvCont:cecs.values()) {
			i++;
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}

			if (chainEvCont.getQuery().hasTaxons()) {
				if (i==1) {
					dol = chainEvCont.getQuery().getFirstTaxon();
				} else {
					if (!chainEvCont.getQuery().getFirstTaxon().equals(dol)) {
						LOGGER.warn("Different domain of lifes for different chains of the same PDB. " +
								"First chain is "+dol+" while chain "+chainEvCont.getRepresentativeChainCode()+" is "+chainEvCont.getQuery().getFirstTaxon());
					}
					dol = null;
				}				
			}
		}
		return dol;
	}
	
	public void closeConnections() {
		if (useLocalUniprot) {			
			this.uniprotLocalConn.close();
			LOGGER.info("Connection to local Uniprot database closed");
		}
		// there doesn't seem to be a way of closing the japi connection, we do nothing in that case
	}
}
