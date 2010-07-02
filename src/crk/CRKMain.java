package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.xml.sax.SAXException;

import owl.core.connections.pisa.PisaConnection;
import owl.core.connections.pisa.PisaInterface;
import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.structure.CiffilePdb;
import owl.core.structure.Pdb;
import owl.core.structure.PdbCodeNotFoundError;
import owl.core.structure.PdbLoadError;
import owl.core.structure.AminoAcid;

public class CRKMain {
	
	// constants
	private static final String   PROGRAM_NAME = "crk";
	private static final String   ENTROPIES_FILE_SUFFIX = ".entropies";
	private static final String   KAKS_FILE_SUFFIX = ".kaks";
	
	// pdb data location
	private static final String   LOCAL_CIF_DIR = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";
	private static final String   PDB_FTP_CIF_URL = "ftp://ftp.wwpdb.org/pub/pdb/data/structures/all/mmCIF/";
	private static final boolean  ONLINE = false;
	
	// sifts file location
	private static final String   SIFTS_FILE = "/nfs/data/dbs/uniprot/current/pdb_chain_uniprot.lst";
	
	// pisa locations
	private static final String   PISA_INTERFACES_URL = "http://www.ebi.ac.uk/msd-srv/pisa/cgi-bin/interfaces.pisa?";
	
	// blast dirs
	private static final String   BLAST_BIN_DIR = "/home/duarte_j/bin";
	private static final String   BLAST_DB_DIR = "/nfs/data/dbs/uniprot/current";
	private static final String   BLAST_DB = "uniprot_all.fasta"; //"uniprot_sprot.fasta";
	private static final int      DEFAULT_BLAST_NUMTHREADS = 1;
	
	// tcoffee stuff
	private static final File     TCOFFE_BIN = new File("/usr/bin/t_coffee");
	private static final boolean  TCOFFEE_VERYFAST_MODE = true;

	// selecton stuff
	private static final File     SELECTON_BIN = new File("/home/duarte_j/bin/selecton");
	private static final double	  SELECTON_EPSILON = 1000;

	// crk parameters
	// cutoffs
	private static final double   DEFAULT_IDENTITY_CUTOFF = 0.6;
	private static final double   QUERY_COVERAGE_CUTOFF = 0.85;
	private static final int      MIN_HOMOLOGS_CUTOFF = 10;
	private static final double   CUTOFF_ASA_INTERFACE_REPORTING = 350;
	
	// core assignment thresholds
	private static final double   SOFT_CUTOFF_CA = 0.95;
	private static final double   HARD_CUTOFF_CA = 0.82;
	private static final double   RELAX_STEP_CA = 0.01;
	private static final int      MIN_NUM_RES_CA = 6;        // threshold for total sum of 2 members of interface
	private static final int      MIN_NUM_RES_MEMBER_CA = 3; // threshold for each of the member of the interface
		
	// cutoffs for the final bio/xtal call
	protected static final double DEFAULT_ENTR_BIO_CUTOFF = 0.95;
	protected static final double DEFAULT_ENTR_XTAL_CUTOFF = 1.05;
	protected static final double DEFAULT_KAKS_BIO_CUTOFF = 0.79;
	protected static final double DEFAULT_KAKS_XTAL_CUTOFF = 0.81;
	
	// entropy calculation default
	private static final int      DEFAULT_ALPHABET = 20;

	// cache dirs
	private static final String   EMBL_CDS_CACHE_DIR = "/nfs/data/dbs/emblcds_cache";
	private static final String   BLAST_CACHE_DIR = "/nfs/data/dbs/blast_cache/current";


