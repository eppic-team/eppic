package crk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.xml.sax.SAXException;

import owl.core.connections.pisa.PisaConnection;
import owl.core.runners.TcoffeeException;
import owl.core.runners.blast.BlastException;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.structure.SpaceGroup;
import owl.core.util.FileFormatException;
import owl.core.util.Goodies;

public class CRKMain {
	
	// CONSTANTS
	private static final String   PROGRAM_NAME = "crk";
	private static final String   CONFIG_FILE_NAME = ".crk.conf";
	private static final String   ENTROPIES_FILE_SUFFIX = ".entropies";
	private static final String   KAKS_FILE_SUFFIX = ".kaks";
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
	
	// default pymol exec
	private static final File	  DEF_PYMOL_EXE = new File("/usr/bin/pymol");
	
	// default crk cutoffs
	private static final double   DEF_QUERY_COVERAGE_CUTOFF = 0.85;
	private static final int      DEF_MIN_HOMOLOGS_CUTOFF = 10;
	private static final double   DEF_MIN_INTERF_AREA_REPORTING = 300;
	// default pdb2uniprot mapping blast thresholds
	private static final double   DEF_PDB2UNIPROT_ID_THRESHOLD = 0.95;
	private static final double   DEF_PDB2UNIPROT_QCOV_THRESHOLD = 0.85;
		
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
	private static final double[] DEF_CA_CUTOFFS = {0.85};
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
	
	private static File		PYMOL_EXE;
	
	private static double   QUERY_COVERAGE_CUTOFF;
	private static int      MIN_HOMOLOGS_CUTOFF;
	private static double   MIN_INTERF_AREA_REPORTING; 
	
	private static double   PDB2UNIPROT_ID_THRESHOLD;
	private static double   PDB2UNIPROT_QCOV_THRESHOLD;
			
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
	private CRKParams params;
	
	private PdbAsymUnit pdb;
	private ChainInterfaceList interfaces;
	private ChainEvolContextList cecs;
	private InterfaceEvolContextList iecList;
		
	public CRKMain() {

		
	}
	
	public void parseCommandLine(String[] args) throws CRKException {
		String defCACutoffsStr = String.format("%4.2f",DEF_CA_CUTOFFS[0]);
		for (int i=1;i<DEF_CA_CUTOFFS.length;i++) {
			defCACutoffsStr += String.format(",%4.2f",DEF_CA_CUTOFFS[i]);
		}
		
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
		"                  (and slower). Default: "+DEF_NSPHEREPOINTS_ASA_CALC+"\n" +
		"  [-I <file>]  :  binary file containing the interface enumeration output of a previous \n" +
		"                  run of CRK\n" +
		"  [-C <file>]  :  binary file containing the evolutionary scores for a particular \n" +
		"                  sequence output of a previous run of CRK\n" +
		"  [-l]         :  if specified thumbnail images will be generated for each interface \n" +
		"                  (requires pymol)\n" +
		"  [-L <file>]  :  a file where progress log will be written to. Default: progress log \n" +
		"                  written to std output\n" +
		"  [-u]         :  debug, if specified debug output will be also shown on standard output\n\n";
		
		params.parseCommandLine(args, PROGRAM_NAME, help);
		params.checkCommandLineInput();

	}
		
	public void setUpLogging() {
		
		// setting up the file logger for log4j
		try {
			FileAppender fullLogAppender = new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p - %m%n"),params.getOutDir()+"/"+params.getBaseName()+".log",false);
			fullLogAppender.setThreshold(Level.INFO);
			ConsoleAppender errorAppender = new ConsoleAppender(new PatternLayout("%5p - %m%n"),ConsoleAppender.SYSTEM_ERR);
			errorAppender.setThreshold(Level.ERROR);
			ConsoleAppender outAppender = new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %5p - %m%n"),ConsoleAppender.SYSTEM_OUT);
			outAppender.setThreshold(Level.DEBUG);
			ROOTLOGGER.addAppender(fullLogAppender);
			ROOTLOGGER.addAppender(errorAppender);
			if (params.getDebug())
				ROOTLOGGER.addAppender(outAppender);
		} catch (IOException e) {
			System.err.println("Couldn't open log file "+params.getOutDir()+"/"+params.getBaseName()+".log"+" for writing.");
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
			LOGGER.fatal("Error while reading from config file " + userConfigFile + ": " + e.getMessage());
			System.exit(1);
		}

	}

