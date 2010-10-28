package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.xml.sax.SAXException;

import owl.core.connections.pisa.PisaConnection;
import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbCodeNotFoundError;
import owl.core.structure.PdbLoadError;
import owl.core.structure.AminoAcid;
import owl.core.util.FileFormatError;

public class CRKMain {
	
	// CONSTANTS
	private static final String   PROGRAM_NAME = "crk";
	private static final String   CONFIG_FILE_NAME = ".crk.conf";
	private static final String   ENTROPIES_FILE_SUFFIX = ".entropies";
	private static final String   KAKS_FILE_SUFFIX = ".kaks";
	private static final Pattern  PDBCODE_PATTERN = Pattern.compile("^\\d\\w\\w\\w$");
	private static final double   INTERFACE_DIST_CUTOFF = 5.9;
	private static final Pattern  NONPROT_PATTERN = Pattern.compile("^X+$");
	
	// DEFAULTS FOR CONFIG FILE ASSIGNABLE CONSTANTS
	// defaults for pdb data location
	private static final String   DEF_LOCAL_CIF_DIR = "/pdbdata/pdb/data/structures/all/mmCIF";
	private static final String   DEF_PDB_FTP_CIF_URL = "ftp://ftp.wwpdb.org/pub/pdb/data/structures/all/mmCIF/";
	private static final boolean  DEF_USE_ONLINE_PDB = false;

	// defaults for pisa locations
	private static final String   DEF_PISA_INTERFACES_URL = "http://www.ebi.ac.uk/msd-srv/pisa/cgi-bin/interfaces.pisa?";

	// default sifts file location
	private static final String   DEF_SIFTS_FILE = "ftp://ftp.ebi.ac.uk/pub/databases/msd/sifts/text/pdb_chain_uniprot.lst";	
	
	// default blast settings
	private static final String   DEF_BLAST_BIN_DIR = "/usr/bin";
	
	// default tcoffee settings
	private static final File     DEF_TCOFFE_BIN = new File("/usr/bin/t_coffee");

	// default selecton stuff
	private static final File     DEF_SELECTON_BIN = new File("/usr/bin/selecton");
	private static final double	  DEF_SELECTON_EPSILON = 0.1;

	// default naccess location
	private static final File     DEF_NACCESS_EXE = new File("/usr/bin/naccess");
	
	// default crk cutoffs
	private static final double   DEF_QUERY_COVERAGE_CUTOFF = 0.85;
	private static final int      DEF_MIN_HOMOLOGS_CUTOFF = 10;
	private static final double   DEF_CUTOFF_ASA_INTERFACE_REPORTING = 350;
		
	// cutoffs for the final bio/xtal call
	private static final double   DEF_ENTR_BIO_CUTOFF = 0.95;
	private static final double   DEF_ENTR_XTAL_CUTOFF = 1.05;
	private static final double   DEF_KAKS_BIO_CUTOFF = 0.84;
	private static final double   DEF_KAKS_XTAL_CUTOFF = 0.86;
	
	// default cache dirs
	private static final String   DEF_EMBL_CDS_CACHE_DIR = null;
	private static final String   DEF_BLAST_CACHE_DIR = null;

	// DEFAULTS FOR COMMAND LINE PARAMETERS
	private static final double   DEF_IDENTITY_CUTOFF = 0.6;

	private static final int      DEF_NUMTHREADS = Runtime.getRuntime().availableProcessors();
	
	// default entropy calculation default
	private static final int      DEF_ENTROPY_ALPHABET = 10;

	// default crk core assignment thresholds
	//private static final double   DEF_SOFT_CUTOFF_CA = 0.95;
	//private static final double   DEF_HARD_CUTOFF_CA = 0.82;
	//private static final double   DEF_RELAX_STEP_CA = 0.01;	
	private static final double[] DEF_CA_CUTOFFS = {0.85, 0.90, 0.95};
	private static final int      DEF_MIN_NUM_RES_CA = 6;
	private static final int      DEF_MIN_NUM_RES_MEMBER_CA = 3; 

	private static final boolean  DEF_USE_TCOFFEE_VERYFAST_MODE = true;
	
	private static final int      DEF_MAX_NUM_SEQUENCES_SELECTON = 60;
	
	private static final int      DEF_NSPHEREPOINTS_ASA_CALC = 960;
	
