package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
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
	private static final double   DEF_MIN_INTERF_AREA_REPORTING = 300;
		
	// default cache dirs
	private static final String   DEF_EMBL_CDS_CACHE_DIR = null;
	private static final String   DEF_BLAST_CACHE_DIR = null;
	
	// default clash distance: in theory, a disulfide bond distance (2.05) is the minimum distance we could reasonably expect
	private static final double   DEF_INTERCHAIN_ATOM_CLASH_DISTANCE = 1.5; 
	
	// DEFAULTS FOR COMMAND LINE PARAMETERS
	private static final double   DEF_IDENTITY_CUTOFF = 0.6;

	private static final int      DEF_NUMTHREADS = Runtime.getRuntime().availableProcessors();
	
	// default entropy calculation default
	private static final int      DEF_ENTROPY_ALPHABET = 10;

	// default cutoffs for the final bio/xtal call
	private static final double   DEF_GRAY_ZONE_WIDTH = 0.01;
	private static final double   DEF_ENTR_CALL_CUTOFF = 1.00;
	private static final double   DEF_KAKS_CALL_CUTOFF = 0.85;
	
	// default crk core assignment thresholds
	private static final double   DEF_SOFT_CUTOFF_CA = 0.95;
	private static final double   DEF_HARD_CUTOFF_CA = 0.82;
	private static final double   DEF_RELAX_STEP_CA = 0.01;	
	private static final double[] DEF_CA_CUTOFFS = {0.85, 0.90, 0.95};
	private static final int      DEF_MIN_NUM_RES_CA = 6;
	private static final int      DEF_MIN_NUM_RES_MEMBER_CA = 3; 

	private static final boolean  DEF_USE_TCOFFEE_VERYFAST_MODE = true;
	
	private static final int      DEF_MAX_NUM_SEQUENCES_SELECTON = 60;
	
	private static final int      DEF_NSPHEREPOINTS_ASA_CALC = 9600;
	
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
	private static double   MIN_INTERF_AREA_REPORTING; 
			
	private static String   EMBL_CDS_CACHE_DIR;
	private static String   BLAST_CACHE_DIR;
	
	private static double   INTERCHAIN_ATOM_CLASH_DISTANCE;

	// and finally the ones with no defaults
	private static String   BLAST_DB_DIR; // no default
	private static String   BLAST_DB;     // no default

	
	
	// THE ROOT LOGGER (log4j)
	private static final Logger ROOTLOGGER = Logger.getRootLogger();
	private static final Log LOGGER = LogFactory.getLog(CRKMain.class);
	

	
	/**
	 * The main of CRK 
	 */
	public static void main(String[] args) throws IOException {
		
		String pdbCode = null;
		boolean doScoreCRK = false;
		double idCutoff = DEF_IDENTITY_CUTOFF;
		String baseName = null;
		File outDir = new File(".");
		int numThreads = DEF_NUMTHREADS;
		int reducedAlphabet = DEF_ENTROPY_ALPHABET;
		boolean useTcoffeeVeryFastMode = DEF_USE_TCOFFEE_VERYFAST_MODE;
		
		boolean zooming = false;
		
		double bsaToAsaSoftCutoff = DEF_SOFT_CUTOFF_CA;
		double bsaToAsaHardCutoff = DEF_HARD_CUTOFF_CA;
		double relaxationStep = DEF_RELAX_STEP_CA;
		
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

		double grayZoneWidth = DEF_GRAY_ZONE_WIDTH;
		double[] entrCallCutoff  = {DEF_ENTR_CALL_CUTOFF};
		double[] kaksCallCutoff  = {DEF_KAKS_CALL_CUTOFF};

		
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
		"  [-c <floats>]:  comma separated list of BSA cutoffs for core assignment. Default: \n"+
		"                  "+defCACutoffsStr+"\n" +
		"  [-z]         :  use zooming for core assignment\n"+
		"  [-Z <floats>]:  set parameters for zooming (only used if -z specified). Specify 3 \n" +
		"                  comma separated values: soft BSA cutoff, hard BSA cutoff and \n" +
		"                  relaxation step. Default: "+DEF_SOFT_CUTOFF_CA+","+DEF_HARD_CUTOFF_CA+","+DEF_RELAX_STEP_CA+"\n"+
		"  [-m <int>]   :  cutoff for number of interface core residues, if still below \n" +
		"                  this value after applying hard cutoff then the interface is not\n" +
		"                  scored and considered a crystal contact. Default "+DEF_MIN_NUM_RES_CA+"\n" +
		"  [-M <int>]   :  cutoff for number of interface member core residues, if still \n" +
		"                  below this value after applying hard cutoff then the interface \n" +
		"                  member is not scored and considered a crystal contact. Default: "+DEF_MIN_NUM_RES_MEMBER_CA+"\n" +
		"  [-x <floats>]:  comma separated list of entropy score cutoffs for calling BIO/XTAL.\n" +
		"                  Default: " + String.format("%4.2f",DEF_ENTR_CALL_CUTOFF)+"\n"+
		"  [-X <floats>]:  comma separated list of ka/ks score cutoffs for calling BIO/XTAL.\n"+
		"                  Default: " + String.format("%4.2f",DEF_KAKS_CALL_CUTOFF)+"\n"+
		"  [-g <float>] :  a margin to be added around the score cutoffs for calling BIO/XTAL\n" +
		"                  defining an undetermined (gray) prediction zone. Default: "+String.format("%4.2f",DEF_GRAY_ZONE_WIDTH)+"\n"+
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
		


		Getopt g = new Getopt(PROGRAM_NAME, args, "i:kd:a:b:o:r:tc:zZ:m:M:x:X:g:e:q:pnA:h?");
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
				String[] ctokens = g.getOptarg().split(",");
				cutoffsCA = new double[ctokens.length];
				for (int i=0;i<ctokens.length;i++) {
					cutoffsCA[i] = Double.parseDouble(ctokens[i]);
				}
				break;
			case 'z':
				zooming = true;
				break;
			case 'Z':
				String[] ztokens = g.getOptarg().split(",");
				cutoffsCA = new double[ztokens.length];
				bsaToAsaSoftCutoff = Double.parseDouble(ztokens[0]);
				bsaToAsaHardCutoff = Double.parseDouble(ztokens[1]);
				relaxationStep = Double.parseDouble(ztokens[2]);
				break;
			case 'm':
				minNumResCA = Integer.parseInt(g.getOptarg());
				break;
			case 'M':
				minNumResMemberCA = Integer.parseInt(g.getOptarg());
				break;
			case 'x':
				String[] xtokens = g.getOptarg().split(",");
				entrCallCutoff = new double[xtokens.length];
				for (int i=0;i<xtokens.length;i++){
					entrCallCutoff[i] = Double.parseDouble(xtokens[i]);
				}
				break;
			case 'X':
				String[] Xtokens = g.getOptarg().split(",");
				kaksCallCutoff = new double[Xtokens.length];
				for (int i=0;i<Xtokens.length;i++){
					kaksCallCutoff[i] = Double.parseDouble(Xtokens[i]);
				}				
				break;
			case 'g':
				grayZoneWidth = Double.parseDouble(g.getOptarg());
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
		try {
			ROOTLOGGER.addAppender(new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p - %m%n"),outDir+"/"+baseName+".log",false));
		} catch (IOException e) {
			System.err.println("Couldn't open log file "+outDir+"/"+baseName+".log"+" for writing.");
			System.err.println(e.getMessage());
			System.exit(1);
		}
	    ROOTLOGGER.setLevel(Level.INFO);

	    // TODO now using the apache common logging framework which acts as a meta framework for other frameworks such
	    //      as log4j or java logging. We always should instantiate loggers from apache commons (LogFactory.getLog()).
	    //      Left to do is still configure the logging in CRK properly by using the apache commons mechanism. At the moment
	    //      the logging configuration is done using log4j/java logger mechanisms.
		
		loadConfigFile();
		
		
		try {


			String pdbName = pdbCode; // the name to be used in many of the output files
			if (inFile!=null) pdbName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
			
			// 0 load pdb
			PdbAsymUnit pdb = doLoadPdb(inFile, pdbCode, outDir);
			
			// 1 finding interfaces
			InterfaceEnumerator interfEnumerator = new InterfaceEnumerator();
			ChainInterfaceList interfaces = 
				interfEnumerator.enumerate(pdb, usePisa, useNaccess, PISA_INTERFACES_URL, INTERFACE_DIST_CUTOFF, NACCESS_EXE, nSpherePointsASAcalc, numThreads);
			interfEnumerator.calcRimAndCores(zooming, bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, minNumResCA, cutoffsCA);
			interfEnumerator.logToFile(outDir, baseName, pdbName);
			interfEnumerator.checkForClashes(usePisa, INTERCHAIN_ATOM_CLASH_DISTANCE);
			interfEnumerator.checkNotEmptyListAboveArea(MIN_INTERF_AREA_REPORTING);
			
			// 2 finding evolutionary context
			EvolContextFinder evolContextFinder = new EvolContextFinder(pdb, pdbName, baseName);
			evolContextFinder.logUniqSequences();
			TreeMap<String,ChainEvolContext> cecs = evolContextFinder.getAllChainEvContext();
			evolContextFinder.retrieveQueryData(EMBL_CDS_CACHE_DIR, SIFTS_FILE, BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, numThreads, doScoreCRK);
			evolContextFinder.retrieveHomologues(BLAST_CACHE_DIR, BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, numThreads, idCutoff, QUERY_COVERAGE_CUTOFF, EMBL_CDS_CACHE_DIR, doScoreCRK, maxNumSeqsSelecton);			
			evolContextFinder.align(TCOFFEE_BIN, useTcoffeeVeryFastMode);
			evolContextFinder.writeSeqsAndAlnToFile(doScoreCRK,outDir);
			evolContextFinder.computeEvolutionaryScores(outDir, reducedAlphabet, doScoreCRK, SELECTON_BIN, selectonEpsilon);
			evolContextFinder.writeEvolScoresLog(outDir, doScoreCRK, ENTROPIES_FILE_SUFFIX, KAKS_FILE_SUFFIX);
			
			// 3 scoring
			System.out.println("Scores:");
			
			InterfaceScorer interfScorer = new InterfaceScorer(interfaces, cecs, pdb, outDir, baseName, pdbName);
			interfScorer.createInterfEvolContextList(MIN_HOMOLOGS_CUTOFF, minNumResCA, minNumResMemberCA, 
				idCutoff, QUERY_COVERAGE_CUTOFF, maxNumSeqsSelecton, MIN_INTERF_AREA_REPORTING);
			interfScorer.doEntropyScoring(ENTROPIES_FILE_SUFFIX, entrCallCutoff, grayZoneWidth);
			interfScorer.doKaksScoring(doScoreCRK, KAKS_FILE_SUFFIX, kaksCallCutoff, grayZoneWidth);
			
			
			

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
	
	private static PdbAsymUnit doLoadPdb(File inFile, String pdbCode, File outDir) {
		PdbAsymUnit pdb = null;
		File cifFile = null; // if given a pdb code in command line we will store the cif file here
		try {
			if (inFile==null) {
				cifFile = new File(outDir,pdbCode + ".cif");
				PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, PDB_FTP_CIF_URL, pdbCode, cifFile, USE_ONLINE_PDB);
				pdb = new PdbAsymUnit(cifFile);	
			} else {
				pdb = new PdbAsymUnit(inFile);
			}
		} catch (IOException e) {
			System.err.println("Couldn't get cif file for code "+pdbCode+" from ftp or couldn't uncompress it.");
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (FileFormatError e) {
			LOGGER.error("File format error: "+e.getMessage());
			System.exit(1);
		} catch (PdbLoadError e) {
			LOGGER.error("Couldn't load file "+((inFile==null)?cifFile.toString():inFile.toString()));
			LOGGER.error(e.getMessage());
			System.err.println("Couldn't load file "+((inFile==null)?cifFile.toString():inFile.toString()));
			System.exit(1);
		}
		
		if (pdb.getCrystalCell()==null) {
			LOGGER.fatal("No crystal information found in source "+pdbCode);
			System.exit(1);
		}
		return pdb;
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
			MIN_INTERF_AREA_REPORTING = Double.parseDouble(p.getProperty("MIN_INTERF_AREA_REPORTING", new Double(DEF_MIN_INTERF_AREA_REPORTING).toString())); 
					
			EMBL_CDS_CACHE_DIR  = p.getProperty("EMBL_CDS_CACHE_DIR", DEF_EMBL_CDS_CACHE_DIR);
			BLAST_CACHE_DIR     = p.getProperty("BLAST_CACHE_DIR", DEF_BLAST_CACHE_DIR);

			INTERCHAIN_ATOM_CLASH_DISTANCE = Double.parseDouble(p.getProperty("INTERCHAIN_ATOM_CLASH_DISTANCE", new Double(DEF_INTERCHAIN_ATOM_CLASH_DISTANCE).toString()));
			
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
