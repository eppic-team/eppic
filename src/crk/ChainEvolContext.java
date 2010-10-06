package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.features.SiftsFeature;
import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.runners.blast.BlastHit;
import owl.core.runners.blast.BlastHitList;
import owl.core.runners.blast.BlastRunner;
import owl.core.runners.blast.BlastXMLParser;
import owl.core.sequence.ProteinToCDSMatch;
import owl.core.sequence.Sequence;
import owl.core.sequence.UniprotEntry;
import owl.core.sequence.UniprotHomolog;
import owl.core.sequence.UniprotHomologList;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
import owl.core.structure.Pdb;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadError;

public class ChainEvolContext {
	
	private static final Log LOGGER = LogFactory.getLog(ChainEvolContext.class);

	// blast constants
	private static final String BLASTOUT_SUFFIX = "blast.out.xml";
	private static final String BLAST_BASENAME = "pdb2unimapping";
	private static final String FASTA_SUFFIX = ".fa";
	private static final int 	BLAST_OUTPUT_TYPE = 7;  // xml output
	private static final boolean BLAST_NO_FILTERING = true;
	private static final boolean DEBUG = false;
	
	private static final double PDB2UNIPROT_ID_THRESHOLD = 95.0;
	private static final double PDB2UNIPROT_QCOVERAGE_THRESHOLD = 0.85;

	
	
	private PdbAsymUnit pdb; 				// all chains of the corresponding PDB entry 
	private String representativeChain;		// the pdb chain code of the representative chain
	private String pdbCode; 		 		// the pdb code (if no pdb code then Pdb.NO_PDB_CODE)
	private String sequence;
	private String pdbName;					// a name to identify the PDB, will be the pdbCode if a PDB entry or the file name without extension if from file
	
	private UniprotEntry query;							// the uniprot id, seq, cds corresponding to this chain's sequence
	private PairwiseSequenceAlignment alnPdb2Uniprot; 	// the alignment between the pdb sequence and the uniprot sequence (query)
	
	private UniprotHomologList homologs;	// the homologs of this chain's sequence
		
	
	public ChainEvolContext(PdbAsymUnit pdb, String representativeChain, String pdbName) {
		this.pdb = pdb;
		this.pdbCode = pdb.getPdbCode();
		this.pdbName = pdbName;
		this.sequence = pdb.getChain(representativeChain).getSequence();
		this.representativeChain = representativeChain;
	}
	
