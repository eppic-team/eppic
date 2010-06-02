package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.features.InvalidFeatureCoordinatesException;
import owl.core.features.OverlappingFeatureException;
import owl.core.features.SiftsFeature;
import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.sequence.UniprotHomolog;
import owl.core.sequence.UniprotHomologList;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.structure.Pdb;
import owl.core.structure.PdbLoadError;

public class ChainEvolContext {
	
	private Pdb pdb;
	private String pdbChainCode;
	
	private List<UniprotHomolog> queryData;
	
	private UniprotHomologList homologs;
	
	private MultipleSequenceAlignment aln;
	
	public ChainEvolContext(Pdb pdb, String pdbChainCode) {
		this.pdb = pdb;
		this.pdbChainCode = pdbChainCode;
	}
	
	public void retrieveQueryData(String siftsFile) throws IOException, PdbLoadError {
		
		queryData = new ArrayList<UniprotHomolog>();
		pdb.load(pdbChainCode);
		if (!pdb.getPdbCode().equals(Pdb.NO_PDB_CODE)) {
			String pdbCode = pdb.getPdbCode();
			SiftsConnection siftsConn = new SiftsConnection(siftsFile);
			Collection<SiftsFeature> mappings = null;
			try {
				mappings = siftsConn.getMappings(pdbCode, pdbChainCode);		
				for (SiftsFeature mapping:mappings) {
					pdb.addFeature(mapping); 
				}
				for (SiftsFeature sifts:mappings) {
					queryData.add(new UniprotHomolog(sifts.getUniprotId()));
				}

			} catch (NoMatchFoundException e) {
				System.err.println("No SIFTS mapping could be found for "+pdbCode+pdbChainCode);
				//TODO blast, find uniprot mapping and use it if one can be found
			} catch (OverlappingFeatureException e1) {
				System.err.println("Unexpected error");
				System.err.println(e1.getMessage());
				System.exit(1);
			} catch (InvalidFeatureCoordinatesException e2){
				System.err.println("Unexpected error");
				System.err.println(e2.getMessage());
				System.exit(1);			
			}
		} else {
			//TODO blast to find mapping
		}

		System.out.println("Uniprot ids for the query "+pdb.getPdbCode()+pdbChainCode+": ");
		for (UniprotHomolog queryMember:queryData) {
			queryMember.retrieveUniprotKBData();
			queryMember.retrieveEmblCdsSeqs();
			System.out.println(queryMember.getUniId());
		}
	}
	
	public void retrieveHomologs(String blastBinDir, String blastDbDir, String blastDb, int blastNumThreads, double idCutoff) 
	throws IOException, BlastError {
		homologs = new UniprotHomologList(pdb.getPdbCode()+pdbChainCode, pdb.getSequence());
		
		System.out.println("Blasting...");
		homologs.searchWithBlast(blastBinDir, blastDbDir, blastDb, blastNumThreads);
		System.out.println(homologs.size()+" homologs found by blast");
		
		applyIdentityCutoff(idCutoff);
		
		System.out.println("Looking up UniprotKB data...");
		homologs.retrieveUniprotKBData();
		
		System.out.println("Retrieving EMBL cds sequences...");
		homologs.retrieveEmblCdsSeqs();
		
//		System.out.println("Summary:");
//		for (UniprotHomolog hom:homologs) {
//			System.out.printf("%s\t%5.1f",hom.getUniId(),hom.getPercentIdentity());
//			for (String id:hom.getTaxIds()){
//				System.out.print("\t"+id);
//			}
//			for (String emblCdsId:hom.getEmblCdsIds()) {
//				System.out.print("\t"+emblCdsId);
//			}
//			System.out.println();
//			for (Sequence seq:hom.getEmblCdsSeqs()) {
//				seq.writeToPrintStream(System.out);
//			}
//		}
		
	}
	
	public void applyIdentityCutoff(double idCutoff) {
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

	public Pdb getPdb() {
		return pdb;
	}
	
	public void printSummary(PrintStream ps) {
		ps.println("Query: "+pdb.getPdbCode()+pdbChainCode);
		ps.println("Uniprot ids for query:");
		for (UniprotHomolog hom:queryData) {
			ps.print(hom.getUniId()+" (");
			for (String emblcdsid: hom.getEmblCdsIds()) {
				ps.print(" "+emblcdsid);
			}
			ps.println(" )");
		}
		ps.println();
		ps.println("Homologs: "+homologs.size()+" at "+String.format("%3.1f",homologs.getIdCutoff())+" identity cut-off");
		for (UniprotHomolog hom:homologs) {
			ps.print(hom.getUniId()+" (");
			for (String emblcdsid: hom.getEmblCdsIds()) {
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
}
