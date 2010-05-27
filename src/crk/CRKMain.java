package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import owl.core.connections.pisa.PisaConnection;
import owl.core.connections.pisa.PisaInterface;
import owl.core.connections.pisa.PisaMolecule;
import owl.core.connections.pisa.PisaResidue;
import owl.core.connections.pisa.PisaRimCore;
import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.structure.Pdb;
import owl.core.structure.PdbCodeNotFoundError;
import owl.core.structure.PdbLoadError;
import owl.core.structure.PdbasePdb;
import owl.core.util.MySQLConnection;

public class CRKMain {
	
	private static final String   PROGRAM_NAME = "crk";
	
	private static final String   PDBASEDB = "pdbase";
	
	private static final String   SIFTS_FILE = "/nfs/data/dbs/uniprot/current/pdb_chain_uniprot.lst";
	
	private static final String   BLAST_BIN_DIR = "/home/duarte_j/bin";
	private static final String   BLAST_DB_DIR = "/nfs/data/dbs/uniprot/current";
	private static final String   BLAST_DB = "uniprot_all.fasta"; //"uniprot_sprot.fasta";
	private static final int      DEFAULT_BLAST_NUMTHREADS = 1;
	
	private static final File     TCOFFE_BIN = new File("/usr/bin/t_coffee");
	private static final boolean  TCOFFEE_VERYFAST_MODE = true;
	
	private static final double   DEFAULT_IDENTITY_CUTOFF = 0.6;
	
	private static final String   PISA_INTERFACES_URL = "http://www.ebi.ac.uk/msd-srv/pisa/cgi-bin/interfaces.pisa?";
	
	// core assignment
	private static final double   SOFT_CUTOFF_CA = 0.95;
	private static final double   HARD_CUTOFF_CA = 0.82;
	private static final double   RELAX_STEP_CA = 0.01;
	private static final int      MIN_NUM_RES_CA = 6;

	public static void main(String[] args) throws SQLException, PdbCodeNotFoundError, PdbLoadError, IOException, BlastError, TcoffeeError, SAXException {
		
		String pdbCode = null;
		double idCutoff = DEFAULT_IDENTITY_CUTOFF;
		String baseName = null;
		File outDir = new File(".");
		int blastNumThreads = DEFAULT_BLAST_NUMTHREADS;

		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -i :  input PDB code\n" +
		"  [-d]:  sequence identity cut-off, homologs below this threshold won't be considered, \n" +
		"         default: "+String.format("%3.1f",DEFAULT_IDENTITY_CUTOFF)+"\n"+
		"  [-a]:  number of threads for blast. Default: "+DEFAULT_BLAST_NUMTHREADS+"\n"+
		"  [-b]:  basename for output files. Default: PDB code \n"+
		"  [-o]:  output dir, where output files will be written. Default: current dir \n\n";


		Getopt g = new Getopt(PROGRAM_NAME, args, "i:d:a:b:o:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				pdbCode = g.getOptarg();
				break;
			case 'd':
				idCutoff = Double.parseDouble(g.getOptarg());
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
		
		if (pdbCode==null) {
			System.err.println("Missing argument -i");
			System.exit(1);
		}
		
		if (baseName==null) {
			baseName=pdbCode;
		}

		// files
		
		File alnFile = new File(outDir,baseName+".homologs.aln");
		


		MySQLConnection conn = new MySQLConnection();
	
		Pdb pdb = new PdbasePdb(pdbCode,PDBASEDB,conn);
		String[] chains = pdb.getChains();
		Map<String, List<String>> uniqSequences = new HashMap<String, List<String>>();
		// finding the entities (groups of identical chains)
		for (String chain:chains) {
			
			pdb.load(chain);
			if (uniqSequences.containsKey(pdb.getSequence())) {
				uniqSequences.get(pdb.getSequence()).add(chain);
			} else {
				List<String> list = new ArrayList<String>();
				list.add(chain);
				uniqSequences.put(pdb.getSequence(),list);
			}		
		}
		System.out.println("Unique sequences for "+pdbCode+": ");
		int i = 1;
		for (List<String> entity:uniqSequences.values()) {
			System.out.print(i+":");
			for (String chain:entity) {
				System.out.print(" "+chain);
			}
			System.out.println();
		}
		

		Map<String,ChainEvolContext> allChains = new HashMap<String,ChainEvolContext>();
		for (List<String> entity:uniqSequences.values()) {
			String representativeChain = entity.get(0);
			ChainEvolContext chainEvCont = new ChainEvolContext(new PdbasePdb(pdbCode,PDBASEDB,conn), representativeChain);
			// 1) getting the uniprot ids corresponding to the query (the pdb sequence)
			chainEvCont.retrieveQueryData(SIFTS_FILE);
			// 2) getting the homologs and sequence data and creating multiple sequence alignment
			chainEvCont.retrieveHomologs(BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, blastNumThreads, idCutoff);
			// align
			chainEvCont.align(TCOFFE_BIN, TCOFFEE_VERYFAST_MODE);
			// writing the alignment to file
			chainEvCont.writeAlignmentToFile(alnFile);

			for (String chain:entity) {
				allChains.put(chain,chainEvCont);
			}
		}
		
		
		// 3) getting PISA interfaces description
		PisaConnection pc = new PisaConnection(PISA_INTERFACES_URL, null, null);
		List<String> pdbCodes = new ArrayList<String>();
		pdbCodes.add(pdbCode);
		List<PisaInterface> interfaces = pc.getInterfacesDescription(pdbCodes).get(pdbCode);
		
		for (PisaInterface pi:interfaces) {
			PisaMolecule mol1 = pi.getFirstMolecule();
			PisaRimCore rimcore1 = mol1.getRimAndCore(SOFT_CUTOFF_CA,HARD_CUTOFF_CA,RELAX_STEP_CA,MIN_NUM_RES_CA);
			
			PisaMolecule mol2 = pi.getSecondMolecule();
			PisaRimCore rimcore2 = mol2.getRimAndCore(SOFT_CUTOFF_CA,HARD_CUTOFF_CA,RELAX_STEP_CA,MIN_NUM_RES_CA);
			

			System.out.println("## Interface "+pi.getId());
			System.out.println("# First molecule "+mol1.getChainId());
			if (rimcore1!=null) {
				System.out.printf("bsa/asa cutoff used: %4.2f\n",rimcore1.getBsaToAsaCutoff());
				System.out.println("Core: ");
				System.out.println(rimcore1.getCoreResidues());
				System.out.println("Rim: ");
				System.out.println(rimcore1.getRimResidues());
			} else {
				System.out.println("No core within the cutoffs");
			}
			
			System.out.println("# Second molecule "+mol2.getChainId());
			if (rimcore2!=null) {			
				System.out.printf("bsa/asa cutoff used: %4.1f\n",rimcore2.getBsaToAsaCutoff());
				System.out.println("Core: ");
				System.out.println(rimcore2.getCoreResidues());
				System.out.println("Rim: ");
				System.out.println(rimcore2.getRimResidues());
			} else {
				System.out.println("No core within the cutoffs");
			}

		}
		
		// 4) getting entropies and printing them
		//aln.printProfile(System.out, pdbId);
	}

}