	// GLOBAL VARIABLES ASSIGNABLE FROM CONFIG FILE
	private static String   LOCAL_CIF_DIR;
	private static String   PDB_FTP_CIF_URL;
	private static boolean  USE_ONLINE_PDB;
	
	private static String   PISA_INTERFACES_URL;
	
	private static String   SIFTS_FILE;
	
	private static String   BLAST_BIN_DIR;
	
	private static File     TCOFFEE_BIN;
	
	private static File     SELECTON_BIN;

	private static File     NACCESS_EXE;
	
	private static double   QUERY_COVERAGE_CUTOFF;
	private static int      MIN_HOMOLOGS_CUTOFF;
	private static double   CUTOFF_ASA_INTERFACE_REPORTING; 
			
	private static double 	ENTR_BIO_CUTOFF;
	private static double 	ENTR_XTAL_CUTOFF;
	private static double 	KAKS_BIO_CUTOFF;
	private static double 	KAKS_XTAL_CUTOFF;
	
	private static String   EMBL_CDS_CACHE_DIR;
	private static String   BLAST_CACHE_DIR;

	// and finally the ones with no defaults
	private static String   BLAST_DB_DIR; // no default
	private static String   BLAST_DB;     // no default

	
	
	// THE ROOT LOGGER (log4j)
	private static final Logger ROOTLOGGER = Logger.getRootLogger();
	private static final Log LOGGER = LogFactory.getLog(CRKMain.class);
	

	
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
	public static void main(String[] args) throws PdbLoadError, IOException, BlastError, TcoffeeError, SAXException {
		
		String pdbCode = null;
		boolean doScoreCRK = false;
		double idCutoff = DEF_IDENTITY_CUTOFF;
		String baseName = null;
		File outDir = new File(".");
		int numThreads = DEF_NUMTHREADS;
		int reducedAlphabet = DEF_ENTROPY_ALPHABET;
		boolean useTcoffeeVeryFastMode = DEF_USE_TCOFFEE_VERYFAST_MODE;
		
		double[] cutoffsCA  = DEF_CA_CUTOFFS;
		String defCACutoffsStr = String.format("%4.2f",DEF_CA_CUTOFFS[0]);
		for (int i=1;i<DEF_CA_CUTOFFS.length;i++) {
			defCACutoffsStr += String.format(",%4.2f",DEF_CA_CUTOFFS[i]);
		}
		
		int      minNumResCA  = DEF_MIN_NUM_RES_CA;
		int      minNumResMemberCA = DEF_MIN_NUM_RES_MEMBER_CA; 
		
		double selectonEpsilon = DEF_SELECTON_EPSILON;

		int maxNumSeqsSelecton = DEF_MAX_NUM_SEQUENCES_SELECTON;
		
		boolean usePisa = false;

		boolean useNaccess = false;
		
		int nSpherePointsASAcalc = DEF_NSPHEREPOINTS_ASA_CALC;
		
		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -i          :  input PDB code or PDB file or mmCIF file\n" +
		"  [-k]         :  score based on ka/ks ratios as well as on entropies. Much \n" +
		"                  slower, requires running of the selecton external program\n" +
		"  [-d <float>] :  sequence identity cut-off, homologs below this threshold won't\n" +
		"                  be considered, default: "+String.format("%3.1f",DEF_IDENTITY_CUTOFF)+"\n"+
		"  [-a <int>]   :  number of threads for blast and ASA calculation. Default: "+DEF_NUMTHREADS+"\n"+
		"  [-b <str>]   :  basename for output files. Default: PDB code \n"+
		"  [-o <dir>]   :  output dir, where output files will be written. Default: current\n" +
		"                  dir \n" +
		"  [-r <int>]   :  specify the number of groups of aminoacids (reduced alphabet) to\n" +
		"                  be used for entropy calculations.\n" +
		"                  Valid values are 2, 4, 6, 8, 10, 15 and 20. Default: "+DEF_ENTROPY_ALPHABET+"\n" +
		"  [-t]         :  if specified t_coffee will be run in normal mode instead of very\n" +
		"                  fast mode\n" +
		"  [-c <floats>]:  comma separated list of BSA cutoffs for core assignment. Default: "+defCACutoffsStr+"\n" +
		"  [-m <int>]   :  cutoff for number of interface core residues, if still below \n" +
		"                  this value after applying hard cutoff then the interface is not\n" +
		"                  scored and considered a crystal contact. Default "+DEF_MIN_NUM_RES_CA+"\n" +
		"  [-M <int>]   :  cutoff for number of interface member core residues, if still \n" +
		"                  below this value after applying hard cutoff then the interface \n" +
		"                  member is not scored and considerd a crystal contact. Default: "+DEF_MIN_NUM_RES_MEMBER_CA+"\n" +
		"  [-e <float>] :  epsilon value for selecton. Default "+String.format("%4.2f",DEF_SELECTON_EPSILON)+"\n" +
		"  [-q <int>]   :  maximum number of sequences to keep for calculation of conservation \n" +
		"                  scores. Default: "+DEF_MAX_NUM_SEQUENCES_SELECTON+". This is especially important when using \n" +
		"                  the -k option, with too many sequences, selecton will run too long\n" +
		"                  (and inaccurately because of ks saturation)\n" +
		"  [-p]         :  use PISA interface enumeration (will be downloaded from web) \n" +
		"                  instead of ours (only possible for existing PDB entries).\n" +
		"  [-n]         :  use NACCESS for ASA/BSA calculations, otherwise area calculations \n" +
		"                  done with the internal rolling ball algorithm implementation \n" +
		"                  (multi-threaded using number of CPUs specified in -a)\n" +
		"  [-A <int>]   :  number of sphere points for ASA calculation, this parameter controls\n" +
		"                  the accuracy of the ASA calculations, the bigger the more accurate \n" +
		"                  (and slower). Default: "+DEF_NSPHEREPOINTS_ASA_CALC+"\n\n";
		


		Getopt g = new Getopt(PROGRAM_NAME, args, "i:kd:a:b:o:r:tc:m:M:e:q:pnA:h?");
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
				numThreads = Integer.parseInt(g.getOptarg());
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
			case 't':
				useTcoffeeVeryFastMode = false;
				break;
			case 'c':
				String[] tokens = g.getOptarg().split(",");
				cutoffsCA = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					cutoffsCA[i] = Double.parseDouble(tokens[i]);
				}
				break;
			case 'm':
				minNumResCA = Integer.parseInt(g.getOptarg());
				break;
			case 'M':
				minNumResMemberCA = Integer.parseInt(g.getOptarg());
				break;
			case 'e':
				selectonEpsilon = Double.parseDouble(g.getOptarg());
				break;
			case 'q':
				maxNumSeqsSelecton = Integer.parseInt(g.getOptarg());
				break;
			case 'p':
				usePisa = true;
				break;
			case 'n':
				useNaccess = true;
				break;
			case 'A':
				nSpherePointsASAcalc = Integer.parseInt(g.getOptarg());
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
		
