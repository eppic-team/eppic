package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.connections.UniProtConnection;
import owl.core.connections.UniprotLocalConnection;
import owl.core.features.SiftsFeature;
import owl.core.runners.TcoffeeException;
import owl.core.runners.blast.BlastException;
import owl.core.runners.blast.BlastHit;
import owl.core.runners.blast.BlastHitList;
import owl.core.runners.blast.BlastRunner;
import owl.core.runners.blast.BlastXMLParser;
import owl.core.sequence.Sequence;
import owl.core.sequence.Homolog;
import owl.core.sequence.HomologList;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.sequence.UnirefEntry;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
import owl.core.structure.AminoAcid;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.util.Interval;

public class ChainEvolContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ChainEvolContext.class);

	// blast constants
	private static final String BLASTOUT_SUFFIX = "blast.out.xml";
	private static final String BLAST_BASENAME = "pdb2unimapping";
	private static final String FASTA_SUFFIX = ".fa";
	private static final int 	BLAST_OUTPUT_TYPE = 7;  // xml output
	private static final boolean BLAST_NO_FILTERING = true;
	private static final boolean DEBUG = false;
	
	
	// members 
	private String representativeChain;		// the pdb chain code of the representative chain
	private String pdbCode; 		 		// the pdb code (if no pdb code then Pdb.NO_PDB_CODE)
	private String sequence;
	private String pdbName;					// a name to identify the PDB, will be the pdbCode if a PDB entry or the file name without extension if from file
	private String seqIdenticalChainsStr;	// a string of the form A(B,C,D,E) containing all represented sequence identical chains
	
	private UnirefEntry query;							// the uniprot info (id,seq) corresponding to this chain's sequence
	private Interval queryInterv;						// the interval of the query uniprot reference sequence that is actually used in the blast search
	private boolean hasQueryMatch;						// whether we could find the query's uniprot match or not
	private PairwiseSequenceAlignment alnPdb2Uniprot; 	// the alignment between the pdb sequence and the uniprot sequence (query)
	
	private double idCutoff;
	private double queryCov;
	
	private HomologList homologs;	// the homologs of this chain's sequence
		
	private boolean searchWithFullUniprot;  // mode of searching, true=we search with full uniprot seq, false=we search with PDB matching part only
	
	private List<String> queryWarnings;
	
	private boolean useLocalUniprot;
	private transient UniProtConnection uniprotJapiConn;
	private transient UniprotLocalConnection uniprotLocalConn;
	
	private String uniprotVer;

	public ChainEvolContext(String sequence, String representativeChain, String pdbCode, CRKParams params) throws SQLException {
		this.pdbCode = pdbCode;
		this.pdbName = params.getJobName();
		this.sequence = sequence;
		this.representativeChain = representativeChain;
		this.hasQueryMatch = false;
		this.searchWithFullUniprot = true;
		this.queryWarnings = new ArrayList<String>();
		
		
		if (params.getLocalUniprotDbName()!=null) {
			this.useLocalUniprot = true;
			this.uniprotLocalConn = new UniprotLocalConnection(params.getLocalUniprotDbName(),params.getLocalTaxonomyDbName());
			LOGGER.info("Using local UniProt connection to retrieve UniProtKB data. Local databases: "+params.getLocalUniprotDbName()+" and "+params.getLocalTaxonomyDbName());
		} else {
			this.useLocalUniprot = false;
			this.uniprotJapiConn = new UniProtConnection();
			LOGGER.info("Using remote UniProt JAPI connection to retrieve UniProtKB data");
		}
	}
	
	/**
	 * Retrieves the Uniprot mapping corresponding to the query PDB sequence 
	 * @throws IOException
	 * @throws BlastException
	 * @throws InterruptedException
	 */
	public void retrieveQueryData(CRKParams params) throws IOException, BlastException, InterruptedException {
		
		String siftsLocation = params.getSiftsFile();
		boolean useSifts = params.isUseSifts();
		String blastBinDir = params.getBlastBinDir();
		String blastDbDir = params.getBlastDbDir();
		String blastDb = params.getBlastDb();
		int blastNumThreads = params.getNumThreads();
		double pdb2uniprotIdThreshold = params.getPdb2uniprotIdThreshold();
		double pdb2uniprotQcovThreshold = params.getPdb2uniprotQcovThreshold();
		boolean useUniparc = params.isUseUniparc();
		
		String queryUniprotId = null;
		query = null;
		
		if (sequence.length()<=CRKParams.PEPTIDE_LENGTH_CUTOFF) {
			queryWarnings.add("Chain is a peptide ("+sequence.length()+" residues)");
		}
		
		// two possible cases: 
		// 1) PDB code known and so SiftsFeatures can be taken from SiftsConnection (unless useSifts is set to false)
		Collection<SiftsFeature> mappings = null;
		if (!pdbCode.equals(PdbAsymUnit.NO_PDB_CODE)) {
			if (useSifts) {
				SiftsConnection siftsConn = new SiftsConnection(siftsLocation);
				try {
					mappings = siftsConn.getMappings(pdbCode, representativeChain);
					HashSet<String> uniqUniIds = new HashSet<String>();
					for (SiftsFeature sifts:mappings) {
						uniqUniIds.add(sifts.getUniprotId());
					}
					if (uniqUniIds.size()>1) {
						LOGGER.error("More than one uniprot SIFTS mapping for the query PDB code "+pdbCode+" chain "+representativeChain);
						String msg = ("Uniprot IDs are: ");
						for (String uniId:uniqUniIds){
							msg+=(uniId+" ");
						}
						LOGGER.error(msg);
						LOGGER.error("Check if the PDB entry is biologically reasonable (likely to be an engineered entry). Won't do evolution analysis on this chain.");

						String warning = "More than one UniProt id correspond to chain "+representativeChain+": ";
						for (String uniId:uniqUniIds){
							warning+=(uniId+" ");
						}
						warning+=". This is most likely a chimeric chain";
						queryWarnings.add(warning);

						// we continue with a null query, i.e. we treat it in the same way as no match
						queryUniprotId = null;
					} else {
						queryUniprotId = uniqUniIds.iterator().next();
					}

				} catch (NoMatchFoundException e) {
					LOGGER.warn("No SIFTS mapping could be found for "+pdbCode+representativeChain);
					LOGGER.info("Trying blasting to find one.");
					queryUniprotId = findUniprotMapping(blastBinDir, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold, useUniparc);  
				}
			} else {
				LOGGER.info("Using SIFTS feature is turned off. Will blast to find query-to-uniprot mapping");
				queryUniprotId = findUniprotMapping(blastBinDir, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold, useUniparc);
			}
		// 2) PDB code not known and so SiftsFeatures have to be found by blasting, aligning etc.
		} else {
			LOGGER.info("No PDB code available. Can't use SIFTS. Blasting to find the query's Uniprot mapping.");
			queryUniprotId = findUniprotMapping(blastBinDir, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold, useUniparc);
		}
		
		if (queryUniprotId!=null) hasQueryMatch = true;
		
		if (hasQueryMatch) {

			LOGGER.info("Uniprot id for the query "+pdbCode+representativeChain+": "+queryUniprotId);

			// once we have the identifier we get the data from uniprot
			try {
				if (useLocalUniprot) {
					query = uniprotLocalConn.getUnirefEntry(queryUniprotId);
				} else {
					query = uniprotJapiConn.getUnirefEntry(queryUniprotId);
				}

				// and finally we align the 2 sequences (in case of mapping from SIFTS we rather do this than trusting the SIFTS alignment info)
				alnPdb2Uniprot = new PairwiseSequenceAlignment(sequence, query.getSequence(), pdbCode+representativeChain, query.getUniId());
				LOGGER.info("The PDB SEQRES to Uniprot alignmnent:\n"+getQueryPdbToUniprotAlnString());
				LOGGER.info("Query ("+pdbCode+representativeChain+") length: "+sequence.length());
				LOGGER.info("Uniprot ("+query.getUniId()+") length: "+query.getLength());
				LOGGER.info("Alignment length: "+alnPdb2Uniprot.getLength());
			} catch (PairwiseSequenceAlignmentException e1) {
				LOGGER.fatal("Problem aligning PDB sequence "+pdbCode+representativeChain+" to its Uniprot match "+query.getUniId());
				LOGGER.fatal(e1.getMessage());
				LOGGER.fatal("Can't continue");
				System.exit(1);
			}  catch (NoMatchFoundException e) {
				LOGGER.error("Couldn't find Uniprot id "+queryUniprotId+" through Uniprot JAPI. Obsolete?");
				query = null;
				hasQueryMatch = false;
			} catch (SQLException e) {
				LOGGER.error("Could not retrieve the uniprot data for uniprot id "+queryUniprotId+" from local database: "+e.getMessage());
				query = null;
				hasQueryMatch = false;
			}
		}
		// TODO anyway we should do also a sanity check of our alignment against the SIFTS mappings (if we have them) 
		//if (mappings!=null) {
		// compare mappings to the alnPdb2Uniprot	
		//}
	}
	
	public void retrieveHomologs(CRKParams params,File blastCache) 
	throws IOException, BlastException, UniprotVerMisMatchException, InterruptedException {
		
		
		String blastBinDir = params.getBlastBinDir();
		String blastDbDir = params.getBlastDbDir();
		String blastDb = params.getBlastDb();
		int blastNumThreads = params.getNumThreads(); 
		int maxNumSeqs = params.getMaxNumSeqs();
		HomologsSearchMode searchMode = params.getHomologsSearchMode();
		double pdb2uniprotMaxScovForLocal = params.getPdb2uniprotMaxScovForLocal();
		boolean useUniparc = params.isUseUniparc();
		
		queryInterv = new Interval(1,query.getLength());
		
		LOGGER.info("Homologs search mode: "+searchMode.getName());
		if (searchMode==HomologsSearchMode.GLOBAL) {
			searchWithFullUniprot = true;
		} else if (searchMode==HomologsSearchMode.LOCAL) {
			searchWithFullUniprot = false;
			queryInterv = new Interval(alnPdb2Uniprot.getFirstMatchingPos(false)+1,alnPdb2Uniprot.getLastMatchingPos(false)+1);
		} else {
			if (sequence.length()<pdb2uniprotMaxScovForLocal*query.getLength()) {
				queryInterv = new Interval(alnPdb2Uniprot.getFirstMatchingPos(false)+1,alnPdb2Uniprot.getLastMatchingPos(false)+1);
				searchWithFullUniprot = false;
				LOGGER.info("PDB sequence for chain "+representativeChain+" covers only "+
						String.format("%4.2f",(double)sequence.length()/(double)query.getLength())+
						" of Uniprot "+query.getUniId()+
						" (cutoff is "+String.format("%4.2f", pdb2uniprotMaxScovForLocal)+")");
			} else {
				searchWithFullUniprot = true;
				LOGGER.info("PDB sequence for chain "+representativeChain+" coverage of Uniprot reference "+
						query.getUniId()+" is "+String.format("%4.2f",(double)sequence.length()/(double)query.getLength())+
						" (thus above cutoff "+String.format("%4.2f", pdb2uniprotMaxScovForLocal)+" for local mode)");
			}
		}
		if (searchWithFullUniprot) LOGGER.info("Using full Uniprot sequence "+query.getUniId()+" for blast search");
		else LOGGER.info("Using Uniprot "+query.getUniId()+" subsequence "+queryInterv.beg+"-"+queryInterv.end+" for blast search");
		
		homologs = new HomologList(query,queryInterv);
		
		if (useUniparc) LOGGER.info("Uniparc hits will be used");
		else LOGGER.info("Uniparc hits won't be used");
		homologs.setUseUniparc(useUniparc);
		
		homologs.searchWithBlast(blastBinDir, blastDbDir, blastDb, blastNumThreads, maxNumSeqs, blastCache);
		LOGGER.info(homologs.getSizeFullList()+" homologs found by blast");
		 
	}
	
	public void filterToSameDomainOfLife() {
		if (query.hasTaxons()) {
			LOGGER.info("Filtering to domain: "+query.getFirstTaxon());
			homologs.filterToSameDomainOfLife();
			LOGGER.info(homologs.getSizeFilteredSubset()+" homologs after filtering to "+query.getFirstTaxon());
		} else {
			LOGGER.info("Taxons of query are unknown, can't filter to same domain of life.");
		}
	}
	
	public void removeRedundancy() {
		homologs.removeRedundancy();
	}
	
	public void removeIdenticalToQuery(double minQueryCov) {
		homologs.removeIdenticalToQuery(minQueryCov);
	}
	
	public void skimList(int maxDesiredHomologs) {
		homologs.skimList(maxDesiredHomologs);
	}
	
	/**
	 * Retrieves the Uniprot data and metadata
	 * @throws IOException
	 * @throws UniprotVerMisMatchException
	 * @throws SQLException 
	 */
	public void retrieveHomologsData() throws IOException, UniprotVerMisMatchException, SQLException {
		if (useLocalUniprot) {
			homologs.retrieveUniprotKBData(uniprotLocalConn);
		} else {
			homologs.retrieveUniprotKBData(uniprotJapiConn);
			homologs.retrieveUniparcData(null);
		}
	}
	
	public void applyIdentityCutoff(double homSoftIdCutoff, double homHardIdCutoff, double homIdStep, double queryCovCutoff, int minNumSeqs) {
		this.queryCov = queryCovCutoff;
		// applying identity cutoff
		this.idCutoff = homSoftIdCutoff;
		while (true) { 
			homologs.filterToMinIdAndCoverage(idCutoff, queryCovCutoff);
			if (homologs.getSizeFilteredSubset()>=minNumSeqs) break;
			LOGGER.info("Tried "+String.format("%4.2f",idCutoff)+" identity cutoff, only "+homologs.getSizeFilteredSubset()+" homologs found ("+minNumSeqs+" required)");
			// instead of putting the condition in the while above, we need it here so that in case that the hard cutoff
			// is reached and still not enough homologs the last really tried idCutoff is the one that stays stored and logged in next line
			// the 0.001 just to be sure we really reach the hard cutoff (there were problems with rounding)
			if (Math.abs(idCutoff-homHardIdCutoff)<0.001) break; 
			idCutoff -= homIdStep;
		}
		LOGGER.info(homologs.getSizeFilteredSubset()+" homologs after applying "+String.format("%4.2f",idCutoff)+" identity cutoff and "+String.format("%4.2f",queryCovCutoff)+" query coverage cutoff");
	}

	public void align(CRKParams params) throws IOException, TcoffeeException, InterruptedException, UniprotVerMisMatchException { 
		File tcoffeeBin = params.getTcoffeeBin();
		boolean tcoffeeVeryFastMode = params.isUseTcoffeeVeryFastMode();
		int nThreads = params.getNumThreads();
		
		LOGGER.info("Alignment mode for chain "+getRepresentativeChainCode()+" is: "+params.getAlignmentMode().getName());
		boolean useHspsOnly = false;
		if (params.getAlignmentMode()==AlignmentMode.AUTO) {
			if (isSearchWithFullUniprot()) {
				useHspsOnly = false;
				LOGGER.info("Full sequences will be used for tcoffee alignment (because search mode is GLOBAL)");
			}
			else {
				useHspsOnly = true;
				LOGGER.info("Only matching HSP subsequences will be used for tcoffee alignment (because search mode is LOCAL)");
			}
		} else if (params.getAlignmentMode()==AlignmentMode.FULLLENGTH) {
			useHspsOnly = false;
		} else if (params.getAlignmentMode()==AlignmentMode.HSPSONLY) {
			useHspsOnly = true;
		}
		
		// 3) alignment of the protein sequences using tcoffee
		File alnCacheFile = null;
		// beware we only try to use cache file if we are in default mode: uniparc=true, filter by domain=false, veryFastMode=false
		if (params.getBlastCacheDir()!=null && params.isUseUniparc() && !params.isFilterByDomain() && !tcoffeeVeryFastMode) {
			String intervStr = "";
			if (!isSearchWithFullUniprot()) {
				intervStr = "."+getQueryInterval().beg+"-"+getQueryInterval().end;
			}
			// a cache file will look like  Q9UKX7.i60.c85.m60.1-109.full.aln (that corresponds to 3tj3C) 
			//                              P52294.i60.c85.m60.full.aln       (that corresponds to 3tj3A)
			// i=identity threshold used, c=query coverage, m=max num seqs, optional two last numbers are the subinterval
			alnCacheFile = new File(params.getBlastCacheDir(),getQuery().getUniId()+
					".i"+String.format("%2.0f", idCutoff*100.0)+
					".c"+String.format("%2.0f", queryCov*100.0)+
					".m"+params.getMaxNumSeqs()+
					intervStr+
					"."+(useHspsOnly?"hsp":"full")+
					".aln"); 
		}
		
		homologs.computeTcoffeeAlignment(tcoffeeBin, tcoffeeVeryFastMode, nThreads, useHspsOnly, alnCacheFile);
	}
	
	public void writeAlignmentToFile(File alnFile) throws FileNotFoundException {
		homologs.writeAlignmentToFile(alnFile); 
	}
	
	public void writeHomologSeqsToFile(File outFile, CRKParams params) throws FileNotFoundException {
		boolean useHspsOnly = false;
		if (params.getAlignmentMode()==AlignmentMode.AUTO) {
			if (isSearchWithFullUniprot()) useHspsOnly = false;
			else useHspsOnly = true;
		} else if (params.getAlignmentMode()==AlignmentMode.FULLLENGTH) {
			useHspsOnly = false;
		} else if (params.getAlignmentMode()==AlignmentMode.HSPSONLY) {
			useHspsOnly = true;
		}
		homologs.writeToFasta(outFile, false, useHspsOnly);
	}
	
	public MultipleSequenceAlignment getAlignment() {
		return homologs.getAlignment();
	}
	
	/**
	 * Returns the query's PDB SEQRES to uniprot alignment as a nicely formatted
	 * aligmnent string in several lines with a middle line of matching characters,
	 * e.g. 
	 * 1abcA   AAAA--BCDEFGICCC
	 *         ||.|  ||.|||:|||
	 * QABCD1  AABALCBCJEFGLCCC
	 * @return
	 */
	public String getQueryPdbToUniprotAlnString() {
		return alnPdb2Uniprot.getFormattedAlignmentString();
	}
	
	public PairwiseSequenceAlignment getPdb2uniprotAln() {
		return alnPdb2Uniprot;
	}
	
	/**
	 * Returns a list of the conservation scores values, i.e. entropy or ka/ks ratios 
	 * depending on scoType.
	 * The sequence to which the list refers is the query Uniprot/CDS translated sequence, 
	 * not the PDB SEQRES.   
	 * @param scoType
	 * @return
	 */
	public List<Double> getConservationScores(ScoringType scoType) {
		if (scoType.equals(ScoringType.ENTROPY)) {
			return homologs.getEntropies();
		}
		throw new IllegalArgumentException("Given scoring type "+scoType+" is not recognized ");

	}
	
	/**
	 * Compute the sequence entropies for all reference sequence (uniprot) positions
	 * @param reducedAlphabet
	 */
	public void computeEntropies(int reducedAlphabet) {
		homologs.computeEntropies(reducedAlphabet);
	}
	
	/**
	 * Calculates the evolutionary score for the given list of residues by summing up evolutionary
	 * scores per residue and averaging (optionally weighted by BSA)
	 * @param residues
	 * @param scoType whether the evolutionary score should be entropy or Ka/Ks
	 * @param weighted whether the scores should be weighted by BSA of each residue
	 * @return
	 */
	public double calcScoreForResidueSet(List<Residue> residues, ScoringType scoType, boolean weighted) {
		double totalScore = 0.0;
		double totalWeight = 0.0;
		List<Double> conservScores = getConservationScores(scoType);
		for (Residue res:residues){
			int resSer = res.getSerial(); 

			int queryPos = -2;
			if (scoType==ScoringType.ENTROPY) {
				queryPos = getQueryUniprotPosForPDBPos(resSer); 
			} 
			if (queryPos!=-1) {   
				double weight = 1.0;
				if (weighted) {
					weight = res.getBsa();
				}
				totalScore += weight*(conservScores.get(queryPos));
				totalWeight += weight;
			} else {

			}

		}
		return totalScore/totalWeight;
	}
	
	/**
	 * Gets the size of the reduced alphabet used for calculating entropies
	 * @return the size of the reduced alphabet or 0 if no entropies have been computed
	 */
	public int getReducedAlphabet() {
		return homologs.getReducedAlphabet();
	}
	
	/**
	 * Prints a summary of the query and uniprot/cds identifiers and homologs with 
	 * their uniprot/cds identifiers
	 * @param ps
	 */
	public void printSummary(PrintStream ps) {
		ps.println("Query: "+pdbName+representativeChain);
		ps.println("Uniprot id for query:");
		ps.print(this.query.getUniId());
		if (this.query.hasTaxons()) ps.println("\t"+this.query.getFirstTaxon()+"\t"+this.query.getLastTaxon());
		else ps.println("\tunknown taxonomy");
		ps.println();
		
		ps.println("Uniprot version: "+getUniprotVer());
		ps.println("Homologs: "+homologs.getSizeFilteredSubset()+" with minimum "+String.format("%4.2f",homologs.getIdCutoff())+" identity and "+
				String.format("%4.2f",homologs.getQCovCutoff())+" query coverage");
		for (Homolog hom:homologs.getFilteredSubset()) {
			ps.printf("%-13s",hom.getIdentifier());
			ps.printf("\t%5.1f",hom.getPercentIdentity());
			ps.printf("\t%5.1f",hom.getBlastHit().getQueryCoverage()*100.0);
			if (hom.getUnirefEntry().hasTaxons()) ps.print("\t"+hom.getUnirefEntry().getFirstTaxon()+"\t"+hom.getUnirefEntry().getLastTaxon());
			ps.println();
		}
	}
	
	public void printConservationScores(PrintStream ps, ScoringType scoType, PdbAsymUnit pdb) {
		if (scoType.equals(ScoringType.ENTROPY)) {
			ps.println("# Entropies for all query sequence positions (reference UniProt: "+query.getUniId()+") based on a "+homologs.getReducedAlphabet()+" letters alphabet.");
			ps.println("# seqres\tpdb\tuniprot\tuniprot_res\tentropy");
		} 
		PdbChain chain = pdb.getChain(representativeChain);
		List<Double> conservationScores = getConservationScores(scoType);		
		for (int i=0;i<conservationScores.size();i++) {
			int resser = 0;
			if (scoType.equals(ScoringType.ENTROPY)) {
				resser = getPDBPosForQueryUniprotPos(i+queryInterv.beg-1);
			} 			
			String pdbresser = chain.getPdbResSerFromResSer(resser);
			ps.printf("%4d\t%4s\t%4d\t%3s\t%5.2f\n",
					resser, 
					pdbresser, 
					i+queryInterv.beg, 
					AminoAcid.one2three(query.getSequence().charAt(i+queryInterv.beg-1)), 
					conservationScores.get(i));
		}
	}
	
	public int getNumHomologs() {
		if (homologs==null) return 0;
		return homologs.getSizeFilteredSubset();
	}
	
	public String getRepresentativeChainCode() {
		return representativeChain;
	}
	
	public String getPdbSequence() {
		return sequence;
	}
	
	/**
	 * Tells whether a given position of the reference PDB sequence (starting at 1)
	 * is reliable with respect to:
	 *  the PDB to uniprot matching (mismatches occur mostly because of engineered residues in PDB)
	 * @param resser the (cif) PDB residue serial corresponding to the SEQRES record, starting at 1
	 * @return
	 */
	public boolean isPdbSeqPositionMatchingUniprot(int resser) {
		// we check if the PDB to uniprot mapping is reliable (identity) at the resser position
		if (!alnPdb2Uniprot.isMatchingTo2(resser-1)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Given a residue serial of the reference PDB SEQRES sequence (starting at 1), returns
	 * its corresponding Uniprot's sequence index within subinterval (starting at 0)
	 * @param resser
	 * @return the mapped uniprot sequence position or -1 if it maps to a gap
	 */
	public int getQueryUniprotPosForPDBPos(int resser) {
		int uniprotPos = alnPdb2Uniprot.getMapping1To2(resser-1);
		if (uniprotPos==-1) return -1; // maps to a gap in alignment: return -1
		// the position in the subsequence that was used for blasting
		int pos = uniprotPos - (queryInterv.beg-1); 
		// check if it is out of the subsequence: can happen when his tags or other engineered residues at termini 
		// in n-terminal (pos<0), e.g. 3n1e
		// or c-terminal (pos>=subsequence length) e.g. 2eyi
		if (pos<0 || pos>=queryInterv.getLength()) return -1;  
		return pos;
	}
	
	/**
	 * Given a sequence index of the query Uniprot sequence (starting at 0), returns its
	 * corresponding PDB SEQRES position (starting at 1)
	 * @param queryPos
	 * @return the mapped PDB SEQRES sequence position or -1 if it maps to a gap
	 */
	public int getPDBPosForQueryUniprotPos(int queryPos) {
		return alnPdb2Uniprot.getMapping2To1(queryPos)+1;
	}
	
	private String findUniprotMapping(String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads, double pdb2uniprotIdThreshold, double pdb2uniprotQcovThreshold, boolean useUniparc) 
			throws IOException, BlastException, InterruptedException {
		// for too short sequence it doesn't make sense to blast
		if (this.sequence.length()<CRKParams.MIN_SEQ_LENGTH_FOR_BLASTING) {
			LOGGER.info("Chain "+representativeChain+" too short for blasting");
			// query warnings for peptides (with a higher cutoff than this) are already logged before, see above
			return null;
		}
		
		File outBlast = File.createTempFile(BLAST_BASENAME,BLASTOUT_SUFFIX);
		File inputSeqFile = File.createTempFile(BLAST_BASENAME,FASTA_SUFFIX);
		if (!DEBUG) {
			outBlast.deleteOnExit();
			inputSeqFile.deleteOnExit();
		}
		
		new Sequence(pdbName,this.sequence).writeToFastaFile(inputSeqFile);

		BlastRunner blastRunner = new BlastRunner(blastBinDir, blastDbDir);
		blastRunner.runBlastp(inputSeqFile, blastDb, outBlast, BLAST_OUTPUT_TYPE, BLAST_NO_FILTERING, blastNumThreads, 500);

		BlastHitList blastList = null;
		try {
			BlastXMLParser blastParser = new BlastXMLParser(outBlast);
			blastList = blastParser.getHits();
		} catch (SAXException e) {
			// if this happens it means that blast doesn't format correctly its XML, i.e. has a bug
			LOGGER.fatal("Unexpected error: "+e.getMessage());
			System.exit(1);
		}

		String uniprotMapping = null;

		if (!blastList.isEmpty()) {
			BlastHit best = null;
			blastList.sort();
			for (int i=0;i<blastList.size();i++) {
				best = blastList.get(i);
				if (isHitUniprot(best)) break;
				if (i==blastList.size()-1) { // if not a single hit is uniprot then we get to here and we have to catch it
					LOGGER.error("No UniProt match could be found for the query "+pdbName+representativeChain+
							": no blast hit was a UniProt match (total "+blastList.size()+" blast hits)");
					queryWarnings.add("Blast didn't find a UniProt match for the chain. All blast hits were non-uniprot matches (total "+blastList.size()+" blast hits).");
					return null;
				}
			}
			if ((best.getTotalPercentIdentity()/100.0)>pdb2uniprotIdThreshold && best.getQueryCoverage()>pdb2uniprotQcovThreshold) {
				uniprotMapping = getDeflineAccession(best);
				LOGGER.info("Blast found UniProt id "+uniprotMapping+" as best hit with "+
						String.format("%5.2f%% id and %4.2f coverage",best.getTotalPercentIdentity(),best.getQueryCoverage()));
			} else {
				LOGGER.error("No UniProt match could be found for the query "+pdbName+representativeChain+" within cutoffs "+
						String.format("%5.2f%% id and %4.2f coverage",pdb2uniprotIdThreshold,pdb2uniprotQcovThreshold));
				LOGGER.error("Best match was "+best.getSubjectId()+", with "+
						String.format("%5.2f%% id and %4.2f coverage",best.getTotalPercentIdentity(),best.getQueryCoverage()));
				LOGGER.error("Alignment: ");
				LOGGER.error(best.getMaxScoringHsp().getAlignment().getFastaString(null, true));
				queryWarnings.add("Blast didn't find a UniProt match for the chain. Best match was "+getDeflineAccession(best)+", with "+
						String.format("%5.2f%% id and %4.2f coverage",best.getTotalPercentIdentity(),best.getQueryCoverage()));
			}			
		} else {
			LOGGER.error("No UniProt match could be found for the query "+pdbName+representativeChain+". Blast returned no hits.");
			queryWarnings.add("Blast didn't find a UniProt match for the chain (no hits returned by blast)");
		}

		return uniprotMapping;
	}

	/**
	 * Gets the uniprot id, uniprot isoform id or uniparc id given a blast hit
	 * @param hit
	 * @return the id or null if the defline doesn't match the regexes for uniprot, uniprot isoform or uniparc ids
	 */
	private String getDeflineAccession(BlastHit hit) {
		String sid = hit.getSubjectId();
		Matcher m = Sequence.DEFLINE_PRIM_ACCESSION_REGEX.matcher(sid);
		if (m.matches()) {
			return m.group(1); 
		} else {
			Matcher m2 = Sequence.DEFLINE_PRIM_ACCESSION_UNIREF_REGEX.matcher(sid);
			if (m2.matches()) {
				return m2.group(1);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Tells whether given BlastHit's defline matches a uniprot id
	 * @param hit
	 * @return
	 */
	private boolean isHitUniprot(BlastHit hit) {
		String sid = hit.getSubjectId();
		Matcher m = Sequence.DEFLINE_PRIM_ACCESSION_REGEX.matcher(sid);
		if (m.matches()) {
			return true;
		} else {
			Matcher m2 = Sequence.DEFLINE_PRIM_ACCESSION_UNIREF_REGEX.matcher(sid);
			if (m2.matches()) {
				String uniId = m2.group(1);
				if (uniId.contains("-")) {
					LOGGER.warn("Blast hit "+uniId+" is a Uniprot isoform id. Skipping it");
					return false;
				} else if (uniId.startsWith("UPI")) {
					LOGGER.warn("Blast hit "+uniId+" is a Uniparc id. Skipping it");
					return false;
				} else {
					return true;
				}
			} else {
				LOGGER.warn("Could not find Uniprot id in blast subject id "+sid+". Skipping it");
				return false;
			}
		}
	}
	
	/**
	 * Gets the PDB identifier: a PDB code if query was a PDB entry or a PDB file name. 
	 * @return
	 */
	public String getPdbName() {
		return this.pdbName;
	}
	
	public String getUniprotVer() {
		return uniprotVer;
	}
	
	public void setUniprotVer(String uniprotVer) {
		this.uniprotVer = uniprotVer;
	}
	
	/**
	 * Sets the sequence-identical chains string for this ChainEvolContext
	 * @param seqIdenticalChainsStr
	 */
	public void setSeqIdenticalChainsStr(String seqIdenticalChainsStr) {
		this.seqIdenticalChainsStr = seqIdenticalChainsStr;
	}
	
	/**
	 * Returns the sequence-identical chains string for this ChainEvolContext
	 * @return
	 */
	public String getSeqIndenticalChainStr() {
		return seqIdenticalChainsStr;
	}
	
	/**
	 * The uniprot match for the query, use {@link #hasQueryMatch()} to check whether this can be called. 
	 * @return
	 */
	public UnirefEntry getQuery() {
		return query;
	}
	
	/**
	 * The interval in the Uniprot match (query) that is considered for the blast search
	 * @return
	 */
	public Interval getQueryInterval() {
		return queryInterv;
	}
	
	/**
	 * Whether the query's uniprot match could be found or not.
	 * @return
	 */
	public boolean hasQueryMatch() {
		return hasQueryMatch;
	}
	
	/**
	 * Whether the search for homologs is based on full Uniprot or on a sub-interval, 
	 * get the interval with {@link #getQueryInterval()} 
	 * @return
	 */
	public boolean isSearchWithFullUniprot() {
		return searchWithFullUniprot;
	}
	
	/**
	 * Returns the actual identity cut-off applied after calling {@link #applyIdentityCutoff(double, double, double, double, int)}
	 * @return
	 */
	public double getIdCutoff() {
		return idCutoff;
	}
	
	/**
	 * Returns a list of strings containing warnings related to the query sequence matching
	 * @return
	 */
	public List<String> getQueryWarnings() {
		return queryWarnings;
	}
}
