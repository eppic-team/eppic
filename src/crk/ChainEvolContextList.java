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

import owl.core.runners.TcoffeeException;
import owl.core.runners.blast.BlastException;
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
	
	public ChainEvolContextList(PdbAsymUnit pdb, CRKParams params) throws SQLException {
		this.cecs = new TreeMap<String, ChainEvolContext>();
		this.chain2repChain = pdb.getChain2repChainMap();
		this.pdbName = params.getJobName();
		
		for (String representativeChain:pdb.getAllRepChains()) {
						
			PdbChain chain = pdb.getChain(representativeChain);
			
			if (!chain.getSequence().isProtein()) {
				LOGGER.warn("Representative chain "+representativeChain+" does not seem to be a protein chain. Won't analyse it.");
				continue;
			}

			ChainEvolContext cec = new ChainEvolContext(chain.getSequenceMSEtoMET(), representativeChain, pdb.getPdbCode(), params);
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
	
	public void retrieveQueryData(CRKParams params) throws CRKException {
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
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
			
			File blastCacheFile = null;
			if (params.getBlastCacheDir()!=null) {
				blastCacheFile = new File(params.getBlastCacheDir(),chainEvCont.getQuery().getUniprotId()+".blast.xml"); 
			}
			try {
				chainEvCont.retrieveHomologs(params, blastCacheFile);
				LOGGER.info("Uniprot version used: "+chainEvCont.getUniprotVer());

				// the uniprot ver will be set only when at least one sequence has uniprot match
				// if not a single sequence has match then it will be null
				this.uniprotVer = chainEvCont.getUniprotVer();

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
	
	public void retrieveHomologsData(CRKParams params) throws CRKException {
		params.getProgressLog().print("chains: ");
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			params.getProgressLog().print(chainEvCont.getRepresentativeChainCode()+" ");
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
	
	public void applyIdentityCutoff(CRKParams params) {
		
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
			String queryFirstTaxon = chainEvCont.getQuery().getFirstTaxon();
			if (queryFirstTaxon.equals("Bacteria") || queryFirstTaxon.equals("Archaea")) {
				homSoftIdCutoff = params.getHomSoftIdCutoffBacteria(); 
				homHardIdCutoff = params.getHomHardIdCutoffBacteria();
			}
			chainEvCont.applyIdentityCutoff(homSoftIdCutoff, homHardIdCutoff, params.getHomIdStep(), queryCovCutoff, minNumSeqs);
		}
	}
	
	public void filter(CRKParams params) {
		for (ChainEvolContext chainEvCont:cecs.values()) {
			if (!chainEvCont.hasQueryMatch()) {
				// no query uniprot match, we do nothing with this sequence
				continue;
			}
			
			// filtering optionally for domain of life
			if (params.isFilterByDomain()) chainEvCont.filterToSameDomainOfLife();

			// remove redundancy
			chainEvCont.removeRedundancy();

			// skimming so that there's not too many sequences 
			chainEvCont.skimList(params.getMaxNumSeqs());

		}
	}
	
	public void align(CRKParams params) throws CRKException {
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
				chainEvCont.writeHomologSeqsToFile(outFile);

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
	
}