		File inFile = new File(pdbCode);
		Matcher m = PDBCODE_PATTERN.matcher(pdbCode);
		if (m.matches()) {
			inFile = null;
		}
		
		if (inFile!=null && !inFile.exists()){
			System.err.println("Given file "+inFile+" does not exist!");
			System.exit(1);
		}
		
		if (baseName==null) {
			baseName=pdbCode;
			if (inFile!=null) {
				baseName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
			}
		}
		
		if (!AminoAcid.isValidNumGroupsReducedAlphabet(reducedAlphabet)) {
			System.err.println("Invalid number of amino acid groups specified ("+reducedAlphabet+")");
			System.exit(1);
		}
		
		if (usePisa && inFile!=null) {
			System.err.println("Can only get PISA interface enumeration for a PDB code. Can't use '-p' if the PDB given is a file");
			System.exit(1);
		}

		// turn off jaligner logging (we only use NeedlemanWunschGotoh)
		// (for some reason this doesn't work if condensated into one line, it seems that one needs to instantiate the logger and then call setLevel)
		// (and even weirder, for some reason it doesn't work if you put the code in its own private static method!)
		java.util.logging.Logger jalLogger = java.util.logging.Logger.getLogger("NeedlemanWunschGotoh");
		jalLogger.setLevel(java.util.logging.Level.OFF);
		
