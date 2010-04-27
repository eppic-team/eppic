package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import owl.core.runners.blast.BlastError;
import owl.core.sequence.UniprotHomolog;
import owl.core.sequence.UniprotHomologList;
import owl.core.sequence.Sequence;
import owl.core.structure.Pdb;
import owl.core.structure.PdbCodeNotFoundError;
import owl.core.structure.PdbLoadError;
import owl.core.structure.PdbasePdb;
import owl.core.util.MySQLConnection;

public class CRKMain {
	
	private static final String PROGRAM_NAME = "crk";
	
	private static final String PDBASEDB = "pdbase";
	
	private static final String BLAST_BIN_DIR = "/usr/bin";
	private static final String BLAST_DB_DIR = "/nfs/data/dbs/uniprot/current";
	private static final String BLAST_DB = "uniprot_sprot.fasta";//"uniprot_all.fasta";
	private static final int DEFAULT_BLAST_NUMTHREADS = 1;

	public static void main(String[] args) throws SQLException, PdbCodeNotFoundError, PdbLoadError, IOException, BlastError {
		
		String pdbId = null;
		String baseName = null;
		File outDir = new File(".");
		int blastNumThreads = DEFAULT_BLAST_NUMTHREADS;

		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -i :  input PDB code\n" +
		"  [-a]:  number of threads for blast. Default: "+DEFAULT_BLAST_NUMTHREADS+"\n"+
		"  [-b]:  basename for output files. Default: PDB code \n"+
		"  [-o]:  output dir, where output files will be written. Default: current dir \n\n";


		Getopt g = new Getopt(PROGRAM_NAME, args, "i:a:b:o:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				pdbId = g.getOptarg();
				break;
			case 'a':
				blastNumThreads = Integer.parseInt(g.getOptarg());
				break;
			case 'b':
				baseName = g.getOptarg();
				break;				
			case 'o':
				outDir = new File(g.getOptarg());
				break;
			case 'h':
			case '?':
				System.out.println(help);
				System.exit(0);
				break; // getopt() already printed an error
			}
		}
		
		if (pdbId==null) {
			System.err.println("Missing argument -i");
			System.exit(1);
		}
		
		if (baseName==null) {
			baseName=pdbId;
		}

		MySQLConnection conn = new MySQLConnection();
	
		String pdbCode = pdbId.substring(0, 4);
		String pdbChainCode = pdbId.substring(4,5);
		
		Pdb pdb = new PdbasePdb(pdbCode,PDBASEDB,conn);
		pdb.load(pdbChainCode);
		
		UniprotHomologList homologs = new UniprotHomologList(pdbId, pdb.getSequence());
		
		System.out.println("Blasting...");
		homologs.searchWithBlast(BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, blastNumThreads);
		
		System.out.println("Looking up UniprotKB data...");
		homologs.retrieveUniprotKBData();
		
		System.out.println("Retrieving EMBL cds sequences...");
		homologs.retrieveEmblCdsSeqs();
		
		System.out.println("Summary:");
		for (UniprotHomolog hom:homologs) {
			System.out.printf("%s\t%5.1f",hom.getUniId(),hom.getPercentIdentity());
			for (String id:hom.getTaxIds()){
				System.out.print("\t"+id);
			}
			for (String emblCdsId:hom.getEmblCdsIds()) {
				System.out.print("\t"+emblCdsId);
			}
			System.out.println();
			for (Sequence seq:hom.getEmblCdsSeqs()) {
				seq.writeToPrintStream(System.out);
			}
		}
		
	}

}
