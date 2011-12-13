package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.features.SiftsFeature;
import owl.core.runners.TcoffeeException;
import owl.core.runners.blast.BlastException;
import owl.core.runners.blast.BlastHit;
import owl.core.runners.blast.BlastHitList;
import owl.core.runners.blast.BlastRunner;
import owl.core.runners.blast.BlastXMLParser;
//import owl.core.sequence.ProteinToCDSMatch;
import owl.core.sequence.Sequence;
import owl.core.sequence.UniprotEntry;
import owl.core.sequence.Homolog;
import owl.core.sequence.HomologList;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
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
	
	private UniprotEntry query;							// the uniprot id, seq, cds corresponding to this chain's sequence
	private Interval queryInterv;
	private boolean hasQueryMatch;						// whether we could find the query's uniprot match or not
	private PairwiseSequenceAlignment alnPdb2Uniprot; 	// the alignment between the pdb sequence and the uniprot sequence (query)
	
	private HomologList homologs;	// the homologs of this chain's sequence
		
	private boolean searchWithFullUniprot;  // mode of searching, true=we search with full uniprot seq, false=we search with PDB matching part only

	public ChainEvolContext(String sequence, String representativeChain, String pdbCode, String pdbName) {
		this.pdbCode = pdbCode;
		this.pdbName = pdbName;
		this.sequence = sequence;
		this.representativeChain = representativeChain;
		this.hasQueryMatch = false;
		this.searchWithFullUniprot = true;
	}
	
	/**
	 * Retrieves the Uniprot mapping corresponding to the query PDB sequence 
	 * @param siftsLocation file or URL of the SIFTS PDB to Uniprot mapping table
	 * @param blastBinDir
	 * @param blastDbDir
	 * @param blastDb
	 * @param blastNumThreads
	 * @param retrieveCDS whether to retrieve CDSs or not
	 * @throws IOException
	 * @throws BlastException
	 * @throws InterruptedException
	 */
	public void retrieveQueryData(String siftsLocation, String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads, double pdb2uniprotIdThreshold, double pdb2uniprotQcovThreshold) 
	throws IOException, BlastException, InterruptedException {
		
		// two possible cases: 
		// 1) PDB code known and so SiftsFeatures can be taken from SiftsConnection
		Collection<SiftsFeature> mappings = null;
		if (!pdbCode.equals(PdbAsymUnit.NO_PDB_CODE)) {
			SiftsConnection siftsConn = new SiftsConnection(siftsLocation);
			try {
				mappings = siftsConn.getMappings(pdbCode, representativeChain);
				HashSet<String> uniqUniIds = new HashSet<String>();
				for (SiftsFeature sifts:mappings) {
					uniqUniIds.add(sifts.getUniprotId());
				}
				if (uniqUniIds.size()>1) {
					LOGGER.error("More than one uniprot SIFTS mapping for the query PDB code "+pdbCode);
					String msg = ("Uniprot IDs are: ");
					for (String uniId:uniqUniIds){
						msg+=(uniId+" ");
					}
					LOGGER.error(msg);
					LOGGER.error("Check if the PDB entry is biologically reasonable (likely to be an engineered entry). Won't do evolution analysis on this chain.");
					// we continue with a null query, i.e. we treat it in the same way as no match
					query = null;
				} else {
					query = new UniprotEntry(uniqUniIds.iterator().next());
				}

			} catch (NoMatchFoundException e) {
				LOGGER.warn("No SIFTS mapping could be found for "+pdbCode+representativeChain);
				LOGGER.info("Trying blasting to find one.");
				query = findUniprotMapping(blastBinDir, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold);
			}
		// 2) PDB code not known and so SiftsFeatures have to be found by blasting, aligning etc.
		} else {
			LOGGER.info("No PDB code available. Can't use SIFTS. Blasting to find the query's Uniprot mapping.");
			query = findUniprotMapping(blastBinDir, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold);
		}
		
		if (query!=null) hasQueryMatch = true;
		
		if (hasQueryMatch) {

			LOGGER.info("Uniprot id for the query "+pdbCode+representativeChain+": "+query.getUniId());

			// once we have the identifier we get the data from uniprot
			try {
				query.retrieveUniprotKBData();
			} catch (NoMatchFoundException e) {
				LOGGER.error("Couldn't find Uniprot id "+query.getUniId()+" through Uniprot JAPI. Obsolete?");
				System.exit(1);
			}

			// and finally we align the 2 sequences (in case of mapping from SIFTS we rather do this than trusting the SIFTS alignment info)
			try {
				alnPdb2Uniprot = new PairwiseSequenceAlignment(sequence, query.getUniprotSeq().getSeq(), pdbCode+representativeChain, query.getUniprotSeq().getName());
				LOGGER.info("The PDB SEQRES to Uniprot alignmnent:\n"+getQueryPdbToUniprotAlnString());
				LOGGER.info("Query ("+pdbCode+representativeChain+") length: "+sequence.length());
				LOGGER.info("Uniprot ("+query.getUniId()+") length: "+query.getLength());
				LOGGER.info("Alignment length: "+alnPdb2Uniprot.getLength());
			} catch (PairwiseSequenceAlignmentException e1) {
				LOGGER.fatal("Problem aligning PDB sequence "+pdbCode+representativeChain+" to its Uniprot match "+query.getUniId());
				LOGGER.fatal(e1.getMessage());
				LOGGER.fatal("Can't continue");
				System.exit(1);
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
		double homSoftIdCutoff = params.getHomSoftIdCutoff();
		double homHardIdCutoff = params.getHomHardIdCutoff();
		double homIdStep = params.getHomIdStep();
		double queryCovCutoff = params.getQueryCoverageCutoff();
		int maxNumSeqs = params.getMaxNumSeqs();
		HomologsSearchMode searchMode = params.getHomologsSearchMode();
		double pdb2uniprotMaxScovForLocal = params.getPdb2uniprotMaxScovForLocal();
		int minHomologsCutoff = params.getMinHomologsCutoff();
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
				LOGGER.info("PDB sequence covers only "+String.format("%4.2f",(double)sequence.length()/(double)query.getLength())+
						" of Uniprot "+query.getUniId()+
						" (cutoff is "+String.format("%4.2f", pdb2uniprotMaxScovForLocal)+")");
			} else {
				searchWithFullUniprot = true;
			}
		}
		if (searchWithFullUniprot) LOGGER.info("Using full Uniprot sequence for blast search");
		else LOGGER.info("Using Uniprot subsequence "+queryInterv.beg+"-"+queryInterv.end+" for blast search");
		
		homologs = new HomologList(query,queryInterv);
		
		if (useUniparc) LOGGER.info("Uniparc hits will be used");
		else LOGGER.info("Uniparc hits won't be used");
		homologs.setUseUniparc(useUniparc);
		
		homologs.searchWithBlast(blastBinDir, blastDbDir, blastDb, blastNumThreads, maxNumSeqs, blastCache);
		LOGGER.info(homologs.getSizeFullList()+" homologs found by blast");
		
		applyIdentityCutoff(homSoftIdCutoff, homHardIdCutoff, homIdStep, queryCovCutoff, minHomologsCutoff);
		 
	}
	
	public void filterToSameDomainOfLife() {
		LOGGER.info("Filtering to domain: "+query.getFirstTaxon());
		homologs.filterToSameDomainOfLife();
		LOGGER.info(homologs.getSizeFilteredSubset()+" homologs after filtering to "+query.getFirstTaxon());
	}
	
	public void removeRedundancy() {
		homologs.removeRedundancy();
	}
	
	public void skimList(int maxDesiredHomologs) {
		homologs.skimList(maxDesiredHomologs);
	}
	
	/**
	 * Retrieves the Uniprot data and metadata
	 * @throws IOException
	 * @throws UniprotVerMisMatchException
	 */
	public void retrieveHomologsData() throws IOException, UniprotVerMisMatchException {
		homologs.retrieveUniprotKBData();
		homologs.retrieveUniparcData(null);
	}
	
	private void applyIdentityCutoff(double homSoftIdCutoff, double homHardIdCutoff, double homIdStep, double queryCovCutoff, int minHomologsCutoff) {
		// applying identity cutoff
		double idcutoff = homSoftIdCutoff;
		while (idcutoff>=homHardIdCutoff) {
			homologs.filterToMinIdAndCoverage(idcutoff, queryCovCutoff);
			if (homologs.getSizeFilteredSubset()>=minHomologsCutoff) break;
			LOGGER.info("Tried "+String.format("%4.2f",idcutoff)+" identity cutoff, only "+homologs.getSizeFilteredSubset()+" homologs found ("+minHomologsCutoff+" required)");
			idcutoff -= homIdStep;
		}
		LOGGER.info(homologs.getSizeFilteredSubset()+" homologs after applying "+String.format("%4.2f",idcutoff)+" identity cutoff and "+String.format("%4.2f",queryCovCutoff)+" query coverage cutoff");
	}

	public void align(File tcoffeeBin, boolean tcoffeeVeryFastMode, int nThreads) throws IOException, TcoffeeException, InterruptedException{
		// 3) alignment of the protein sequences using tcoffee
		homologs.computeTcoffeeAlignment(tcoffeeBin, tcoffeeVeryFastMode, nThreads);
	}
	
	public void writeAlignmentToFile(File alnFile) throws FileNotFoundException {
		homologs.writeAlignmentToFile(alnFile); 
	}
	
	public void writeNucleotideAlignmentToFile(File alnFile) throws FileNotFoundException {
		homologs.writeNucleotideAlignmentToFile(alnFile);
	}
	
	public void writeHomologSeqsToFile(File outFile) throws FileNotFoundException {
		homologs.writeToFasta(outFile, false);
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
	 * Returns a multiple sequence alignment of all valid CDS sequences from the 
	 * UniprotHomologList by mapping the CDS to the protein sequences alignment. 
	 * @return
	 */
	public MultipleSequenceAlignment getNucleotideAlignment() {
		return homologs.getNucleotideAlignment();
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
//		if (scoType.equals(ScoringType.KAKS)) {
//			return homologs.getKaksRatios();
//		}
		throw new IllegalArgumentException("Given scoring type "+scoType+" is not recognized ");

	}
	
	/**
	 * Compute the sequence ka/ks ratios with selecton for all reference CDS sequence positions
	 * @param selectonBin
	 * @param resultsFile
	 * @param logFile
	 * @param treeFile
	 * @param globalResultsFile
	 * @param epsilon
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void computeKaKsRatiosSelecton(File selectonBin, File resultsFile, File logFile, File treeFile, File globalResultsFile, double epsilon) 
	throws IOException, InterruptedException {
		homologs.computeKaKsRatiosSelecton(selectonBin, resultsFile, logFile, treeFile, globalResultsFile, epsilon);
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
//			else if (scoType==ScoringType.KAKS) {
//				queryPos = getQueryCDSPosForPDBPos(resSer);
//			}
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
		ps.print(this.query.getUniId()+" (");
		for (String emblcdsid: query.getEmblCdsIds()) {
			ps.print(" "+emblcdsid);
		}
		ps.println(" )");
		
		ps.println();
		ps.println("Uniprot version: "+homologs.getUniprotVer());
		ps.println("Homologs: "+homologs.getSizeFilteredSubset()+" at "+String.format("%4.2f",homologs.getIdCutoff())+" identity cut-off and "+
				String.format("%4.2f",homologs.getQCovCutoff())+" query coverage cutoff");
		for (Homolog hom:homologs.getFilteredSubset()) {
			ps.printf("%-13s",hom.getIdentifier());
			//ps.print(" (");
			//for (String emblcdsid: hom.getUniprotEntry().getEmblCdsIds()) {
			//	ps.print(" "+emblcdsid);
			//}
			//ps.print(" )");
			ps.printf("\t%5.1f",hom.getPercentIdentity());
			if (hom.isUniprot()) ps.print("\t"+hom.getUniprotEntry().getFirstTaxon()+"\t"+hom.getUniprotEntry().getLastTaxon());
			ps.println();
		}
	}
	
	public void printConservationScores(PrintStream ps, ScoringType scoType, PdbAsymUnit pdb) {
		if (scoType.equals(ScoringType.ENTROPY)) {
			ps.println("# Entropies for all query sequence positions based on a "+homologs.getReducedAlphabet()+" letters alphabet.");
			ps.println("# seqres\tpdb\tuniprot\tentropy");
		} 
//		else if (scoType.equals(ScoringType.KAKS)){
//			ps.println("# Ka/Ks for all query sequence positions.");
//			ps.println("# seqres\tpdb\ttranslated-CDS\tka/ks");
//		}
		PdbChain chain = pdb.getChain(representativeChain);
		List<Double> conservationScores = getConservationScores(scoType);
		for (int i=0;i<conservationScores.size();i++) {
			int resser = 0;
			if (scoType.equals(ScoringType.ENTROPY)) {
				resser = getPDBPosForQueryUniprotPos(i);
			} 
//			else if (scoType.equals(ScoringType.KAKS)){
//				resser = getPDBPosForQueryCDSPos(i);
//			}
			String pdbresser = chain.getPdbResSerFromResSer(resser);
			ps.printf("%4d\t%4s\t%4d\t%5.2f\n",resser,pdbresser,i+1,conservationScores.get(i));
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
	
//	public int getNumHomologsWithCDS() {
//		if (homologs==null) return 0;
//		return homologs.getNumHomologsWithCDS();
//	}
	
//	public int getNumHomologsWithValidCDS() {
//		if (homologs==null) return 0;
//		return homologs.getNumHomologsWithValidCDS();
//	}

//	/**
//	 * Gets the ProteinToCDSMatch of the best CDS match for the query protein.  
//	 * @return
//	 */
//	public ProteinToCDSMatch getQueryRepCDS() {
//		ProteinToCDSMatch seq = this.query.getRepresentativeCDS();
//		return seq;
//	}
	
//	public boolean isConsistentGeneticCodeType() {
//		return this.homologs.isConsistentGeneticCodeType();
//	}
	
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
	
//	/**
//	 * Tells whether a given position of the reference PDB sequence (starting at 1)
//	 * is reliable with respect to:
//	 * a) the CDS matching of the reference sequence (not reliable if CDS translation doesn't match exactly the protein residue)
//	 * b) the CDS matchings of the homologs (not reliable if CDS translation doesn't match exactly the protein residue)
//	 * @param resser the (cif) PDB residue serial corresponding to the SEQRES record, starting at 1
//	 * @return
//	 */
//	public boolean isPdbSeqPositionReliable(int resser) {
//		// we map the pdb resser to the uniprot sequence position and check whether it is reliable CDS-wise for all homologs
//		int uniprotPos = getQueryUniprotPosForPDBPos(resser);
//		if (uniprotPos==-1) {
//			return false; // if it maps to a gap then we have no info whatsoever from CDSs, totally unreliable
//		}
//		if (!homologs.isReferenceSeqPositionReliable(uniprotPos)) {
//			return false;
//		}
//		return true;		
//	}
	
	/**
	 * Given a residue serial of the reference PDB SEQRES sequence (starting at 1), returns
	 * its corresponding Uniprot's sequence index (starting at 0)
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
	
//	/**
//	 * Given a residue serial of the reference PDB SEQRES sequence (starting at 1), returns 
//	 * its corresponding representative-translated-CDS sequence index (starting at 0)  
//	 * @param resser
//	 * @return the mapped translated-CDS sequence position or -1 if it maps to a gap
//	 */
//	public int getQueryCDSPosForPDBPos(int resser) {
//		int uniprotPos = getQueryUniprotPosForPDBPos(resser);
//		if (uniprotPos==-1) {
//			return -1;
//		}
//		return this.getQueryRepCDS().getBestTranslation().getAln().getMapping1To2(uniprotPos);
//	}
	
//	/**
//	 * Given a sequence index of the query's representative-translated CDS sequence (starting at 0), 
//	 * returns its corresponding residue serial of the reference PDB SEQRES sequence (starting at 1). 
//	 * @param queryPos
//	 * @return the mapped PDB SEQRES sequence position or -1 if it maps to a gap
//	 */
//	public int getPDBPosForQueryCDSPos(int queryPos) {
//		int uniprotPos = this.getQueryRepCDS().getBestTranslation().getAln().getMapping2To1(queryPos);
//		if (uniprotPos==-1){
//			return -1;
//		}
//		return this.getPDBPosForQueryUniprotPos(uniprotPos);
//	}
	
	private UniprotEntry findUniprotMapping(String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads, double pdb2uniprotIdThreshold, double pdb2uniprotQcovThreshold) throws IOException, BlastException, InterruptedException {
		
		File outBlast = File.createTempFile(BLAST_BASENAME,BLASTOUT_SUFFIX);
		File inputSeqFile = File.createTempFile(BLAST_BASENAME,FASTA_SUFFIX);
		if (!DEBUG) {
			outBlast.deleteOnExit();
			inputSeqFile.deleteOnExit();
		}
		
		new Sequence(pdbName,this.sequence).writeToFastaFile(inputSeqFile);

		String uniprotVer = HomologList.readUniprotVer(blastDbDir);
		LOGGER.info("Query blast search for Uniprot mapping using Uniprot version "+uniprotVer);
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

		UniprotEntry uniprotMapping = null;
		BlastHit best = blastList.getBestHit(); // if null then list was empty, no hits at all
		if (best!=null && (best.getTotalPercentIdentity()/100.0)>pdb2uniprotIdThreshold && best.getQueryCoverage()>pdb2uniprotQcovThreshold) {
			
			String sid = best.getSubjectId();
			Matcher m = Sequence.DEFLINE_PRIM_ACCESSION_REGEX.matcher(sid);
			if (m.matches()) {
				String uniId = m.group(1);
				uniprotMapping = new UniprotEntry(uniId);
			} else {
				Matcher m2 = Sequence.DEFLINE_PRIM_ACCESSION_UNIREF_REGEX.matcher(sid);
				if (m2.matches()) {
					String uniId = m2.group(1);
					if (uniId.startsWith("UPI") || uniId.contains("-")) {
						LOGGER.error("Best blast hit "+uniId+" is a Uniparc id or a Uniprot isoform id. No Uniprot match");						
					} else {
						uniprotMapping = new UniprotEntry(uniId);
					}
				} else {
					LOGGER.error("Could not find Uniprot id in subject id "+sid+". No Uniprot match");
				}
			}
			
		} else {
			LOGGER.error("No Uniprot match could be found for the query "+pdbName+representativeChain);
			if (best!=null) {
				LOGGER.error("Best match was "+best.getSubjectId()+", with "+
						String.format("%5.2f%% id and %4.2f coverage",best.getTotalPercentIdentity(),best.getQueryCoverage()));
				LOGGER.error("Alignment: ");
				LOGGER.error(best.getMaxScoringHsp().getAlignment().getFastaString(null, true));
			}
		}
		return uniprotMapping;
	}

//	/**
//	 * Tells whether Ka/Ks analysis is possible for this chain.
//	 * Ka/Ks analysis will not be possible in following cases: 
//	 * - no uniprot query match 
//	 * - no representative CDS for the query 
//	 * - no consistency in genetic code types in homologs
//	 * @return
//	 */
//	public boolean canDoKaks() {
//		boolean canDoKaks = true;
//		if (!hasQueryMatch() || !this.homologs.hasCDSData() || getQueryRepCDS()==null || !isConsistentGeneticCodeType()) {
//			canDoKaks = false;
//		}
//		return canDoKaks;
//	}
	
	/**
	 * Gets the PDB identifier: a PDB code if query was a PDB entry or a PDB file name. 
	 * @return
	 */
	public String getPdbName() {
		return this.pdbName;
	}
	
	public String getUniprotVer() {
		return this.homologs.getUniprotVer();
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
	public UniprotEntry getQuery() {
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
}
