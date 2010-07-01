package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.features.SiftsFeature;
import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.sequence.ProteinToCDSMatch;
import owl.core.sequence.UniprotEntry;
import owl.core.sequence.UniprotHomolog;
import owl.core.sequence.UniprotHomologList;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
import owl.core.structure.Pdb;
import owl.core.structure.PdbLoadError;

public class ChainEvolContext {
	
	private Map<String,Pdb> pdbs; 			// pdbs for all chains corresponding to this entity (pdb chain codes to Pdb objects)
	private String representativeChain;		// the pdb chain code of the representative chain
	private String pdbCode; 		 		// the pdb code (if no pdb code then Pdb.NO_PDB_CODE)
	private String sequence;
	
	private UniprotEntry query;							// the uniprot id, seq, cds corresponding to this chain's sequence
	private PairwiseSequenceAlignment alnPdb2Uniprot; 	// the alignment between the pdb sequence and the uniprot sequence (query)
	
	private UniprotHomologList homologs;	// the homologs of this chain's sequence
		
	
	public ChainEvolContext(Map<String,Pdb> pdbs, String representativeChain) {
		this.pdbs = pdbs;
		this.pdbCode = pdbs.get(representativeChain).getPdbCode();
		this.sequence = pdbs.get(representativeChain).getSequence();
		this.representativeChain = representativeChain;
	}
	
	/**
	 * Retrieves the Uniprot mapping corresponding to the query PDB sequence 
	 * @param siftsLocation file or URL of the SIFTS PDB to Uniprot mapping table
	 * @param emblCDScache a FASTA file containing the cached sequences (if present, sequences
	 * won't be refetched online
	 * @throws IOException
	 * @throws PdbLoadError
	 */
	public void retrieveQueryData(String siftsLocation, File emblCDScache) throws IOException, PdbLoadError {
		
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
					System.err.println("More than one uniprot SIFTS mapping for the query PDB code "+pdbCode);
					System.err.print("Uniprot IDs are: ");
					for (String uniId:uniqUniIds){
						System.err.print(uniId+" ");
					}
					System.err.println();
					System.err.println("Check if the PDB entry is biologically reasonable (likely to be an engineered entry). Won't continue.");
					System.exit(1);
				}
				query = new UniprotEntry(uniqUniIds.iterator().next());


			} catch (NoMatchFoundException e) {
				System.err.println("No SIFTS mapping could be found for "+pdbCode+representativeChain);
				System.exit(1);
				//TODO blast, find uniprot mapping and use it if one can be found
			}
		// 2) PDB code not known and so SiftsFeatures have to be found by blasting, aligning etc.
		} else {
			System.err.println("No PDB code given. Can't continue, not implemented yet!");
			System.exit(1);
			//TODO blast to find mapping
		}
		
		System.out.print("Uniprot id for the query "+pdbCode+representativeChain+": ");
		System.out.println(query.getUniId());
		
		// once we have the identifier we get the data from uniprot
		query.retrieveUniprotKBData();
		query.retrieveEmblCdsSeqs(emblCDScache);
		
		
		// and finally we align the 2 sequences (rather than trusting the SIFTS alignment info)
		try {
			alnPdb2Uniprot = new PairwiseSequenceAlignment(sequence, query.getUniprotSeq().getSeq(), pdbCode+representativeChain, query.getUniprotSeq().getName());
			System.out.println("The PDB SEQRES to Uniprot alignmnent:");
			alnPdb2Uniprot.writeAlignment(System.out);
		} catch (PairwiseSequenceAlignmentException e1) {
			System.err.println("Problem aligning PDB sequence "+pdbCode+representativeChain+" to its Uniprot match "+query.getUniId());
			System.err.println(e1.getMessage());
			System.err.println("Can't continue");
			System.exit(1);
		}
		// TODO anyway we should do also a sanity check of our alignment against the SIFTS mappings (if we have them) 
		//if (mappings!=null) {
		// compare mappings to the alnPdb2Uniprot	
		//}
	}
	
	public void retrieveHomologs(String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads, double idCutoff, File emblCDScache, File blastCache) 
	throws IOException, BlastError {
		homologs = new UniprotHomologList(query);
		
		System.out.println("Blasting...");
		homologs.searchWithBlast(blastBinDir, blastDbDir, blastDb, blastNumThreads, blastCache);
		System.out.println(homologs.size()+" homologs found by blast");
		
		applyIdentityCutoff(idCutoff);
		
		System.out.println("Looking up UniprotKB data...");
		homologs.retrieveUniprotKBData();
		
		System.out.println("Retrieving EMBL cds sequences...");
		homologs.retrieveEmblCdsSeqs(emblCDScache);
				
	}
	
	private void applyIdentityCutoff(double idCutoff) {
		// applying identity cutoff
		homologs.restrictToMinId(idCutoff);
		System.out.println(homologs.size()+" homologs after applying "+String.format("%4.2f",idCutoff)+" identity cutoff");

	}

	public void align(File tcoffeeBin, boolean tcoffeeVeryFastMode) throws IOException, TcoffeeError{
		// 3) alignment of the protein sequences using tcoffee
		System.out.println("Aligning protein sequences with t_coffee...");
		homologs.computeTcoffeeAlignment(tcoffeeBin, tcoffeeVeryFastMode);
	}
	
	public void writeAlignmentToFile(File alnFile) throws FileNotFoundException {
		homologs.writeAlignmentToFile(alnFile); 
	}
	
	public void writeNucleotideAlignmentToFile(File alnFile) throws FileNotFoundException {
		homologs.writeNucleotideAlignmentToFile(alnFile);
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
		return pdbs.get(pdbChainCode);
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
			map.put(resser, conservationScores.get(resser-1));
		}
		pdb.setBFactorsPerResidue(map);		
	}
	
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
		ps.println("Query: "+pdbCode+representativeChain);
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
		} else if (scoType.equals(ScoringType.KAKS)){
			ps.println("# Ka/Ks for all query sequence positions.");
		}
		List<Double> conservationScores = getConservationScores(scoType);
		for (int i=0;i<conservationScores.size();i++) {
			ps.printf("%4d\t%5.2f\n",i+1,conservationScores.get(i));
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
		if (!homologs.isReferenceSeqPositionReliable(getQueryUniprotPosForPDBPos(resser))) {
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
	 * Given a residue serial of the reference PDB SEQRES sequence (starting at 1), returns 
	 * its corresponding representative-translated-CDS sequence index (starting at 0)  
	 * @param resser
	 * @return the mapped translated-CDS sequence position or -1 if it maps to a gap
	 */
	public int getQueryCDSPosForPDBPos(int resser) {
		return this.getQueryRepCDS().getBestTranslation().getAln().getMapping1To2(getQueryUniprotPosForPDBPos(resser));
	}
}
