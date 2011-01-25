package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
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
import owl.core.structure.Pdb;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadError;
import owl.core.structure.AminoAcid;
import owl.core.structure.SpaceGroup;
import owl.core.util.FileFormatError;

public class CRKMain {
	
	// CONSTANTS
	private static final String   PROGRAM_NAME = "crk";
	private static final String   CONFIG_FILE_NAME = ".crk.conf";
	private static final String   ENTROPIES_FILE_SUFFIX = ".entropies";
	private static final String   KAKS_FILE_SUFFIX = ".kaks";
	private static final Pattern  PDBCODE_PATTERN = Pattern.compile("^\\d\\w\\w\\w$");
	private static final Pattern  NONPROT_PATTERN = Pattern.compile("^X+$");
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
	
	// fields
	private String pdbCode;
	private boolean doScoreCRK;
	private double idCutoff;
	private String baseName;
	private File outDir;
	private int numThreads;
	private int reducedAlphabet;
	private boolean useTcoffeeVeryFastMode;
	
	private boolean zooming;
	
	private double bsaToAsaSoftCutoff;
	private double bsaToAsaHardCutoff;
	private double relaxationStep;
	
	private double[] cutoffsCA;
	private String defCACutoffsStr;
	
	private int      minNumResCA;
	private int      minNumResMemberCA; 
	
	private double selectonEpsilon;

	private int maxNumSeqsSelecton;
	
	private boolean usePisa;

	private boolean useNaccess;
	
	private int nSpherePointsASAcalc;

	private double grayZoneWidth;
	private double[] entrCallCutoff;
	private double[] kaksCallCutoff;
	
	private File inFile;
	private String pdbName;
	private PdbAsymUnit pdb;
	private ChainInterfaceList interfaces;
	private ChainEvolContextList cecs;
	private File cifFile;
	private InterfaceEvolContextList iecList;
	
	private PrintStream progressLogPS;

	public CRKMain(PrintStream progressLogPS) {
		this.progressLogPS = progressLogPS;
		
		pdbCode = null;
		doScoreCRK = false;
		idCutoff = DEF_IDENTITY_CUTOFF;
		baseName = null;
		outDir = new File(".");
		numThreads = DEF_NUMTHREADS;
		reducedAlphabet = DEF_ENTROPY_ALPHABET;
		useTcoffeeVeryFastMode = DEF_USE_TCOFFEE_VERYFAST_MODE;
		
		zooming = false;
		
		bsaToAsaSoftCutoff = DEF_SOFT_CUTOFF_CA;
		bsaToAsaHardCutoff = DEF_HARD_CUTOFF_CA;
		relaxationStep = DEF_RELAX_STEP_CA;
		
		cutoffsCA  = DEF_CA_CUTOFFS;
		defCACutoffsStr = String.format("%4.2f",DEF_CA_CUTOFFS[0]);
		for (int i=1;i<DEF_CA_CUTOFFS.length;i++) {
			defCACutoffsStr += String.format(",%4.2f",DEF_CA_CUTOFFS[i]);
		}
		
		minNumResCA  = DEF_MIN_NUM_RES_CA;
		minNumResMemberCA = DEF_MIN_NUM_RES_MEMBER_CA; 
		
		selectonEpsilon = DEF_SELECTON_EPSILON;

		maxNumSeqsSelecton = DEF_MAX_NUM_SEQUENCES_SELECTON;
		
		usePisa = false;

		useNaccess = false;
		
		nSpherePointsASAcalc = DEF_NSPHEREPOINTS_ASA_CALC;

		grayZoneWidth = DEF_GRAY_ZONE_WIDTH;
		entrCallCutoff  = new double[1]; 
		entrCallCutoff[0] = DEF_ENTR_CALL_CUTOFF;
		kaksCallCutoff  = new double[1];
		kaksCallCutoff[0] = DEF_KAKS_CALL_CUTOFF;
 
	}
	
	public void parseCommandLine(String[] args) {
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
		
		inFile = new File(pdbCode);
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

	}
	
	public void setUpLogging() {
		
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

	}
	
