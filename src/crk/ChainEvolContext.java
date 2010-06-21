package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.features.InvalidFeatureCoordinatesException;
import owl.core.features.OverlappingFeatureException;
import owl.core.features.SiftsFeature;
import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.sequence.ProteinToCDSMatch;
import owl.core.sequence.UniprotEntry;
import owl.core.sequence.UniprotHomolog;
import owl.core.sequence.UniprotHomologList;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.structure.Pdb;
import owl.core.structure.PdbLoadError;

public class ChainEvolContext {
	
	private Map<String,Pdb> pdbs; 		// pdbs for all chains corresponding to this entity (pdb chain codes to Pdb objects)
	private String representativeChain;	// the pdb chain code of the representative chain
	private String pdbCode; 		 	// the pdb code (if no pdb code then Pdb.NO_PDB_CODE)
	private String sequence; 			// the sequence for this chain
	
	private List<UniprotEntry> queryData;	// the uniprot id, seq, cds corresponding to this chain's sequence
	
	private UniprotHomologList homologs;	// the homologs of this chain's sequence
	
	private MultipleSequenceAlignment aln;
	
	public ChainEvolContext(Map<String,Pdb> pdbs, String representativeChain) {
		this.pdbs = pdbs;
		this.pdbCode = pdbs.get(representativeChain).getPdbCode();
		this.sequence = pdbs.get(representativeChain).getSequence();
		this.representativeChain = representativeChain;
	}
	
	/**
	 * 
	 * @param siftsLocation file or URL of the SIFTS PDB to Uniprot mapping table
	 * @param emblCDScache a FASTA file containing the cached sequences (if present, sequences
	 * won't be refetched online
	 * @throws IOException
	 * @throws PdbLoadError
	 */
	public void retrieveQueryData(String siftsLocation, File emblCDScache) throws IOException, PdbLoadError {
		
		queryData = new ArrayList<UniprotEntry>();
		// two possible cases: 
		// 1) PDB code known and so SiftsFeatures can be taken from SiftsConnection
		Collection<SiftsFeature> mappings = null;
		if (!pdbCode.equals(Pdb.NO_PDB_CODE)) {
			SiftsConnection siftsConn = new SiftsConnection(siftsLocation);
			try {
				mappings = siftsConn.getMappings(pdbCode, representativeChain);		
				for (SiftsFeature sifts:mappings) {
					queryData.add(new UniprotEntry(sifts.getUniprotId()));
				}

			} catch (NoMatchFoundException e) {
				System.err.println("No SIFTS mapping could be found for "+pdbCode+representativeChain);
				//TODO blast, find uniprot mapping and use it if one can be found
			}
		// 2) PDB code not known and so SiftsFeatures have to be found by blasting, aligning etc.
		} else {
			//TODO blast to find mapping
		}

		// once we have the identifiers we get the data from uniprot
		System.out.println("Uniprot ids for the query "+pdbCode+representativeChain+": ");
		for (UniprotEntry entry:queryData) {
			entry.retrieveUniprotKBData();
			entry.retrieveEmblCdsSeqs(emblCDScache);
			System.out.println(entry.getUniId());
		}
		// and finally we add the SiftsFeatures if we have them
		if (mappings!=null) {
			try {
				for (UniprotEntry entry:queryData) {
					for (SiftsFeature sifts:mappings) {
						if (sifts.getUniprotId().equals(entry.getUniId())) {
							entry.addFeature(sifts);
						}
					}
				}
			} catch (InvalidFeatureCoordinatesException e) {
				System.err.println("Unexpected error: inconsistency in SIFTS mapping data.");
				System.err.println(e.getMessage());
				System.exit(1);
			} catch (OverlappingFeatureException e) {
				System.err.println("Unexpected error: inconsistency in SIFTS mapping data.");
				System.err.println(e.getMessage());
				System.exit(1);
			} 
		}
	}
	
	public void retrieveHomologs(String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads, double idCutoff, File emblCDScache, File blastCache) 
	throws IOException, BlastError {
		homologs = new UniprotHomologList(pdbCode+representativeChain, sequence);
		
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
		aln = homologs.getTcoffeeAlignment(tcoffeeBin, tcoffeeVeryFastMode);
	}
	
	public void writeAlignmentToFile(File alnFile) throws FileNotFoundException {
		aln.writeFasta(new PrintStream(alnFile), 80, true);
	}
	
	public MultipleSequenceAlignment getAlignment() {
		return aln;
	}

	public Pdb getPdb(String pdbChainCode) {
		return pdbs.get(pdbChainCode);
	}
	
	public void setEntropiesAsBfactors(String pdbChainCode, int reducedAlphabet) {
		Pdb pdb = getPdb(pdbChainCode);
		HashMap<Integer,Double> entropies = new HashMap<Integer, Double>();
		for (int resser:pdb.getAllSortedResSerials()){
			entropies.put(resser, this.aln.getColumnEntropy(this.aln.seq2al(pdbCode+representativeChain, resser), reducedAlphabet));
		}
		pdb.setBFactorsPerResidue(entropies);
	}
	
	public void printSummary(PrintStream ps) {
		ps.println("Query: "+pdbCode+representativeChain);
		ps.println("Uniprot ids for query:");
		for (UniprotEntry entry:queryData) {
			ps.print(entry.getUniId()+" (");
			for (String emblcdsid: entry.getEmblCdsIds()) {
				ps.print(" "+emblcdsid);
			}
			ps.println(" )");
		}
		ps.println();
		ps.println("Uniprot version: "+homologs.getUniprotVer());
		ps.println("Homologs: "+homologs.size()+" at "+String.format("%3.1f",homologs.getIdCutoff())+" identity cut-off");
		for (UniprotHomolog hom:homologs) {
			ps.print(hom.getUniId()+" (");
			for (String emblcdsid: hom.getUniprotEntry().getEmblCdsIds()) {
				ps.print(" "+emblcdsid);
			}
			ps.println(" )");
			
		}
	}
	
	public void printEntropies(PrintStream ps, int reducedAlphabet) {
		for (int i=1;i<=this.aln.getAlignmentLength();i++) {
			ps.printf("%4d\t%5.2f\n",i,this.aln.getColumnEntropy(i,reducedAlphabet));
		}
	}
	
	public int getNumHomologs() {
		return homologs.size();
	}
	
	public String getRepresentativeChainCode() {
		return representativeChain;
	}
	
	public int getNumHomologsWithCDS() {
		return homologs.getNumHomologsWithCDS();
	}
	
	public int getNumHomologsWithValidCDS() {
		return homologs.getNumHomologsWithValidCDS();
	}

	public ProteinToCDSMatch getRepQueryCDS() {
		ProteinToCDSMatch seq = null;
		for (UniprotEntry entry:queryData) {
			seq = entry.getRepresentativeCDS();
			if (seq == null){
				continue;
			} else {
				if (queryData.size()>1) {
					System.err.println("Query has multiple SIFTS mapping to uniprot, using the first one with good CDS translation.");
				}
				break;
			}
		}
		return seq;
	}
}