	/**
	 * Retrieves the Uniprot mapping corresponding to the query PDB sequence 
	 * @param siftsLocation file or URL of the SIFTS PDB to Uniprot mapping table
	 * @param emblCDScache a FASTA file containing the cached sequences (if present, sequences
	 * won't be refetched online
	 * @param blastBinDir
	 * @param blastDbDir
	 * @param blastDb
	 * @param blastNumThreads
	 * @throws IOException
	 * @throws PdbLoadError
	 * @throws BlastError
	 */
	public void retrieveQueryData(String siftsLocation, File emblCDScache, String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads) 
	throws IOException, PdbLoadError, BlastError {
		
		// two possible cases: 
		// 1) PDB code known and so SiftsFeatures can be taken from SiftsConnection
		Collection<SiftsFeature> mappings = null;
		if (!pdbCode.equals(Pdb.NO_PDB_CODE)) {
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
					LOGGER.error("Check if the PDB entry is biologically reasonable (likely to be an engineered entry). Won't continue.");
					System.exit(1);
				}
				query = new UniprotEntry(uniqUniIds.iterator().next());


			} catch (NoMatchFoundException e) {
				LOGGER.warn("No SIFTS mapping could be found for "+pdbCode+representativeChain);
				LOGGER.info("Trying blasting to find one.");
				query = findUniprotMapping(blastBinDir, blastDbDir, blastDb, blastNumThreads);
			}
		// 2) PDB code not known and so SiftsFeatures have to be found by blasting, aligning etc.
		} else {
			LOGGER.info("No PDB code available. Can't use SIFTS. Blasting to find the query's Uniprot mapping.");
			query = findUniprotMapping(blastBinDir, blastDbDir, blastDb, blastNumThreads);
		}
		
		LOGGER.info("Uniprot id for the query "+pdbCode+representativeChain+": "+query.getUniId());
		
		// once we have the identifier we get the data from uniprot
		query.retrieveUniprotKBData();
		query.retrieveEmblCdsSeqs(emblCDScache);
		
		
		// and finally we align the 2 sequences (in case of mapping from SIFTS we rather do this than trusting the SIFTS alignment info)
		try {
			alnPdb2Uniprot = new PairwiseSequenceAlignment(sequence, query.getUniprotSeq().getSeq(), pdbCode+representativeChain, query.getUniprotSeq().getName());
			LOGGER.info("The PDB SEQRES to Uniprot alignmnent:\n"+alnPdb2Uniprot.getFormattedAlignmentString());
		} catch (PairwiseSequenceAlignmentException e1) {
			LOGGER.fatal("Problem aligning PDB sequence "+pdbCode+representativeChain+" to its Uniprot match "+query.getUniId());
			LOGGER.fatal(e1.getMessage());
			LOGGER.fatal("Can't continue");
			System.exit(1);
		}
		// TODO anyway we should do also a sanity check of our alignment against the SIFTS mappings (if we have them) 
		//if (mappings!=null) {
		// compare mappings to the alnPdb2Uniprot	
		//}
	}
	
	public void retrieveHomologs(String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads, double idCutoff, double queryCovCutoff, File blastCache) 
	throws IOException, BlastError {
		homologs = new UniprotHomologList(query);
		
		homologs.searchWithBlast(blastBinDir, blastDbDir, blastDb, blastNumThreads, blastCache);
		LOGGER.info(homologs.size()+" homologs found by blast");
		
		applyIdentityCutoff(idCutoff, queryCovCutoff);
	}
	
	public void removeRedundancy() {
		homologs.removeRedundancy();
	}
	
	public void skimList(int maxDesiredHomologs) {
		homologs.skimList(maxDesiredHomologs);
	}
	
	public void retrieveHomologsData(File emblCDScache) throws IOException {
		homologs.retrieveUniprotKBData();
		
		homologs.retrieveEmblCdsSeqs(emblCDScache);
		
	}
	
	private void applyIdentityCutoff(double idCutoff, double queryCovCutoff) {
		// applying identity cutoff
		homologs.restrictToMinIdAndCoverage(idCutoff, queryCovCutoff);
		LOGGER.info(homologs.size()+" homologs after applying "+String.format("%4.2f",idCutoff)+" identity cutoff and "+String.format("%4.2f",queryCovCutoff)+" query coverage cutoff");

	}

	public void align(File tcoffeeBin, boolean tcoffeeVeryFastMode) throws IOException, TcoffeeError{
		// 3) alignment of the protein sequences using tcoffee
		homologs.computeTcoffeeAlignment(tcoffeeBin, tcoffeeVeryFastMode);
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
	 * Returns a multiple sequence alignment of all valid CDS sequences from the 
	 * UniprotHomologList by mapping the CDS to the protein sequences alignment. 
	 * @return
	 */
	public MultipleSequenceAlignment getNucleotideAlignment() {
		return homologs.getNucleotideAlignment();
	}
	
	public Pdb getPdb(String pdbChainCode) {
		return pdb.getChain(pdbChainCode);
	}
	
	public int getResSerFromPdbResSer(String pdbChainCode, String pdbResSer) {
		return this.getPdb(pdbChainCode).getResSerFromPdbResSer(pdbResSer);
	}

	/**
	 * Set the b-factors of the Pdb object corresponding to given pdbChainCode
	 * with conservation score values (entropy or ka/ks).
	 * @param pdbChainCode
	 * @param scoType
	 * @throws NullPointerException if ka/ks ratios are not calculated yet by calling {@link #computeKaKsRatiosSelecton(File)}
	 */
	public void setConservationScoresAsBfactors(String pdbChainCode, ScoringType scoType) {
		List<Double> conservationScores = getConservationScores(scoType);		
		Pdb pdb = getPdb(pdbChainCode);
		HashMap<Integer,Double> map = new HashMap<Integer, Double>();
		for (int resser:pdb.getAllSortedResSerials()){
			int queryPos = -2;
			if (scoType==ScoringType.ENTROPY) {
				queryPos = this.getQueryUniprotPosForPDBPos(resser); 
			} else if (scoType==ScoringType.KAKS) {
				queryPos = this.getQueryCDSPosForPDBPos(resser);
			}
			if (queryPos!=-1) {   
				map.put(resser, conservationScores.get(queryPos));	
			}
		}
		pdb.setBFactorsPerResidue(map);		
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
		if (scoType.equals(ScoringType.KAKS)) {
			return homologs.getKaksRatios();
		}
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
	 */
	public void computeKaKsRatiosSelecton(File selectonBin, File resultsFile, File logFile, File treeFile, File globalResultsFile, double epsilon) 
	throws IOException {
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
		ps.println("Homologs: "+homologs.size()+" at "+String.format("%3.1f",homologs.getIdCutoff())+" identity cut-off");
		for (UniprotHomolog hom:homologs) {
			ps.print(hom.getUniId()+" (");
			for (String emblcdsid: hom.getUniprotEntry().getEmblCdsIds()) {
				ps.print(" "+emblcdsid);
			}
			ps.print(" )");
			ps.println("\t"+String.format("%5.1f",hom.getPercentIdentity())+"\t"+hom.getUniprotEntry().getFirstTaxon()+"\t"+hom.getUniprotEntry().getLastTaxon());
		}
	}
	
	public void printConservationScores(PrintStream ps, ScoringType scoType) {
		if (scoType.equals(ScoringType.ENTROPY)) {
			ps.println("# Entropies for all query sequence positions based on a "+homologs.getReducedAlphabet()+" letters alphabet.");
			ps.println("# seqres\tuniprot\tentropy");
		} else if (scoType.equals(ScoringType.KAKS)){
			ps.println("# Ka/Ks for all query sequence positions.");
			ps.println("# seqres\ttranslated-CDS\tka/ks");
		}
		List<Double> conservationScores = getConservationScores(scoType);
		for (int i=0;i<conservationScores.size();i++) {
			int resser = 0;
			if (scoType.equals(ScoringType.ENTROPY)) {
				resser = getPDBPosForQueryUniprotPos(i);
			} else if (scoType.equals(ScoringType.KAKS)){
				resser = getPDBPosForQueryCDSPos(i);
			}
			ps.printf("%4d\t%4d\t%5.2f\n",resser,i+1,conservationScores.get(i));
		}
	}
	
	public int getNumHomologs() {
		return homologs.size();
	}
	
	public String getRepresentativeChainCode() {
		return representativeChain;
	}
	
	public String getPdbSequence() {
		return sequence;
	}
	
	public int getNumHomologsWithCDS() {
		return homologs.getNumHomologsWithCDS();
	}
	
	public int getNumHomologsWithValidCDS() {
		return homologs.getNumHomologsWithValidCDS();
	}

	/**
	 * Gets the ProteinToCDSMatch of the best CDS match for the query protein.  
	 * @return
	 */
	public ProteinToCDSMatch getQueryRepCDS() {
		ProteinToCDSMatch seq = this.query.getRepresentativeCDS();
		return seq;
	}
	
	public boolean isConsistentGeneticCodeType() {
		return this.homologs.isConsistentGeneticCodeType();
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
	 * Tells whether a given position of the reference PDB sequence (starting at 1)
	 * is reliable with respect to:
	 * a) the CDS matching of the reference sequence (not reliable if CDS translation doesn't match exactly the protein residue)
	 * b) the CDS matchings of the homologs (not reliable if CDS translation doesn't match exactly the protein residue)
	 * @param resser the (cif) PDB residue serial corresponding to the SEQRES record, starting at 1
	 * @return
	 */
	public boolean isPdbSeqPositionReliable(int resser) {
		// we map the pdb resser to the uniprot sequence position and check whether it is reliable CDS-wise for all homologs
		int uniprotPos = getQueryUniprotPosForPDBPos(resser);
		if (uniprotPos==-1) {
			return false; // if it maps to a gap then we have no info whatsoever from CDSs, totally unreliable
		}
		if (!homologs.isReferenceSeqPositionReliable(uniprotPos)) {
			return false;
		}
		return true;		
	}
	
	/**
	 * Given a residue serial of the reference PDB SEQRES sequence (starting at 1), returns
	 * its corresponding uniprot's sequence index (starting at 0)
	 * @param resser
	 * @return the mapped uniprot sequence position or -1 if it maps to a gap
	 */
	public int getQueryUniprotPosForPDBPos(int resser) {
		return alnPdb2Uniprot.getMapping1To2(resser-1);
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
	
	/**
	 * Given a residue serial of the reference PDB SEQRES sequence (starting at 1), returns 
	 * its corresponding representative-translated-CDS sequence index (starting at 0)  
	 * @param resser
	 * @return the mapped translated-CDS sequence position or -1 if it maps to a gap
	 */
	public int getQueryCDSPosForPDBPos(int resser) {
		int uniprotPos = getQueryUniprotPosForPDBPos(resser);
		if (uniprotPos==-1) {
			return -1;
		}
		return this.getQueryRepCDS().getBestTranslation().getAln().getMapping1To2(uniprotPos);
	}
	
	/**
	 * Given a sequence index of the query's representative-translated CDS sequence (starting at 0), 
	 * returns its corresponding residue serial of the reference PDB SEQRES sequence (starting at 1). 
	 * @param queryPos
	 * @return the mapped PDB SEQRES sequence position or -1 if it maps to a gap
	 */
	public int getPDBPosForQueryCDSPos(int queryPos) {
		int uniprotPos = this.getQueryRepCDS().getBestTranslation().getAln().getMapping2To1(queryPos);
		if (uniprotPos==-1){
			return -1;
		}
		return this.getPDBPosForQueryUniprotPos(uniprotPos);
	}
	
	private UniprotEntry findUniprotMapping(String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads) throws IOException, BlastError {
		
		File outBlast = File.createTempFile(BLAST_BASENAME,BLASTOUT_SUFFIX);
		File inputSeqFile = File.createTempFile(BLAST_BASENAME,FASTA_SUFFIX);
		if (!DEBUG) {
			outBlast.deleteOnExit();
			inputSeqFile.deleteOnExit();
		}
		
		new Sequence(pdbName,this.sequence).writeToFastaFile(inputSeqFile);

		BlastRunner blastRunner = new BlastRunner(blastBinDir, blastDbDir);
		blastRunner.runBlastp(inputSeqFile, blastDb, outBlast, BLAST_OUTPUT_TYPE, BLAST_NO_FILTERING, blastNumThreads);
		String uniprotVer = UniprotHomologList.readUniprotVer(blastDbDir);
		LOGGER.info("Query blast search for Uniprot mapping using Uniprot version "+uniprotVer);
		

		BlastHitList blastList = null;
		try {
			BlastXMLParser blastParser = new BlastXMLParser(outBlast);
			blastList = blastParser.getHits();
		} catch (SAXException e) {
			// if this happens it means that blast doesn't format correctly its XML, i.e. has a bug
			System.err.println("Unexpected error: "+e.getMessage());
			System.exit(1);
		}

		UniprotEntry uniprotMapping = null;
		BlastHit best = blastList.getBestHit();
		if (best.getPercentIdentity()>PDB2UNIPROT_ID_THRESHOLD && best.getQueryCoverage()>PDB2UNIPROT_QCOVERAGE_THRESHOLD) {
			
			String sid = best.getSubjectId();
			Matcher m = Sequence.DEFLINE_PRIM_ACCESSION_REGEX.matcher(sid);
			if (m.matches()) {
				String uniId = m.group(1);
				uniprotMapping = new UniprotEntry(uniId);
			} else {
				LOGGER.error("Could not find uniprot id in subject id "+sid);
				System.exit(1);
			}
		} else {
			LOGGER.error("No Uniprot match could be found for the query "+pdbName+". Best was "+
					String.format("%5.2f%% id and %4.2f coverage",best.getPercentIdentity(),best.getQueryCoverage()));
			System.exit(1);
		}
		return uniprotMapping;
	}
	
}