	/**
	 * 
	 * @param args
	 * @throws SQLException
	 * @throws PdbCodeNotFoundError
	 * @throws PdbLoadError
	 * @throws IOException
	 * @throws BlastError
	 * @throws TcoffeeError
	 * @throws SAXException
	 */
	public static void main(String[] args) throws SQLException, PdbCodeNotFoundError, PdbLoadError, IOException, BlastError, TcoffeeError, SAXException {
		
		String pdbCode = null;
		boolean doScoreCRK = false;
		double idCutoff = DEFAULT_IDENTITY_CUTOFF;
		String baseName = null;
		File outDir = new File(".");
		int blastNumThreads = DEFAULT_BLAST_NUMTHREADS;
		int reducedAlphabet = DEFAULT_ALPHABET;

		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -i :  input PDB code\n" +
		"  [-k]:  score based on ka/ks ratios as well as on entropies. Much slower, requires \n" +
		"         running of external program\n" +
		"  [-d]:  sequence identity cut-off, homologs below this threshold won't be considered, \n" +
		"         default: "+String.format("%3.1f",DEFAULT_IDENTITY_CUTOFF)+"\n"+
		"  [-a]:  number of threads for blast. Default: "+DEFAULT_BLAST_NUMTHREADS+"\n"+
		"  [-b]:  basename for output files. Default: PDB code \n"+
		"  [-o]:  output dir, where output files will be written. Default: current dir \n" +
		"  [-r]:  specify the number of groups of aminoacids (reduced alphabet) to be used\n" +
		"         for entropy calculations.\n" +
		"         Valid values are 2, 4, 6, 8, 10, 15 and 20. Default: "+DEFAULT_ALPHABET+"\n\n";


		Getopt g = new Getopt(PROGRAM_NAME, args, "i:kd:a:b:o:r:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				pdbCode = g.getOptarg();
				break;
			case 'k':
				doScoreCRK = true;
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
			case 'r':
				reducedAlphabet = Integer.parseInt(g.getOptarg()); 
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
		
		if (!AminoAcid.isValidNumGroupsReducedAlphabet(reducedAlphabet)) {
			System.err.println("Invalid number of amino acid groups specified ("+reducedAlphabet+")");
			System.exit(1);
		}

		// to throw away jaligner logging
		System.setProperty("java.util.logging.config.file","/dev/null");
		
		// files
		
		File cifFile = getCifFile(pdbCode, ONLINE, outDir);
		Pdb pdb = new CiffilePdb(cifFile);
		String[] chains = pdb.getChains();
		// map of sequences to list of chain codes
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
			i++;
		}
		

		Map<String,ChainEvolContext> allChains = new HashMap<String,ChainEvolContext>();
		for (List<String> entity:uniqSequences.values()) {
			String representativeChain = entity.get(0);
			Map<String,Pdb> pdbs = new HashMap<String,Pdb>();
			for (String pdbChainCode:entity) {
				Pdb perChainPdb = new CiffilePdb(cifFile);
				perChainPdb.load(pdbChainCode);
				pdbs.put(pdbChainCode,perChainPdb);
			}
			ChainEvolContext chainEvCont = new ChainEvolContext(pdbs, representativeChain);
			// 1) getting the uniprot ids corresponding to the query (the pdb sequence)
			chainEvCont.retrieveQueryData(SIFTS_FILE, new File(EMBL_CDS_CACHE_DIR,baseName+"."+pdbCode+representativeChain+".query.emblcds.fa"));
			if (chainEvCont.getQueryRepCDS()==null) {
				System.err.println("No CDS good match for query sequence!! Exiting");
				System.exit(1);
			}
			// 2) getting the homologs and sequence data and creating multiple sequence alignment
			chainEvCont.retrieveHomologs(BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, blastNumThreads, idCutoff, QUERY_COVERAGE_CUTOFF, 
					new File(EMBL_CDS_CACHE_DIR,baseName+"."+pdbCode+representativeChain+".homologs.emblcds.fa"),
					new File(BLAST_CACHE_DIR,baseName+"."+pdbCode+representativeChain+".blast.xml"));
			if (doScoreCRK) {
				if (!chainEvCont.isConsistentGeneticCodeType()){
					System.err.println("The list of homologs does not have a single genetic code type, can't do CRK analysis on it.");
					System.exit(1);
				}
			}
			// align
			chainEvCont.align(TCOFFE_BIN, TCOFFEE_VERYFAST_MODE);
			
			// writing homolog sequences to file
			chainEvCont.writeHomologSeqsToFile(new File(outDir,baseName+"."+pdbCode+representativeChain+".fa"));
			
			// check the back-translation of CDS to uniprot
			// check whether there we have a good enough CDS for the chain
			if (doScoreCRK) {
				System.out.println("Number of homologs with at least one uniprot CDS mapping: "+chainEvCont.getNumHomologsWithCDS());
				System.out.println("Number of homologs with valid CDS: "+chainEvCont.getNumHomologsWithValidCDS());
			}
			
			// printing summary to file
			PrintStream log = new PrintStream(new File(outDir,baseName+"."+pdbCode+representativeChain+".log"));
			chainEvCont.printSummary(log);
			log.close();
			// writing the alignment to file
			chainEvCont.writeAlignmentToFile(new File(outDir,baseName+"."+pdbCode+representativeChain+".aln"));
			// writing the nucleotides alignment to file
			if (doScoreCRK) {
				chainEvCont.writeNucleotideAlignmentToFile(new File(outDir,baseName+"."+pdbCode+representativeChain+".cds.aln"));
			}
			
			// computing entropies
			chainEvCont.computeEntropies(reducedAlphabet);
			
			// compute ka/ks ratios
			if (doScoreCRK) {
				System.out.println("Running selecton (this will take long)...");
				chainEvCont.computeKaKsRatiosSelecton(SELECTON_BIN, 
						new File(outDir,baseName+"."+pdbCode+representativeChain+".selecton.res"),
						new File(outDir,baseName+"."+pdbCode+representativeChain+".selecton.log"), 
						new File(outDir,baseName+"."+pdbCode+representativeChain+".selecton.tree"),
						new File(outDir,baseName+"."+pdbCode+representativeChain+".selecton.global"),
						SELECTON_EPSILON);
			}
			
			// writing the conservation scores (entropies/kaks) log file 
			PrintStream conservScoLog = new PrintStream(new File(outDir,baseName+"."+pdbCode+representativeChain+ENTROPIES_FILE_SUFFIX));
			chainEvCont.printConservationScores(conservScoLog, ScoringType.ENTROPY);
			conservScoLog.close();
			if (doScoreCRK) {
				conservScoLog = new PrintStream(new File(outDir,baseName+"."+pdbCode+representativeChain+KAKS_FILE_SUFFIX));
				chainEvCont.printConservationScores(conservScoLog, ScoringType.KAKS);
				conservScoLog.close();				
			}

			for (String chain:entity) {
				allChains.put(chain,chainEvCont);
			}
		}
		
		
		// 3) getting PISA interfaces description
		PisaConnection pc = new PisaConnection(PISA_INTERFACES_URL, null, null);
		List<String> pdbCodes = new ArrayList<String>();
		pdbCodes.add(pdbCode);
		List<PisaInterface> interfaces = pc.getInterfacesDescription(pdbCodes).get(pdbCode);
		PrintStream pisaLogPS = new PrintStream(new File(outDir,baseName+".pisa.interfaces"));
		for (PisaInterface pi:interfaces) {
			pisaLogPS.println("Interfaces for "+pdbCode);
			pi.printTabular(pisaLogPS);
		}
		pisaLogPS.close();
		
		// 4) scoring
		PrintStream scoreEntrPS = new PrintStream(new File(outDir,baseName+ENTROPIES_FILE_SUFFIX+".scores"));
		printScoringHeaders(System.out);
		printScoringHeaders(scoreEntrPS);
		PrintStream scoreKaksPS = new PrintStream(new File(outDir,baseName+KAKS_FILE_SUFFIX+".scores"));
		printScoringHeaders(scoreKaksPS);

		for (PisaInterface pi:interfaces) {
			if (pi.getInterfaceArea()>CUTOFF_ASA_INTERFACE_REPORTING) {
				ArrayList<ChainEvolContext> chainsEvCs = new ArrayList<ChainEvolContext>();
				chainsEvCs.add(allChains.get(pi.getFirstMolecule().getChainId()));
				chainsEvCs.add(allChains.get(pi.getSecondMolecule().getChainId()));
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, chainsEvCs);
				
				// entropy scoring
				InterfaceScore scoreNW = iec.scoreEntropy(SOFT_CUTOFF_CA, HARD_CUTOFF_CA, RELAX_STEP_CA, MIN_NUM_RES_CA, MIN_NUM_RES_MEMBER_CA,
						MIN_HOMOLOGS_CUTOFF, false);
				InterfaceScore scoreW = iec.scoreEntropy(SOFT_CUTOFF_CA, HARD_CUTOFF_CA, RELAX_STEP_CA, MIN_NUM_RES_CA, MIN_NUM_RES_MEMBER_CA, 
						MIN_HOMOLOGS_CUTOFF, true);
				
				printScores(System.out, pi, scoreNW, scoreW, DEFAULT_ENTR_BIO_CUTOFF, DEFAULT_ENTR_XTAL_CUTOFF);
				printScores(scoreEntrPS, pi, scoreNW, scoreW, DEFAULT_ENTR_BIO_CUTOFF, DEFAULT_ENTR_XTAL_CUTOFF);
				
				scoreNW.serialize(new File(outDir,baseName+"."+pi.getId()+ENTROPIES_FILE_SUFFIX+".scoreNW.dat"));
				scoreW.serialize(new File(outDir,baseName+"."+pi.getId()+ENTROPIES_FILE_SUFFIX+".scoreW.dat"));
				
				// ka/ks scoring
				if (doScoreCRK) {
					scoreNW = iec.scoreKaKs(SOFT_CUTOFF_CA, HARD_CUTOFF_CA, RELAX_STEP_CA, MIN_NUM_RES_CA, MIN_NUM_RES_MEMBER_CA,
						MIN_HOMOLOGS_CUTOFF, false);
					scoreW = iec.scoreKaKs(SOFT_CUTOFF_CA, HARD_CUTOFF_CA, RELAX_STEP_CA, MIN_NUM_RES_CA, MIN_NUM_RES_MEMBER_CA, 
							MIN_HOMOLOGS_CUTOFF, true);
					
					printScores(System.out, pi, scoreNW, scoreW, DEFAULT_KAKS_BIO_CUTOFF, DEFAULT_KAKS_XTAL_CUTOFF);
					printScores(scoreKaksPS, pi, scoreNW, scoreW, DEFAULT_KAKS_BIO_CUTOFF, DEFAULT_KAKS_XTAL_CUTOFF);
					
					scoreNW.serialize(new File(outDir,baseName+"."+pi.getId()+KAKS_FILE_SUFFIX+".scoreNW.dat"));
					scoreW.serialize(new File(outDir,baseName+"."+pi.getId()+KAKS_FILE_SUFFIX+".scoreW.dat"));					
				}
				
				// writing out the interface pdb file with conservation scores as b factors (for visualization)
				iec.writePdbFile(new File(outDir, baseName+"."+pi.getId()+ENTROPIES_FILE_SUFFIX+".pdb"), ScoringType.ENTROPY);
				if (doScoreCRK) {
					iec.writePdbFile(new File(outDir, baseName+"."+pi.getId()+KAKS_FILE_SUFFIX+".pdb"), ScoringType.KAKS);
				}
			}
			
		}
		scoreEntrPS.close();
		scoreKaksPS.close();
	}
	
	private static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		InterfaceMemberScore.printRimAndCoreHeader(ps,1);
		ps.print("\t");
		InterfaceMemberScore.printRimAndCoreHeader(ps,2);
		ps.print("\t");
		InterfaceMemberScore.printHeader(ps,1);
		ps.print("\t");
		InterfaceMemberScore.printHeader(ps,2);
		ps.print("\t");
		InterfaceCall.printHeader(ps);
		ps.print("\t");
		InterfaceMemberScore.printHeader(ps,1);
		ps.print("\t");
		InterfaceMemberScore.printHeader(ps,2);
		ps.print("\t");		
		InterfaceCall.printHeader(ps);
		ps.println();
		//ps.printf("%45s\t%45s\n","non-weighted","weighted");		
	}
	
	private static void printScores(PrintStream ps, PisaInterface pi, InterfaceScore scoreNW, InterfaceScore scoreW, double bioCutoff, double xtalCutoff) {
		ps.printf("%15s\t%6.1f",
				pi.getId()+"("+pi.getFirstMolecule().getChainId()+"+"+pi.getSecondMolecule().getChainId()+")",
				pi.getInterfaceArea());
		scoreNW.getMemberScore(0).printRimAndCoreInfo(ps);
		ps.print("\t");
		scoreNW.getMemberScore(1).printRimAndCoreInfo(ps);
		ps.print("\t");
		scoreNW.getMemberScore(0).printTabular(ps);
		ps.print("\t");
		scoreNW.getMemberScore(1).printTabular(ps);
		ps.print("\t");
		scoreNW.getCall(bioCutoff, xtalCutoff).printTabular(ps);
		ps.print("\t");
		scoreW.getMemberScore(0).printTabular(ps);
		ps.print("\t");
		scoreW.getMemberScore(1).printTabular(ps);
		ps.print("\t");
		scoreW.getCall(bioCutoff, xtalCutoff).printTabular(ps);
		ps.println();		
	}
	
	private static File getCifFile(String pdbCode, boolean online, File outDir) {
		File cifFile = new File(outDir,pdbCode + ".cif");
		String gzCifFileName = pdbCode+".cif.gz";
		File gzCifFile = null;
		if (!online) {	
			gzCifFile = new File(LOCAL_CIF_DIR,gzCifFileName);
		} else {
			gzCifFile = new File(outDir, gzCifFileName);
			try {
				System.out.println("Downloading cif file from ftp...");
				// getting gzipped cif file from ftp
				URL url = new URL(PDB_FTP_CIF_URL+gzCifFileName);
				URLConnection urlc = url.openConnection();
				InputStream is = urlc.getInputStream();
				FileOutputStream os = new FileOutputStream(gzCifFile);
				int b;
				while ( (b=is.read())!=-1) {
					os.write(b);
				}
				is.close();
				os.close();
			} catch (IOException e) {
				System.err.println("Couldn't get "+gzCifFileName+" file from ftp.");
				System.err.println(e.getMessage());
				System.exit(1);
			}
		} 

		// unzipping file
		try {
			GZIPInputStream zis = new GZIPInputStream(new FileInputStream(gzCifFile));
			FileOutputStream os = new FileOutputStream(cifFile);
			int b;
			while ( (b=zis.read())!=-1) {
				os.write(b);
			}
			zis.close();
			os.close();
		} catch (IOException e) {
			System.err.println("Couldn't uncompress "+gzCifFile+" file into "+cifFile);
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return cifFile;
	}

}