		// setting up the file logger for log4j
	    ROOTLOGGER.addAppender(new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p - %m%n"),outDir+"/"+baseName+".log",false));
	    ROOTLOGGER.setLevel(Level.INFO);

	    // TODO now using the apache common logging framework which acts as a meta framework for other frameworks such
	    //      as log4j or java logging. We always should instantiate loggers from apache commons (LogFactory.getLog()).
	    //      Left to do is still configure the logging in CRK properly by using the apache commons mechanism. At the moment
	    //      the logging configuration is done using log4j/java logger mechanisms.
		
		loadConfigFile();
		
		
		try {

			PdbAsymUnit pdb = null;
			String pdbName = pdbCode; // the name to be used in many of the output files
			File cifFile = null; // if given a pdb code in command line we will store the cif file here, we need to use it later if interf desc downloaded from PISA
			
			try {
				if (inFile==null) {
					cifFile = new File(outDir,pdbCode + ".cif");
					PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, PDB_FTP_CIF_URL, pdbCode, cifFile, USE_ONLINE_PDB);
					pdb = new PdbAsymUnit(cifFile);	
				} else {
					pdb = new PdbAsymUnit(inFile);
					pdbName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
				}
			} catch (IOException e) {
				System.err.println("Couldn't get cif file for code "+pdbCode+" from ftp or couldn't uncompress it.");
				System.err.println(e.getMessage());
				System.exit(1);
			} catch (FileFormatError e) {
				LOGGER.error("File format error: "+e.getMessage());
				System.exit(1);
			} 
			
			if (pdb.getCrystalCell()==null) {
				LOGGER.fatal("No crystal information found in source "+pdbCode);
				System.exit(1);
			}
			
			Map<String, List<String>> uniqSequences = pdb.getUniqueSequences();
			String msg = "Unique sequences for "+pdbName+":";
			int i = 1;
			for (List<String> entity:uniqSequences.values()) {
				msg+=" "+i+":";
				for (String chain:entity) {
					msg+=" "+chain;
				}
				i++;
			}
			LOGGER.info(msg);

			Map<String,ChainEvolContext> allChains = new HashMap<String,ChainEvolContext>();
			for (String seq:uniqSequences.keySet()) {
				List<String> entity = uniqSequences.get(seq);
				String representativeChain = entity.get(0);
				
				Matcher nonprotMatcher = NONPROT_PATTERN.matcher(seq);
				if (nonprotMatcher.matches()) {
					LOGGER.warn("Representative chain "+representativeChain+" does not seem to be a protein chain. Won't analyse it.");
					continue;
				}
	
				ChainEvolContext chainEvCont = new ChainEvolContext(pdb, representativeChain, pdbName);
				// 1) getting the uniprot ids corresponding to the query (the pdb sequence)
				File emblQueryCacheFile = null;
				if (EMBL_CDS_CACHE_DIR!=null) {
					emblQueryCacheFile = new File(EMBL_CDS_CACHE_DIR,baseName+"."+pdbName+representativeChain+".query.emblcds.fa");
				}
				System.out.println("Finding query's uniprot mapping (through SIFTS or blasting)");
				chainEvCont.retrieveQueryData(SIFTS_FILE, emblQueryCacheFile, BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, numThreads,doScoreCRK);
				if (doScoreCRK && chainEvCont.getQueryRepCDS()==null) {
					// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
					LOGGER.error("No CDS good match for query sequence! can't do CRK analysis on it.");
				}
				// 2) getting the homologs and sequence data and creating multiple sequence alignment
				System.out.println("Blasting for homologues...");
				File blastCacheFile = null;
				if (BLAST_CACHE_DIR!=null) {
					blastCacheFile = new File(BLAST_CACHE_DIR,baseName+"."+pdbName+representativeChain+".blast.xml"); 
				}
				try {
					chainEvCont.retrieveHomologs(BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, numThreads, idCutoff, QUERY_COVERAGE_CUTOFF, blastCacheFile);
				} catch (UniprotVerMisMatchException e) {
					LOGGER.error(e.getMessage());
					System.err.println("Mismatch of Uniprot versions! Exiting.");
					System.err.println(e.getMessage());
					System.exit(1);					
				}

				System.out.println("Retrieving UniprotKB data and EMBL CDS sequences");
				File emblHomsCacheFile = null;
				if (EMBL_CDS_CACHE_DIR!=null) {
					emblHomsCacheFile = new File(EMBL_CDS_CACHE_DIR,baseName+"."+pdbName+representativeChain+".homologs.emblcds.fa");
				}
				try {
					chainEvCont.retrieveHomologsData(emblHomsCacheFile, doScoreCRK);
				} catch (UniprotVerMisMatchException e) {
					LOGGER.error(e.getMessage());
					System.err.println("Mismatch of Uniprot versions! Exiting.");
					System.err.println(e.getMessage());
					System.exit(1);
				}
				if (doScoreCRK && !chainEvCont.isConsistentGeneticCodeType()){
					// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
					LOGGER.error("The list of homologs does not have a single genetic code type, can't do CRK analysis on it.");
				}
				// remove redundancy
				chainEvCont.removeRedundancy();

				// skimming so that there's not too many sequences for selecton
				chainEvCont.skimList(maxNumSeqsSelecton);
				
				// align
				System.out.println("Aligning protein sequences with t_coffee...");
				chainEvCont.align(TCOFFEE_BIN, useTcoffeeVeryFastMode);

				// writing homolog sequences to file
				chainEvCont.writeHomologSeqsToFile(new File(outDir,baseName+"."+pdbName+representativeChain+".fa"));

				// check the back-translation of CDS to uniprot
				// check whether we have a good enough CDS for the chain
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					LOGGER.info("Number of homologs with at least one uniprot CDS mapping: "+chainEvCont.getNumHomologsWithCDS());
					LOGGER.info("Number of homologs with valid CDS: "+chainEvCont.getNumHomologsWithValidCDS());
				}

				// printing summary to file
				PrintStream log = new PrintStream(new File(outDir,baseName+"."+pdbName+representativeChain+".log"));
				chainEvCont.printSummary(log);
				log.close();
				// writing the alignment to file
				chainEvCont.writeAlignmentToFile(new File(outDir,baseName+"."+pdbName+representativeChain+".aln"));
				// writing the nucleotides alignment to file
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					chainEvCont.writeNucleotideAlignmentToFile(new File(outDir,baseName+"."+pdbName+representativeChain+".cds.aln"));
				}