	public void doLoadPdb() throws CRKException {

		pdb = null;
		File cifFile = null; // if given a pdb code in command line we will store the cif file here
		try {
			if (!params.isInputAFile()) {
				cifFile = new File(params.getOutDir(),params.getPdbCode() + ".cif");
				try {
					PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, PDB_FTP_CIF_URL, params.getPdbCode(), cifFile, USE_ONLINE_PDB);
				} catch(IOException e) {
					throw new CRKException(e,"Couldn't get cif file for code "+params.getPdbCode()+" from ftp or couldn't uncompress it. Error: "+e.getMessage(),true);
				}
					
			} else {
				cifFile = params.getInFile();
			}
			pdb = new PdbAsymUnit(cifFile);
		} catch (FileFormatException e) {
			throw new CRKException(e,"File format error: "+e.getMessage(),true);
		} catch (PdbLoadException e) {
			throw new CRKException(e,"Couldn't load file "+cifFile+". Error: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new CRKException(e,"Problems reading PDB data from "+cifFile+". Error: "+e.getMessage(),true);
		}
		
		if (pdb.getCrystalCell()==null) {
			throw new CRKException(null, "No crystal information found in source "+params.getPdbCode(), true);
		}
	}
	
	public void doLoadInterfacesFromFile() throws CRKException {
		try {
			params.getProgressLog().println("Loading interfaces enumeration from file "+params.getInterfSerFile());
			LOGGER.info("Loading interfaces enumeration from file "+params.getInterfSerFile());
			interfaces = (ChainInterfaceList)Goodies.readFromFile(params.getInterfSerFile());
		} catch (ClassNotFoundException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		}
		
		if (!pdb.getPdbCode().equals(interfaces.get(0).getFirstMolecule().getPdbCode())) {
			throw new CRKException(null,"PDB codes of given PDB entry/file and given interface enumeration binary file don't match.",true);
		}
		
		if (params.isZooming()) {
			interfaces.calcRimAndCores(params.getBsaToAsaSoftCutoff(), params.getBsaToAsaHardCutoff(), params.getRelaxationStep(), params.getMinNumResCA());
		} else {
			interfaces.calcRimAndCores(params.getCutoffsCA());
		}
		
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) {
			LOGGER.warn(String.format("No interfaces with area above %4.0f. Nothing to score.\n",MIN_INTERF_AREA_REPORTING));			
		}
	}

	public void doFindInterfaces() throws CRKException {

		if (params.isUsePisa()) {
			params.getProgressLog().println("Getting PISA interfaces...");
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
			params.getProgressLog().println("Calculating possible interfaces...");
			try {
				if (params.isUseNaccess()) {
					interfaces = pdb.getAllInterfaces(INTERFACE_DIST_CUTOFF, NACCESS_EXE, 0, 0, false);
					LOGGER.info("Interfaces calculated with NACCESS.");
				} else {
					interfaces = pdb.getAllInterfaces(INTERFACE_DIST_CUTOFF, null, params.getnSpherePointsASAcalc(), params.getNumThreads(), false);
					LOGGER.info("Interfaces calculated with "+params.getnSpherePointsASAcalc()+" sphere points.");
				}
			} catch (IOException e) {
				throw new CRKException(e,"Couldn't run NACCESS for BSA calculation. Error: "+e.getMessage(),true);
			}
		}

		params.getProgressLog().println("Done");


		// checking for clashes
		if (!params.isUsePisa() && interfaces.hasInterfacesWithClashes(INTERCHAIN_ATOM_CLASH_DISTANCE)) {
			String msg = "Clashes found in some of the interfaces (atoms distance below "+INTERCHAIN_ATOM_CLASH_DISTANCE+"):";
			List<ChainInterface> clashyInterfs = interfaces.getInterfacesWithClashes(INTERCHAIN_ATOM_CLASH_DISTANCE);
			for (ChainInterface clashyInterf:clashyInterfs) {
				msg+=("\nInterface: "+clashyInterf.getFirstMolecule().getPdbChainCode()+"+"
						+clashyInterf.getSecondMolecule().getPdbChainCode()+" ("+
						SpaceGroup.getAlgebraicFromMatrix(clashyInterf.getSecondTransf())+
						") Clashes: "+clashyInterf.getNumClashes(INTERCHAIN_ATOM_CLASH_DISTANCE));
			}
			msg+=("\nThis is most likely an error in the structure. If you think the structure is correct, please report a bug.");
			// we used to throw a fatal exception and exit here, but we decided to simply warn and go ahead 
			LOGGER.warn(msg);
			System.err.println(msg);
			//throw new CRKException(null, msg, true);
		}
		if (params.isZooming()) {
			interfaces.calcRimAndCores(params.getBsaToAsaSoftCutoff(), params.getBsaToAsaHardCutoff(), params.getRelaxationStep(), params.getMinNumResCA());
		} else {
			interfaces.calcRimAndCores(params.getCutoffsCA());
		}


		try {
			PrintStream interfLogPS = new PrintStream(params.getOutputFile(".interfaces"));
			interfaces.printTabular(interfLogPS, params.getJobName());
			interfLogPS.close();
		} catch(IOException	e) {
			throw new CRKException(e,"Couldn't log interfaces description to file: "+e.getMessage(),false);
		}
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) {
			LOGGER.warn(String.format("No interfaces with area above %4.0f. Nothing to score.\n",MIN_INTERF_AREA_REPORTING));			
		}

		try {
			Goodies.serialize(params.getOutputFile(".interfaces.dat"),interfaces);
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write serialized ChainInterfaceList object to file: "+e.getMessage(),false);
		}
	}
	
	public void doLoadEvolContextFromFile() throws CRKException {
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) return;
		
		String msg = "Unique sequences for "+params.getJobName()+":";
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
		
		try {
			params.getProgressLog().println("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			LOGGER.info("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			cecs = (ChainEvolContextList)Goodies.readFromFile(params.getChainEvContextSerFile());
		} catch (ClassNotFoundException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		} catch(IOException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		}

		// TODO check whether this looks compatible with the interfaces that we have
	}
	
	public void doFindEvolContext() throws CRKException {
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) return;
		
		String msg = "Unique sequences for "+params.getJobName()+":";
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
			
			Matcher nonprotMatcher = NONPROT_PATTERN.matcher(pdb.getChain(representativeChain).getSequence().getSeq());
			if (nonprotMatcher.matches()) {
				LOGGER.warn("Representative chain "+representativeChain+" does not seem to be a protein chain. Won't analyse it.");
				continue;
			}

			cecs.addChainEvolContext(representativeChain,pdb.getSeqIdenticalGroup(representativeChain),
					new ChainEvolContext(pdb.getChain(representativeChain).getSequence().getSeq(), representativeChain, pdb.getPdbCode(), params.getJobName()));
		}
		// a) getting the uniprot ids corresponding to the query (the pdb sequence)
		for (ChainEvolContext chainEvCont:cecs.getAllChainEvolContext()) {
			File emblQueryCacheFile = null;
			if (EMBL_CDS_CACHE_DIR!=null) {
				emblQueryCacheFile = new File(EMBL_CDS_CACHE_DIR,params.getBaseName()+"."+chainEvCont.getRepresentativeChainCode()+".query.emblcds.fa");
			}
			params.getProgressLog().println("Finding query's uniprot mapping (through SIFTS or blasting)");
			try {
				chainEvCont.retrieveQueryData(SIFTS_FILE, emblQueryCacheFile, BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, params.getNumThreads(),params.isDoScoreCRK(),PDB2UNIPROT_ID_THRESHOLD,PDB2UNIPROT_QCOV_THRESHOLD);
			} catch (BlastException e) {
				throw new CRKException(e,"Couldn't run blast to retrieve query's uniprot mapping: "+e.getMessage(),true);
			} catch (IOException e) {
				throw new CRKException(e,"Problems while retrieving query data: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e,"Thread interrupted while running blast for retrieving query data: "+e.getMessage(),true);
			}
			if (params.isDoScoreCRK() && chainEvCont.getQueryRepCDS()==null) {
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("No CDS good match for query sequence! can't do CRK analysis on it.");
			}

			// b) getting the homologs and sequence data 
			params.getProgressLog().println("Blasting for homologues...");
			File blastCacheFile = null;
			if (BLAST_CACHE_DIR!=null) {
				blastCacheFile = new File(BLAST_CACHE_DIR,params.getBaseName()+"."+chainEvCont.getRepresentativeChainCode()+".blast.xml"); 
			}
			try {
				chainEvCont.retrieveHomologs(BLAST_BIN_DIR, BLAST_DB_DIR, BLAST_DB, params.getNumThreads(), params.getIdCutoff(), QUERY_COVERAGE_CUTOFF, blastCacheFile);
				LOGGER.info("Uniprot version used: "+chainEvCont.getUniprotVer());
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (BlastException e) {
				throw new CRKException(e,"Couldn't run blast to retrieve homologs: "+e.getMessage() ,true);
			} catch (IOException e) {
				throw new CRKException(e,"Problem while blasting for sequence homologs: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e,"Thread interrupted while blasting for sequence homologs: "+e.getMessage(),true);
			}

			params.getProgressLog().println("Retrieving UniprotKB data and EMBL CDS sequences");
			File emblHomsCacheFile = null;
			if (EMBL_CDS_CACHE_DIR!=null) {
				emblHomsCacheFile = new File(EMBL_CDS_CACHE_DIR,params.getBaseName()+"."+chainEvCont.getRepresentativeChainCode()+".homologs.emblcds.fa");
			}
			try {
				chainEvCont.retrieveHomologsData(emblHomsCacheFile, params.isDoScoreCRK());
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (IOException e) {
				throw new CRKException(e, "Problem while fetching CDS data: "+e.getMessage(),true);
			}
			if (params.isDoScoreCRK() && !chainEvCont.isConsistentGeneticCodeType()){
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("The list of homologs does not have a single genetic code type, can't do CRK analysis on it.");
			}
			
			// remove redundancy
			chainEvCont.removeRedundancy();

			// skimming so that there's not too many sequences for selecton
			chainEvCont.skimList(params.getMaxNumSeqsSelecton());
			
			// check the back-translation of CDS to uniprot
			// check whether we have a good enough CDS for the chain
			if (params.isDoScoreCRK() && chainEvCont.canDoCRK()) {
				LOGGER.info("Number of homologs with at least one uniprot CDS mapping: "+chainEvCont.getNumHomologsWithCDS());
				LOGGER.info("Number of homologs with valid CDS: "+chainEvCont.getNumHomologsWithValidCDS());
			}

			// c) align
			params.getProgressLog().println("Aligning protein sequences with t_coffee...");
			try {
				chainEvCont.align(TCOFFEE_BIN, params.isUseTcoffeeVeryFastMode());
			} catch (TcoffeeException e) {
				throw new CRKException(e, "Couldn't run t_coffee to align protein sequences: "+e.getMessage(), true);
			} catch (IOException e) {
				throw new CRKException(e, "Problems while running t_coffee to align protein sequences: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e, "Thread interrupted while running t_coffee to align protein sequences: "+e.getMessage(),true);
			}

			File outFile = null;
			try {
				// writing homolog sequences to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".fa");
				chainEvCont.writeHomologSeqsToFile(outFile);

				// printing summary to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".log");
				PrintStream log = new PrintStream(outFile);
				chainEvCont.printSummary(log);
				log.close();
				// writing the alignment to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".aln");
				chainEvCont.writeAlignmentToFile(outFile);
				// writing the nucleotides alignment to file
				if (params.isDoScoreCRK() && chainEvCont.canDoCRK()) {
					outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".cds.aln");
					chainEvCont.writeNucleotideAlignmentToFile(outFile);
				}
			} catch(FileNotFoundException e){
				LOGGER.error("Couldn't write file "+outFile);
				LOGGER.error(e.getMessage());
			}

			// d) computing entropies
			chainEvCont.computeEntropies(params.getReducedAlphabet());

			// e) compute ka/ks ratios
			if (params.isDoScoreCRK() && chainEvCont.canDoCRK()) {
				params.getProgressLog().println("Running selecton (this will take long)...");
				try {
				chainEvCont.computeKaKsRatiosSelecton(SELECTON_BIN, 
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.res"),
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.log"), 
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.tree"),
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.global"),
						params.getSelectonEpsilon());
				} catch (IOException e) {
					throw new CRKException(e, "Problems while running selecton: "+e.getMessage(), true);
				} catch (InterruptedException e) {
					throw new CRKException(e, "Thread interrupted while running selecton: "+e.getMessage(), true);
				}
			}

			try {
				// writing the conservation scores (entropies/kaks) log file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+ENTROPIES_FILE_SUFFIX);
				PrintStream conservScoLog = new PrintStream(outFile);
				chainEvCont.printConservationScores(conservScoLog, ScoringType.ENTROPY);
				conservScoLog.close();
				if (params.isDoScoreCRK() && chainEvCont.canDoCRK()) {
					outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+KAKS_FILE_SUFFIX);
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
			Goodies.serialize(params.getOutputFile(".chainevolcontext.dat"),cecs);
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write serialized ChainEvolContextList object to file: "+e.getMessage(),false);
		}
		
	}
	
	public void doScoring() throws CRKException {
		if (interfaces.getNumInterfacesAboveArea(MIN_INTERF_AREA_REPORTING)==0) return;
		
		iecList = new InterfaceEvolContextList(params.getJobName(), MIN_HOMOLOGS_CUTOFF, params.getMinNumResCA(), params.getMinNumResMemberCA(), 
				params.getIdCutoff(), QUERY_COVERAGE_CUTOFF, params.getMaxNumSeqsSelecton(), MIN_INTERF_AREA_REPORTING);
		iecList.addAll(interfaces,cecs);
		
		try {
			for (int callCutoffIdx=0;callCutoffIdx<params.getEntrCallCutoffs().length;callCutoffIdx++) {
				String suffix = null;
				if (params.getEntrCallCutoffs().length==1) suffix="";
				else suffix="."+Integer.toString(callCutoffIdx+1);
				PrintStream scoreEntrPS = new PrintStream(params.getOutputFile(ENTROPIES_FILE_SUFFIX+".scores"+suffix));
				// entropy nw
				iecList.scoreEntropy(false);
				//iecList.printScoresTable(params.getProgressLog(), params.getEntrCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getEntrCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
				//iecList.printScoresTable(scoreEntrPS, params.getEntrCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getEntrCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
				PdbScore[] entSc = new PdbScore[2];
				entSc[0] = iecList.getPdbScoreObject();
				// entropy w
				iecList.scoreEntropy(true);
				//iecList.printScoresTable(params.getProgressLog(), params.getEntrCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getEntrCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
				//iecList.printScoresTable(scoreEntrPS, params.getEntrCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getEntrCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
				iecList.writeScoresPDBFiles(params,ENTROPIES_FILE_SUFFIX+".pdb");
				iecList.writeRimCorePDBFiles(params, ".rimcore.pdb");
				entSc[1] = iecList.getPdbScoreObject();
				Goodies.serialize(params.getOutputFile(ENTROPIES_FILE_SUFFIX+".scores.dat"+suffix), entSc);

				if (params.isGenerateThumbnails()) {
					iecList.generateThumbnails(PYMOL_EXE,params,".rimcore.pdb");
				}
				scoreEntrPS.close();
			}
		} catch (IOException e) {
			throw new CRKException(e, "Couldn't write final interface entropy scores or related PDB files. "+e.getMessage(),true);
		} catch (PdbLoadException e) {
			throw new CRKException(e, "Couldn't generate thumbnails, problem in reading PDB file: "+e.getMessage(),false);
		} catch (InterruptedException e) {
			throw new CRKException(e, "Couldn't generate thumbnails, pymol thread interrupted: "+e.getMessage(),false);
		}
		try {
			// ka/ks scoring
			if (params.isDoScoreCRK()) {
				for (int callCutoffIdx=0;callCutoffIdx<params.getKaksCallCutoffs().length;callCutoffIdx++) {
					String suffix = null;
					if (params.getKaksCallCutoffs().length==1) suffix="";
					else suffix="."+Integer.toString(callCutoffIdx+1);
					PrintStream scoreKaksPS = new PrintStream(params.getOutputFile(KAKS_FILE_SUFFIX+".scores"+suffix));
					// kaks nw
					iecList.scoreKaKs(false);
					iecList.printScoresTable(params.getProgressLog(),  params.getKaksCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getKaksCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
					iecList.printScoresTable(scoreKaksPS,  params.getKaksCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getKaksCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
					PdbScore[] kaksSc = new PdbScore[2];
					kaksSc[0] = iecList.getPdbScoreObject();
					// kaks w
					iecList.scoreKaKs(true);
					iecList.printScoresTable(params.getProgressLog(),  params.getKaksCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getKaksCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
					iecList.printScoresTable(scoreKaksPS,  params.getKaksCallCutoff(callCutoffIdx)-params.getGrayZoneWidth(), params.getKaksCallCutoff(callCutoffIdx)+params.getGrayZoneWidth());
					iecList.writeScoresPDBFiles(params, KAKS_FILE_SUFFIX+".pdb");
					iecList.writeRimCorePDBFiles(params, ".rimcore.pdb");
					kaksSc[1] = iecList.getPdbScoreObject();
					Goodies.serialize(params.getOutputFile(KAKS_FILE_SUFFIX+".scores.dat"+suffix), kaksSc);
					scoreKaksPS.close();
				}
			}
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write final interface Ka/Ks scores or related PDB files. "+e.getMessage(),true);
		}
		try {
			// we only write one of this (does not depende on call cutoff and contains both entropies+kaks)
			iecList.writeResidueDetailsFiles(params, "resDetails.dat");
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write score residue details serialized file. "+e.getMessage(),false);
		}

		params.getProgressLog().println("Done scoring");
	}
	
	public void setDefaults() {
		double[] entrCallCutoff = {DEF_ENTR_CALL_CUTOFF};
		double[] kaksCallCutoff  = {DEF_KAKS_CALL_CUTOFF};
		this.params = new CRKParams(null, false, DEF_IDENTITY_CUTOFF,null,new File("."),
				DEF_NUMTHREADS,
				DEF_ENTROPY_ALPHABET,DEF_USE_TCOFFEE_VERYFAST_MODE,
				false,DEF_SOFT_CUTOFF_CA,DEF_HARD_CUTOFF_CA, DEF_RELAX_STEP_CA, 
				DEF_CA_CUTOFFS,
				DEF_MIN_NUM_RES_CA, DEF_MIN_NUM_RES_MEMBER_CA,
				DEF_SELECTON_EPSILON, DEF_MAX_NUM_SEQUENCES_SELECTON,
				false, false,
				DEF_NSPHEREPOINTS_ASA_CALC,
				DEF_GRAY_ZONE_WIDTH,
				entrCallCutoff,kaksCallCutoff,
				null,null,
				false,
				System.out,
				false);
	}
	
	/**
	 * The main of CRK 
	 */
	public static void main(String[] args) {
		
		CRKMain crkMain = new CRKMain();

		crkMain.setDefaults();
		
		try {
			crkMain.parseCommandLine(args);
		
			// turn off jaligner logging (we only use NeedlemanWunschGotoh from that package)
			// (for some reason this doesn't work if condensated into one line, it seems that one needs to instantiate the logger and then call setLevel)
			// (and even weirder, for some reason it doesn't work if you put the code in its own separate method!)
			java.util.logging.Logger jalLogger = java.util.logging.Logger.getLogger("NeedlemanWunschGotoh");
			jalLogger.setLevel(java.util.logging.Level.OFF);

			crkMain.setUpLogging();

			crkMain.loadConfigFile();
			
			try {
				LOGGER.info("Running in host "+InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				LOGGER.warn("Could not determine host where we are running.");
			}
			
			// 0 load pdb
			crkMain.doLoadPdb();

			// 1 finding interfaces
			if (crkMain.params.getInterfSerFile()!=null) {
				crkMain.doLoadInterfacesFromFile();
			} else {
				crkMain.doFindInterfaces();
			}

			// 2 finding evolutionary context
			if (crkMain.params.getChainEvContextSerFile()!=null) {
				crkMain.doLoadEvolContextFromFile();
			} else {
				crkMain.doFindEvolContext();
			}

			// 3 scoring
			crkMain.doScoring();

		} catch (CRKException e) {
			e.log(LOGGER);
			e.exitIfFatal(1);
		} 
//		catch (Exception e) {
//			e.printStackTrace();
//
//			String stack = "";
//			for (StackTraceElement el:e.getStackTrace()) {
//				stack+="\tat "+el.toString()+"\n";				
//			}
//			LOGGER.fatal("Unexpected error. Exiting.\n"+e+"\n"+stack);
//			System.exit(1);
//		}
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
			
			PYMOL_EXE			= new File(p.getProperty("PYMOL_EXE", DEF_PYMOL_EXE.toString()));

			QUERY_COVERAGE_CUTOFF = Double.parseDouble(p.getProperty("QUERY_COVERAGE_CUTOFF", new Double(DEF_QUERY_COVERAGE_CUTOFF).toString()));
			MIN_HOMOLOGS_CUTOFF = Integer.parseInt(p.getProperty("MIN_HOMOLOGS_CUTOFF", new Integer(DEF_MIN_HOMOLOGS_CUTOFF).toString()));
			MIN_INTERF_AREA_REPORTING = Double.parseDouble(p.getProperty("MIN_INTERF_AREA_REPORTING", new Double(DEF_MIN_INTERF_AREA_REPORTING).toString()));
			
			PDB2UNIPROT_ID_THRESHOLD = Double.parseDouble(p.getProperty("PDB2UNIPROT_ID_THRESHOLD", new Double(DEF_PDB2UNIPROT_ID_THRESHOLD).toString()));
			PDB2UNIPROT_QCOV_THRESHOLD = Double.parseDouble(p.getProperty("PDB2UNIPROT_QCOV_THRESHOLD", new Double(DEF_PDB2UNIPROT_QCOV_THRESHOLD).toString()));
					
			EMBL_CDS_CACHE_DIR  = p.getProperty("EMBL_CDS_CACHE_DIR", DEF_EMBL_CDS_CACHE_DIR);
			BLAST_CACHE_DIR     = p.getProperty("BLAST_CACHE_DIR", DEF_BLAST_CACHE_DIR);

			INTERCHAIN_ATOM_CLASH_DISTANCE = Double.parseDouble(p.getProperty("INTERCHAIN_ATOM_CLASH_DISTANCE", new Double(DEF_INTERCHAIN_ATOM_CLASH_DISTANCE).toString()));
			
		} catch (NumberFormatException e) {
			System.err.println("A numerical value in the config file was incorrectly specified: "+e.getMessage()+".\n" +
					"Please check the config file.");
			System.exit(1);
		}
	}
	
	public CRKParams getCRKParams()
	{
		return params;
	}
}