	public void loadConfigFile() {
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

	public void doLoadPdb() throws CRKException {
		pdbName = pdbCode; // the name to be used in many of the output files
		if (inFile!=null) pdbName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));

		pdb = null;
		cifFile = null; // if given a pdb code in command line we will store the cif file here
		try {
			if (inFile==null) {
				cifFile = new File(outDir,pdbCode + ".cif");
				try {
					PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, PDB_FTP_CIF_URL, pdbCode, cifFile, USE_ONLINE_PDB);
				} catch(IOException e) {
					throw new CRKException(e,"Couldn't get cif file for code "+pdbCode+" from ftp or couldn't uncompress it. Error: "+e.getMessage(),true);
				}
				pdb = new PdbAsymUnit(cifFile);	
			} else {
				pdb = new PdbAsymUnit(inFile);
			}
		} catch (FileFormatError e) {
			throw new CRKException(e,"File format error: "+e.getMessage(),true);
		} catch (PdbLoadError e) {
			throw new CRKException(e,"Couldn't load file "+((inFile==null)?cifFile.toString():inFile.toString())+". Error: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new CRKException(e,"Problems reading PDB data from "+((inFile==null)?cifFile.toString():inFile.toString())+". Error: "+e.getMessage(),true);
		}
		
		if (pdb.getCrystalCell()==null) {
			throw new CRKException(null, "No crystal information found in source "+((inFile==null)?pdbCode:inFile.toString()), true);
		}
	}

	public void doFindInterfaces() throws CRKException {

		if (usePisa) {
			progressLogPS.println("Getting PISA interfaces...");
			LOGGER.info("Interfaces from PISA.");
			PisaConnection pc = new PisaConnection(PISA_INTERFACES_URL, null, null);
			List<String> pdbCodes = new ArrayList<String>();
			pdbCodes.add(pdb.getPdbCode());
			try {
				interfaces = pc.getInterfacesDescription(pdbCodes).get(pdb.getPdbCode()).convertToChainInterfaceList(pdb);
			} catch(SAXException e) {
				throw new CRKException(e,"Error while reading PISA xml file"+e.getMessage(),true);
			} catch(IOException e) {
				throw new CRKException(e,"Error while retrieving PISA xml file: "+e.getMessage(),true);
			}
		} else {
			progressLogPS.println("Calculating possible interfaces...");
			try {
				if (useNaccess) {
					interfaces = pdb.getAllInterfaces(INTERFACE_DIST_CUTOFF, NACCESS_EXE, 0, 0);
					LOGGER.info("Interfaces calculated with NACCESS.");
				} else {
					interfaces = pdb.getAllInterfaces(INTERFACE_DIST_CUTOFF, null, nSpherePointsASAcalc, numThreads);
					LOGGER.info("Interfaces calculated with "+nSpherePointsASAcalc+" sphere points.");
				}
			} catch (IOException e) {
				throw new CRKException(e,"Couldn't run NACCESS for BSA calculation. Error: "+e.getMessage(),true);
			}
		}

		progressLogPS.println("Done");


		// checking for clashes
		if (!usePisa && interfaces.hasInterfacesWithClashes(INTERCHAIN_ATOM_CLASH_DISTANCE)) {
			String msg = "Clashes found in some of the interfaces (atoms distance below "+INTERCHAIN_ATOM_CLASH_DISTANCE+"):";
			List<ChainInterface> clashyInterfs = interfaces.getInterfacesWithClashes(INTERCHAIN_ATOM_CLASH_DISTANCE);
			for (ChainInterface clashyInterf:clashyInterfs) {
				msg+=("\nInterface: "+clashyInterf.getFirstMolecule().getPdbChainCode()+"+"
						+clashyInterf.getSecondMolecule().getPdbChainCode()+" ("+
						SpaceGroup.getAlgebraicFromMatrix(clashyInterf.getSecondTransf())+
						") Clashes: "+clashyInterf.getNumClashes(INTERCHAIN_ATOM_CLASH_DISTANCE));
			}
			msg+=("\nThis is most likely an error in the structure. If you think the structure is correct, please report a bug.");
			throw new CRKException(null, msg, true);
		}
		if (zooming) {
			interfaces.calcRimAndCores(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, minNumResCA);
		} else {
			interfaces.calcRimAndCores(cutoffsCA);
		}


		try {
			PrintStream interfLogPS = new PrintStream(new File(outDir,baseName+".interfaces"));
			interfaces.printTabular(interfLogPS, pdbName);
			interfLogPS.close();
		} catch(IOException	e) {
			throw new CRKException(e,"Couldn't log interfaces description to file: "+e.getMessage(),false);
		}
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) {
			LOGGER.warn(String.format("No interfaces with area above %4.0f. Nothing to score.\n",MIN_INTERF_AREA_REPORTING));			
		}

		try {
			interfaces.serialize(new File(outDir,baseName+".interfaces.dat"));
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write serialized ChainInterfaceList object to file: "+e.getMessage(),false);
		}
	}
	
	public void doFindEvolContext() throws CRKException {
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) return;
		
		String msg = "Unique sequences for "+pdbName+":";
		int i = 1;
		for (String representativeChain:pdb.getAllRepChains()) {
			List<String> entity = pdb.getSeqIdenticalGroup(representativeChain);
			msg+=" "+i+":";
			for (String chain:entity) {
				msg+=" "+chain;
			}
			i++;
		}
		LOGGER.info(msg);
		
		
		
		cecs = new ChainEvolContextList();

		for (String representativeChain:pdb.getAllRepChains()) {
			
			Matcher nonprotMatcher = NONPROT_PATTERN.matcher(pdb.getChain(representativeChain).getSequence());
			if (nonprotMatcher.matches()) {
				LOGGER.warn("Representative chain "+representativeChain+" does not seem to be a protein chain. Won't analyse it.");
				continue;
			}

			cecs.addChainEvolContext(representativeChain,pdb.getSeqIdenticalGroup(representativeChain),
					new ChainEvolContext(pdb.getChain(representativeChain).getSequence(), representativeChain, pdb.getPdbCode(), pdbName));
		}
		// a) getting the uniprot ids corresponding to the query (the pdb sequence)
		for (ChainEvolContext chainEvCont:cecs.getAllChainEvolContext()) {
			File emblQueryCacheFile = null;
			if (EMBL_CDS_CACHE_DIR!=null) {
				emblQueryCacheFile = new File(EMBL_CDS_CACHE_DIR,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".query.emblcds.fa");
			}
			progressLogPS.println("Finding query's uniprot mapping (through SIFTS or blasting)");
			try {
				chainEvCont.retrieveQueryData(SIFTS_FILE, emblQueryCacheFile, BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, numThreads,doScoreCRK);
			} catch (BlastError e) {
				throw new CRKException(e,"Couldn't run blast to retrieve query's uniprot mapping: "+e.getMessage(),true);
			}  catch (IOException e) {
				throw new CRKException(e,"Problems while retrieving query data: "+e.getMessage(),true);
			}
			if (doScoreCRK && chainEvCont.getQueryRepCDS()==null) {
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("No CDS good match for query sequence! can't do CRK analysis on it.");
			}

			// b) getting the homologs and sequence data 
			progressLogPS.println("Blasting for homologues...");
			File blastCacheFile = null;
			if (BLAST_CACHE_DIR!=null) {
				blastCacheFile = new File(BLAST_CACHE_DIR,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".blast.xml"); 
			}
			try {
				chainEvCont.retrieveHomologs(BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, numThreads, idCutoff, QUERY_COVERAGE_CUTOFF, blastCacheFile);
				LOGGER.info("Uniprot version used: "+chainEvCont.getUniprotVer());
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (BlastError e) {
				throw new CRKException(e,"Couldn't run blast to retrieve homologs: "+e.getMessage() ,true);
			} catch (IOException e) {
				throw new CRKException(e,"Problem while blasting for sequence homologs: "+e.getMessage(),true);
			}

			progressLogPS.println("Retrieving UniprotKB data and EMBL CDS sequences");
			File emblHomsCacheFile = null;
			if (EMBL_CDS_CACHE_DIR!=null) {
				emblHomsCacheFile = new File(EMBL_CDS_CACHE_DIR,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".homologs.emblcds.fa");
			}
			try {
				chainEvCont.retrieveHomologsData(emblHomsCacheFile, doScoreCRK);
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (IOException e) {
				throw new CRKException(e, "Problem while fetching CDS data: "+e.getMessage(),true);
			}
			if (doScoreCRK && !chainEvCont.isConsistentGeneticCodeType()){
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("The list of homologs does not have a single genetic code type, can't do CRK analysis on it.");
			}
			
			// remove redundancy
			chainEvCont.removeRedundancy();

			// skimming so that there's not too many sequences for selecton
			chainEvCont.skimList(maxNumSeqsSelecton);
			
			// check the back-translation of CDS to uniprot
			// check whether we have a good enough CDS for the chain
			if (doScoreCRK && chainEvCont.canDoCRK()) {
				LOGGER.info("Number of homologs with at least one uniprot CDS mapping: "+chainEvCont.getNumHomologsWithCDS());
				LOGGER.info("Number of homologs with valid CDS: "+chainEvCont.getNumHomologsWithValidCDS());
			}

			// c) align
			progressLogPS.println("Aligning protein sequences with t_coffee...");
			try {
				chainEvCont.align(TCOFFEE_BIN, useTcoffeeVeryFastMode);
			} catch (TcoffeeError e) {
				throw new CRKException(e, "Couldn't run t_coffee to align protein sequences: "+e.getMessage(), true);
			} catch (IOException e) {
				throw new CRKException(e, "Problems while running t_coffee to align protein sequences: "+e.getMessage(),true);
			}

			File outFile = null;
			try {
				// writing homolog sequences to file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".fa");
				chainEvCont.writeHomologSeqsToFile(outFile);

				// printing summary to file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".log");
				PrintStream log = new PrintStream(outFile);
				chainEvCont.printSummary(log);
				log.close();
				// writing the alignment to file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".aln");
				chainEvCont.writeAlignmentToFile(outFile);
				// writing the nucleotides alignment to file
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".cds.aln");
					chainEvCont.writeNucleotideAlignmentToFile(outFile);
				}
			} catch(FileNotFoundException e){
				LOGGER.error("Couldn't write file "+outFile);
				LOGGER.error(e.getMessage());
			}

			// d) computing entropies
			chainEvCont.computeEntropies(reducedAlphabet);

			// e) compute ka/ks ratios
			if (doScoreCRK && chainEvCont.canDoCRK()) {
				progressLogPS.println("Running selecton (this will take long)...");
				try {
				chainEvCont.computeKaKsRatiosSelecton(SELECTON_BIN, 
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.res"),
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.log"), 
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.tree"),
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.global"),
						selectonEpsilon);
				} catch (IOException e) {
					throw new CRKException(e, "Problems while running selecton: "+e.getMessage(), true);
				}
			}

			try {
				// writing the conservation scores (entropies/kaks) log file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+ENTROPIES_FILE_SUFFIX);
				PrintStream conservScoLog = new PrintStream(outFile);
				chainEvCont.printConservationScores(conservScoLog, ScoringType.ENTROPY);
				conservScoLog.close();
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+KAKS_FILE_SUFFIX);
					conservScoLog = new PrintStream(outFile);
					chainEvCont.printConservationScores(conservScoLog, ScoringType.KAKS);
					conservScoLog.close();				
				}
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not write the scores log file "+outFile);
				LOGGER.error(e.getMessage());
			}
		}
		
		try {
			this.cecs.serialize(new File(outDir,baseName+".chainevolcontext.dat"));
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write serialized ChainEvolContextList object to file: "+e.getMessage(),false);
		}
		
	}
	
	public void doScoring() throws CRKException {
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) return;
		
		progressLogPS.println("Scores:");
		
		iecList = new InterfaceEvolContextList(pdbName, MIN_HOMOLOGS_CUTOFF, minNumResCA, minNumResMemberCA, 
				idCutoff, QUERY_COVERAGE_CUTOFF, maxNumSeqsSelecton, MIN_INTERF_AREA_REPORTING);
		for (ChainInterface pi:interfaces) {
			if (pi.isProtein()) {
				ArrayList<ChainEvolContext> chainsEvCs = new ArrayList<ChainEvolContext>();
				Pdb molec1 = pi.getFirstMolecule();
				Pdb molec2 = pi.getSecondMolecule();
				chainsEvCs.add(cecs.getChainEvolContext(molec1.getPdbChainCode()));
				chainsEvCs.add(cecs.getChainEvolContext(molec2.getPdbChainCode()));
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, chainsEvCs);
				iecList.add(iec);
			}
		}		
		try {
			for (int callCutoffIdx=0;callCutoffIdx<entrCallCutoff.length;callCutoffIdx++) {
				String suffix = null;
				if (entrCallCutoff.length==1) suffix="";
				else suffix="."+Integer.toString(callCutoffIdx+1);
				PrintStream scoreEntrPS = new PrintStream(new File(outDir,baseName+ENTROPIES_FILE_SUFFIX+".scores"+suffix));
				// entropy nw
				iecList.scoreEntropy(false);
				iecList.printScoresTable(progressLogPS, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				iecList.printScoresTable(scoreEntrPS, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				// entropy w
				iecList.scoreEntropy(true);
				iecList.printScoresTable(progressLogPS, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				iecList.printScoresTable(scoreEntrPS, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				iecList.writeScoresPDBFiles(outDir, baseName, ENTROPIES_FILE_SUFFIX+".pdb");
				iecList.writeRimCorePDBFiles(outDir, baseName, ".rimcore.pdb");
				scoreEntrPS.close();
			}
		} catch (IOException e) {
			throw new CRKException(e, "Couldn't write final interface entropy scores or related PDB files. "+e.getMessage(),true);
		}
		try {
			// ka/ks scoring
			if (doScoreCRK) {
				for (int callCutoffIdx=0;callCutoffIdx<kaksCallCutoff.length;callCutoffIdx++) {
					String suffix = null;
					if (kaksCallCutoff.length==1) suffix="";
					else suffix="."+Integer.toString(callCutoffIdx+1);
					PrintStream scoreKaksPS = new PrintStream(new File(outDir,baseName+KAKS_FILE_SUFFIX+".scores"+suffix));
					// kaks nw
					iecList.scoreKaKs(false);
					iecList.printScoresTable(progressLogPS,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					iecList.printScoresTable(scoreKaksPS,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					// kaks w
					iecList.scoreKaKs(true);
					iecList.printScoresTable(progressLogPS,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					iecList.printScoresTable(scoreKaksPS,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					iecList.writeScoresPDBFiles(outDir, baseName, KAKS_FILE_SUFFIX+".pdb");
					iecList.writeRimCorePDBFiles(outDir, baseName, ".rimcore.pdb");
					scoreKaksPS.close();
				}
			}
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write final interface Ka/Ks scores or related PDB files. "+e.getMessage(),true);
		}
		
	}
	
	/**
	 * The main of CRK 
	 */
	public static void main(String[] args) {
		
		CRKMain crkMain = new CRKMain(System.out);
		
		crkMain.parseCommandLine(args);

		// turn off jaligner logging (we only use NeedlemanWunschGotoh from that package)
		// (for some reason this doesn't work if condensated into one line, it seems that one needs to instantiate the logger and then call setLevel)
		// (and even weirder, for some reason it doesn't work if you put the code in its own separate method!)
		java.util.logging.Logger jalLogger = java.util.logging.Logger.getLogger("NeedlemanWunschGotoh");
		jalLogger.setLevel(java.util.logging.Level.OFF);
		
		crkMain.setUpLogging();
		
		crkMain.loadConfigFile();

		try {
			// 0 load pdb
			crkMain.doLoadPdb();

			// 1 finding interfaces
			crkMain.doFindInterfaces();

			// 2 finding evolutionary context
			crkMain.doFindEvolContext();

			// 3 scoring
			crkMain.doScoring();

		} catch (CRKException e) {
			e.log(LOGGER);
			e.exitIfFatal(1);
		} 
		catch (Exception e) {
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
		
}