				// computing entropies
				chainEvCont.computeEntropies(reducedAlphabet);

				// compute ka/ks ratios
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					System.out.println("Running selecton (this will take long)...");
					chainEvCont.computeKaKsRatiosSelecton(SELECTON_BIN, 
							new File(outDir,baseName+"."+pdbName+representativeChain+".selecton.res"),
							new File(outDir,baseName+"."+pdbName+representativeChain+".selecton.log"), 
							new File(outDir,baseName+"."+pdbName+representativeChain+".selecton.tree"),
							new File(outDir,baseName+"."+pdbName+representativeChain+".selecton.global"),
							selectonEpsilon);
				}

				// writing the conservation scores (entropies/kaks) log file 
				PrintStream conservScoLog = new PrintStream(new File(outDir,baseName+"."+pdbName+representativeChain+ENTROPIES_FILE_SUFFIX));
				chainEvCont.printConservationScores(conservScoLog, ScoringType.ENTROPY);
				conservScoLog.close();
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					conservScoLog = new PrintStream(new File(outDir,baseName+"."+pdbName+representativeChain+KAKS_FILE_SUFFIX));
					chainEvCont.printConservationScores(conservScoLog, ScoringType.KAKS);
					conservScoLog.close();				
				}

				for (String chain:entity) {
					allChains.put(chain,chainEvCont);
				}
			}


			// 3) getting interfaces 
			ChainInterfaceList interfaces = null;
			if (usePisa) {
				System.out.println("Getting PISA interfaces...");
				LOGGER.info("Interfaces from PISA.");
				PisaConnection pc = new PisaConnection(PISA_INTERFACES_URL, null, null);
				List<String> pdbCodes = new ArrayList<String>();
				pdbCodes.add(pdbCode);
				interfaces = pc.getInterfacesDescription(pdbCodes).get(pdbCode);
			} else {
				System.out.println("Calculating possible interfaces...");
				LOGGER.info("Interfaces calculated.");
				if (useNaccess) {
					interfaces = pdb.getAllInterfaces(INTERFACE_DIST_CUTOFF, NACCESS_EXE, 0, 0);
				} else {
					interfaces = pdb.getAllInterfaces(INTERFACE_DIST_CUTOFF, null, nSpherePointsASAcalc, numThreads);
				}
			}
			
			if (!usePisa && interfaces.hasInterfacesWithClashes()) {
				LOGGER.error("Clashes found in some of the interfaces. This is most likely an error in the structure. If you think the structure is correct, please report a bug.");
				System.err.println("Clashes found in some of the interfaces. This is most likely an error in the structure. If you think the structure is correct, please report a bug.");
				System.exit(1);
			}
			
			interfaces.calcRimAndCores(cutoffsCA);
			
			System.out.println("Done");
			
			PrintStream interfLogPS = new PrintStream(new File(outDir,baseName+".interfaces"));
			interfaces.printTabular(interfLogPS, pdbName);
			interfLogPS.close();

			// 4) scoring
			System.out.println("Scores:");

			InterfaceEvolContextList iecList = new InterfaceEvolContextList(MIN_HOMOLOGS_CUTOFF, minNumResCA, minNumResMemberCA, 
					idCutoff, QUERY_COVERAGE_CUTOFF, maxNumSeqsSelecton);
			for (ChainInterface pi:interfaces) {
				ArrayList<ChainEvolContext> chainsEvCs = new ArrayList<ChainEvolContext>();
				chainsEvCs.add(allChains.get(pi.getFirstMolecule().getPdbChainCode()));
				chainsEvCs.add(allChains.get(pi.getSecondMolecule().getPdbChainCode()));
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, chainsEvCs);
				iecList.add(iec);
			}

			PrintStream scoreEntrPS = new PrintStream(new File(outDir,baseName+ENTROPIES_FILE_SUFFIX+".scores"));
			// entropy nw
			iecList.scoreEntropy(false);
			iecList.printScoresTable(System.out, ENTR_BIO_CUTOFF, ENTR_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
			iecList.printScoresTable(scoreEntrPS, ENTR_BIO_CUTOFF, ENTR_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
			// entropy w
			iecList.scoreEntropy(true);
			iecList.printScoresTable(System.out, ENTR_BIO_CUTOFF, ENTR_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
			iecList.printScoresTable(scoreEntrPS, ENTR_BIO_CUTOFF, ENTR_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
			iecList.writeScoresPDBFiles(outDir, baseName, ENTROPIES_FILE_SUFFIX+".pdb", CUTOFF_ASA_INTERFACE_REPORTING);
			iecList.writeRimCorePDBFiles(outDir, baseName, ".rimcore.pdb", CUTOFF_ASA_INTERFACE_REPORTING);
			scoreEntrPS.close();

			
			// ka/ks scoring
			if (doScoreCRK) {
				PrintStream scoreKaksPS = new PrintStream(new File(outDir,baseName+KAKS_FILE_SUFFIX+".scores"));
				// kaks nw
				iecList.scoreKaKs(false);
				iecList.printScoresTable(System.out,  KAKS_BIO_CUTOFF, KAKS_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
				iecList.printScoresTable(scoreKaksPS,  KAKS_BIO_CUTOFF, KAKS_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
				// kaks w
				iecList.scoreKaKs(true);
				iecList.printScoresTable(System.out,  KAKS_BIO_CUTOFF, KAKS_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
				iecList.printScoresTable(scoreKaksPS,  KAKS_BIO_CUTOFF, KAKS_XTAL_CUTOFF, CUTOFF_ASA_INTERFACE_REPORTING);
				iecList.writeScoresPDBFiles(outDir, baseName, KAKS_FILE_SUFFIX+".pdb", CUTOFF_ASA_INTERFACE_REPORTING);
				iecList.writeRimCorePDBFiles(outDir, baseName, ".rimcore.pdb", CUTOFF_ASA_INTERFACE_REPORTING);
				scoreKaksPS.close();
			}
			

		} catch (Exception e) {
			e.printStackTrace();

			String stack = "";
			for (StackTraceElement el:e.getStackTrace()) {
				stack+="\tat "+el.toString()+"\n";				
			}
			LOGGER.fatal("Unexpected error. Exiting.\n"+e+"\n"+stack);
			System.exit(1);
		}
	}

	private static Properties loadConfigFile(String fileName) throws FileNotFoundException, IOException {
		Properties p = new Properties();
		p.load(new FileInputStream(fileName));
		return p;
	}
	
	private static void applyUserProperties(Properties p) {

		/* The logic here is: First, take the value from the user config file,
		   if that is not found, keep the variable value unchanged.
		   Note that any value in the user config file that is not being processed here is ignored. 
		*/
		try {
			// variables without defaults
			BLAST_DB_DIR    	= p.getProperty("BLAST_DB_DIR");
			BLAST_DB        	= p.getProperty("BLAST_DB");

			LOCAL_CIF_DIR   	= p.getProperty("LOCAL_CIF_DIR", DEF_LOCAL_CIF_DIR);
			PDB_FTP_CIF_URL 	= p.getProperty("PDB_FTP_URL", DEF_PDB_FTP_CIF_URL);
			USE_ONLINE_PDB  	= Boolean.parseBoolean(p.getProperty("USE_ONLINE_PDB", new Boolean(DEF_USE_ONLINE_PDB).toString()));
			
			PISA_INTERFACES_URL = p.getProperty("PISA_INTERFACES_URL", DEF_PISA_INTERFACES_URL);
			
			SIFTS_FILE          = p.getProperty("SIFTS_FILE", DEF_SIFTS_FILE);
			
			BLAST_BIN_DIR       = p.getProperty("BLAST_BIN_DIR", DEF_BLAST_BIN_DIR);
			
			TCOFFEE_BIN 		= new File(p.getProperty("TCOFFEE_BIN", DEF_TCOFFE_BIN.toString()));
			
			SELECTON_BIN 		= new File(p.getProperty("SELECTON_BIN", DEF_SELECTON_BIN.toString()));
			
			NACCESS_EXE         = new File(p.getProperty("NACCESS_EXE", DEF_NACCESS_EXE.toString()));

			QUERY_COVERAGE_CUTOFF = Double.parseDouble(p.getProperty("QUERY_COVERAGE_CUTOFF", new Double(DEF_QUERY_COVERAGE_CUTOFF).toString()));
			MIN_HOMOLOGS_CUTOFF = Integer.parseInt(p.getProperty("MIN_HOMOLOGS_CUTOFF", new Integer(DEF_MIN_HOMOLOGS_CUTOFF).toString()));
			CUTOFF_ASA_INTERFACE_REPORTING = Double.parseDouble(p.getProperty("CUTOFF_ASA_INTERFACE_REPORTING", new Double(DEF_CUTOFF_ASA_INTERFACE_REPORTING).toString())); 
					
			ENTR_BIO_CUTOFF     = Double.parseDouble(p.getProperty("ENTR_BIO_CUTOFF",new Double(DEF_ENTR_BIO_CUTOFF).toString()));
			ENTR_XTAL_CUTOFF    = Double.parseDouble(p.getProperty("ENTR_XTAL_CUTOFF",new Double(DEF_ENTR_XTAL_CUTOFF).toString()));
			KAKS_BIO_CUTOFF     = Double.parseDouble(p.getProperty("KAKS_BIO_CUTOFF",new Double(DEF_KAKS_BIO_CUTOFF).toString()));
			KAKS_XTAL_CUTOFF    = Double.parseDouble(p.getProperty("KAKS_XTAL_CUTOFF",new Double(DEF_KAKS_XTAL_CUTOFF).toString()));;
			
			EMBL_CDS_CACHE_DIR  = p.getProperty("EMBL_CDS_CACHE_DIR", DEF_EMBL_CDS_CACHE_DIR);
			BLAST_CACHE_DIR     = p.getProperty("BLAST_CACHE_DIR", DEF_BLAST_CACHE_DIR);

		} catch (NumberFormatException e) {
			System.err.println("A numerical value in the config file was incorrectly specified: "+e.getMessage()+".\n" +
					"Please check the config file.");
			System.exit(1);
		}
	}
		
	private static void loadConfigFile() {
		// loading settings from config file
		File userConfigFile = new File(System.getProperty("user.home"),CONFIG_FILE_NAME);  
		try {
			if (userConfigFile.exists()) {
				LOGGER.info("Loading user configuration file " + userConfigFile);
				applyUserProperties(loadConfigFile(userConfigFile.getAbsolutePath()));
			}
		} catch (IOException e) {
			System.err.println("Error while reading from config file " + userConfigFile + ": " + e.getMessage());
			System.exit(1);
		}

	}
}
