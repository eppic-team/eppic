package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.runners.blast.BlastRunner;
import owl.core.structure.AminoAcid;

public class CRKParams {
	
	// CONSTANTS
	public static final String     PROGRAM_NAME    = "eppic";
	public static final String	   PROGRAM_VERSION = "1.9.5";
	
	private static final Pattern   PDBCODE_PATTERN = Pattern.compile("^\\d\\w\\w\\w$");
	public static final String     CONFIG_FILE_NAME = ".eppic.conf";
	protected static final String  ENTROPIES_FILE_SUFFIX = ".entropies";
	protected static final String  GEOMETRY_FILE_SUFFIX = ".geometry.scores";
	protected static final String  CRSCORES_FILE_SUFFIX = ".corerim.scores";
	protected static final String  CSSCORES_FILE_SUFFIX = ".coresurface.scores";
	protected static final String  COMBINED_FILE_SUFFIX = ".combined.scores";
	protected static final String  STEPS_LOG_FILE_SUFFIX = ".steps.log";
	public static final double     INTERFACE_DIST_CUTOFF = 5.9;
	// if any interface has this number of clashes we'll abort with an error
	public static final int		   NUM_CLASHES_FOR_ERROR = 30;
	// shorter chains will be considered peptides
	public static final int	       PEPTIDE_LENGTH_CUTOFF = 20; 
	// 10% maximum allowed unreliable residues for calling nopred
	public static final double     MAX_ALLOWED_UNREL_RES = 0.1; 
	// minimum number of core residues per interface member to calculate evol score (if fewer 
	// we don't calculate anything becase it would be too unreliable statistically)
	public static final int        MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE = 4;
	// value to use when core/rim ratio is infinity (rim score=0), some arbitrary large value, unlikely to happen in realistic cases
	public static final double     SCORERATIO_INFINITY_VALUE = 1000;
	// for sequences (strictly) below this length value no blast will be performed
	public static final int		   MIN_SEQ_LENGTH_FOR_BLASTING = 10;
	// the hard limits aka "duarte" limits
	// max limit based on 1pre (bio with 2290 and 0+2 cores) and 2vg5 interface 2 (xtal with 2070 and 0+0 cores) 
	public static final double	   MAX_AREA_XTALCALL = 2200; 
	public static final double 	   MIN_AREA_BIOCALL  = 400;   

	
	// PROPERTY FILES
	protected static final InputStream COLORS_PROPERTIES_IS = CRKParams.class.getResourceAsStream("/resources/chain_colors.dat");
	protected static final InputStream PYMOL_COLOR_MAPPINGS_IS = CRKParams.class.getResourceAsStream("/resources/pymol.colors");
	
	// DEFAULTS FOR COMMAND LINE PARAMETERS
	public static final double    DEF_HOM_SOFT_ID_CUTOFF = 0.6;
	public static final double    DEF_HOM_HARD_ID_CUTOFF = 0.5;

	private static final int      DEF_NUMTHREADS = Runtime.getRuntime().availableProcessors();
	
	private static final String   DEF_OUT_DIR = ".";
	
	// default entropy calculation 
	public static final int       DEF_ENTROPY_ALPHABET = 10;

	// default cutoffs for the final bio/xtal call
	public static final int       DEF_MIN_CORE_SIZE_FOR_BIO = 6;
	public static final double    DEF_ENTR_CALL_CUTOFF = 0.75;
	public static final double    DEF_ZSCORE_CUTOFF = -1.00;
	
	// default core assignment thresholds
	public static final double    DEF_CA_CUTOFF_FOR_GEOM = 0.95;
	public static final double    DEF_CA_CUTOFF_FOR_RIMCORE = 0.70;
	public static final double    DEF_CA_CUTOFF_FOR_ZSCORE = 0.70;

	private static final int      DEF_MAX_NUM_SEQUENCES = 100;
	
	private static final int      DEF_NSPHEREPOINTS_ASA_CALC = 3000;
	
	protected static final HomologsSearchMode DEF_HOMOLOGS_SEARCH_MODE = HomologsSearchMode.AUTO;
	
	private static final AlignmentMode DEF_ALIGNMENT_MODE = AlignmentMode.AUTO;
	
	// DEFAULTS FOR CONFIG FILE ASSIGNABLE CONSTANTS
	// defaults for pdb data location
	private static final String   DEF_LOCAL_CIF_DIR = "/pdbdata/pdb/data/structures/all/mmCIF";
	private static final String   DEF_PDB_FTP_CIF_URL = "ftp://ftp.wwpdb.org/pub/pdb/data/structures/all/mmCIF/";
	private static final boolean  DEF_USE_ONLINE_PDB = false;

	// default sifts file location
	private static final String   DEF_SIFTS_FILE = "ftp://ftp.ebi.ac.uk/pub/databases/msd/sifts/text/pdb_chain_uniprot.lst";	
	private static final boolean  DEF_USE_SIFTS = true;
	
	// default blast settings
	private static final String   DEF_BLAST_BIN_DIR = "/usr/bin";
	private static final String   DEF_BLAST_DATA_DIR = "/usr/share/blast";
	
	// default tcoffee settings
	private static final File     DEF_TCOFFE_BIN = new File("/usr/bin/t_coffee");
	private static final boolean  DEF_USE_TCOFFEE_VERY_FAST_MODE = false;

	// default pymol exec
	private static final File	  DEF_PYMOL_EXE = new File("/usr/bin/pymol");
	
	// default minimum ASA for calling a residue surface
	public static final double    DEF_MIN_ASA_FOR_SURFACE = 5;
	// default minimum number of atoms for a cofactor to be considered for ASA calculations purposes, if -1 all ignored
	public static final int		  DEF_MIN_SIZE_COFACTOR_FOR_ASA = -1;
	
	// default cutoffs
	private static final double   DEF_QUERY_COVERAGE_CUTOFF = 0.85;
	public static final int       DEF_MIN_NUM_SEQUENCES = 10;
	private static final double	  DEF_HOM_ID_STEP = 0.05;
	private static final double   DEF_MIN_QUERY_COV_FOR_IDENTICALS_REMOVAL = 0.85;
	
	// default pdb2uniprot mapping blast thresholds
	private static final double   DEF_PDB2UNIPROT_ID_THRESHOLD = 0.75;
	private static final double   DEF_PDB2UNIPROT_QCOV_THRESHOLD = 0.85;
	// default pdb2uniprot max subject (uniprot) coverage: below this value we do local blast search instead of global (see HomologsSearchMode) 
	protected static final double DEF_PDB2UNIPROT_MAX_SCOV_FOR_LOCAL = 0.5;

	// default cache dirs
	private static final String   DEF_BLAST_CACHE_DIR = null;
	
	// default use uniparc
	private static final boolean  DEF_USE_UNIPARC = true;
	
	// FIELDS
	
	// the parameters
	private String pdbCode;
	private boolean doScoreEntropies;
	private double homSoftIdCutoff;
	private double homHardIdCutoff;
	private double homIdStep;
	private String baseName;
	private File outDir;
	private int numThreads;
	private int reducedAlphabet;
	
	private double caCutoffForGeom;
	private double caCutoffForRimCore;
	private double caCutoffForZscore;
	
	private int    minCoreSizeForBio;
	
	private int maxNumSeqs;
	
	private boolean usePisa;

	private int nSpherePointsASAcalc;

	private double entrCallCutoff;
	
	private double zScoreCutoff;
	
	private File interfSerFile;
	private File chainEvContextSerFile;
	
	private boolean generateThumbnails;
	
	private File progressLogFile;
	private PrintStream progressLog;
	
	private File configFile;
	
	private boolean debug;
	
	private HomologsSearchMode homologsSearchMode;
	
	private AlignmentMode alignmentMode;
	
	private boolean filterByDomain;
	
	// some other fields
	private File inFile;
	private String jobName;
	
	// fields assignable from config file
	private String   localCifDir;
	private String   pdbFtpCifUrl;
	private boolean  useOnlinePdb;
	
	private String   siftsFile;
	
	private boolean  useSifts;
	
	private String   blastBinDir;
	
	private String   blastDataDir; // dir with blosum matrices only needed for blastclust
	
	private File     tcoffeeBin;
	private boolean  useTcoffeeVeryFastMode;
	
	private File     selectonBin;
	private double   selectonEpsilon;

	private File	 pymolExe;
	
	private double minAsaForSurface;
	private int minSizeCofactorForAsa;	
	
	private double   queryCoverageCutoff;
	private int      minNumSeqs;
	
	private double   minQueryCovForIdenticalsRemoval;
	
	private double   pdb2uniprotIdThreshold;
	private double   pdb2uniprotQcovThreshold;
	private double   pdb2uniprotMaxScovForLocal;
			
	private String   blastCacheDir;
	
	private boolean  useUniparc;
	
	// and finally the ones with no defaults
	private String   blastDbDir; // no default
	private String   blastDb;    // no default
	
	private String   localUniprotDbName; // no default
	private String   localTaxonomyDbName; // no default
	
	/**
	 * 
	 */
	public CRKParams() {
		setDefaults();
	}
	
	private void setDefaults() {
		
		this.pdbCode = null;
		this.doScoreEntropies = false;
		this.homSoftIdCutoff = DEF_HOM_SOFT_ID_CUTOFF;
		this.homHardIdCutoff = DEF_HOM_HARD_ID_CUTOFF;
		this.homIdStep = DEF_HOM_ID_STEP;
		this.baseName = null;
		this.outDir = new File(DEF_OUT_DIR);
		this.numThreads = DEF_NUMTHREADS;
		this.reducedAlphabet = DEF_ENTROPY_ALPHABET;
		this.caCutoffForGeom = DEF_CA_CUTOFF_FOR_GEOM;
		this.caCutoffForRimCore = DEF_CA_CUTOFF_FOR_RIMCORE;
		this.caCutoffForZscore = DEF_CA_CUTOFF_FOR_ZSCORE;
		this.minCoreSizeForBio = DEF_MIN_CORE_SIZE_FOR_BIO;
		this.maxNumSeqs = DEF_MAX_NUM_SEQUENCES;
		this.usePisa = false;
		this.nSpherePointsASAcalc = DEF_NSPHEREPOINTS_ASA_CALC;
		this.entrCallCutoff = DEF_ENTR_CALL_CUTOFF;
		this.zScoreCutoff = DEF_ZSCORE_CUTOFF;
		this.interfSerFile = null;
		this.chainEvContextSerFile = null;
		this.generateThumbnails = false;
		this.progressLog = System.out;
		this.debug = false;
		this.homologsSearchMode = DEF_HOMOLOGS_SEARCH_MODE;
		this.alignmentMode = DEF_ALIGNMENT_MODE;
		this.filterByDomain = false;

	}
	
	public void parseCommandLine(String[] args, String programName, String help) {
	

		Getopt g = new Getopt(programName, args, "i:sa:b:o:r:e:c:z:m:x:y:d:D:q:H:G:OpA:I:C:lL:g:uh?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				setPdbCode(g.getOptarg());
				break;
			case 's':
				doScoreEntropies = true;
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
			case 'e':
				caCutoffForGeom = Double.parseDouble(g.getOptarg());
				break;
			case 'c':
				caCutoffForRimCore = Double.parseDouble(g.getOptarg());
				break;				
			case 'z':
				caCutoffForZscore = Double.parseDouble(g.getOptarg());
				break;				
			case 'm':
				minCoreSizeForBio = Integer.parseInt(g.getOptarg());
				break;
			case 'x':
				entrCallCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'y':
				zScoreCutoff = Double.parseDouble(g.getOptarg());
				break;				
			case 'd':
				homSoftIdCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'D':
				homHardIdCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'q':
				maxNumSeqs = Integer.parseInt(g.getOptarg());
				break;
			case 'H':
				homologsSearchMode = HomologsSearchMode.getByName(g.getOptarg());
				break;
			case 'G':
				alignmentMode = AlignmentMode.getByName(g.getOptarg());
				break;
			case 'O':
				filterByDomain = true;
				break;
			case 'p':
				usePisa = true;
				break;
			case 'A':
				nSpherePointsASAcalc = Integer.parseInt(g.getOptarg());
				break;
			case 'I':
				interfSerFile = new File(g.getOptarg());
				break;
			case 'C':
				chainEvContextSerFile = new File(g.getOptarg());
				break;
			case 'l':
				generateThumbnails = true;
				break;
			case 'L':
				progressLogFile = new File(g.getOptarg());
				break;
			case 'g':
				configFile = new File(g.getOptarg());
				break;
			case 'u':
				debug = true;
				break;
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break; // getopt() already printed an error
			}
		}
	}
	
	public void parseCommandLine(String[] args) throws CRKException {
		
		String help = "Usage: \n" +
		PROGRAM_NAME+" ver. "+PROGRAM_VERSION+"\n" +
		"   -i <string> :  input PDB code or PDB/mmCIF file\n" +
		"  [-s]         :  calculate evolutionary entropy-based scores (core-rim and \n" +
		"                  core-surface).\n" +
		"                  If not specified, only geometric scoring is done.\n"+
		"  [-a <int>]   :  number of threads for blast, t-coffee and ASA calculation. Default: "+DEF_NUMTHREADS+"\n"+
		"  [-b <string>]:  basename for output files. Default: as input PDB code or file name\n"+
		"  [-o <dir>]   :  output dir, where output files will be written. Default: current\n" +
		"                  dir \n" +
		"  [-r <int>]   :  specify the number of groups of aminoacids (reduced alphabet) to\n" +
		"                  be used for entropy calculations.\n" +
		"                  Valid values are 2, 4, 6, 8, 10, 15 and 20. Default: "+DEF_ENTROPY_ALPHABET+"\n" +
		"  [-e <float>] :  the BSA/ASA cutoff for core assignment in geometry predictor.\n" +
		"                  Default: "+String.format("%4.2f",DEF_CA_CUTOFF_FOR_GEOM)+"\n" +
		"  [-c <float>] :  the BSA/ASA cutoff for core assignment in core-rim evolutionary \n" +
		"                  predictor. Default: "+String.format("%4.2f",DEF_CA_CUTOFF_FOR_RIMCORE)+"\n" +
		"  [-z <float>] :  the BSA/ASA cutoff for core assignment in core-surface \n" +
		"                  evolutionary predictor. Default: "+String.format("%4.2f",DEF_CA_CUTOFF_FOR_ZSCORE)+"\n" +
		"  [-m <int>]   :  geometry scoring cutoff for number of interface core residues, if \n" +
		"                  below this value the geometry call will be XTAL, if equals or \n" +
		"                  higher the geometry call is BIO. Default "+DEF_MIN_CORE_SIZE_FOR_BIO+"\n" +
		"  [-x <float>] :  core-rim score cutoff for calling BIO/XTAL. If below this score, \n" +
		"                  interface is BIO, if above XTAL. Default: " + String.format("%4.2f",DEF_ENTR_CALL_CUTOFF)+"\n"+
		"  [-y <float>] :  core-surface score cutoff to call BIO/XTAL. If below this score, \n" +
		"                  interface is BIO, if above XTAL. Default: " + String.format("%4.2f",DEF_ZSCORE_CUTOFF)+"\n"+
		"  [-d <float>] :  sequence identity soft cutoff, if enough homologs ("+DEF_MIN_NUM_SEQUENCES+") above this \n" +
		"                  threshold the search for homologs stops, default: "+String.format("%3.1f",DEF_HOM_SOFT_ID_CUTOFF)+"\n"+
		"  [-D <float>] :  sequence identity hard cutoff, if after applying the soft\n" +
		"                  cutoff (see -d), not enough homologs ("+DEF_MIN_NUM_SEQUENCES+") are found\n" +
		"                  then the threshold is lowered in "+String.format("%4.2f",DEF_HOM_ID_STEP)+" steps until this hard\n" +
		"                  cutoff is reached. \n" +
		"                  Default: "+String.format("%3.1f",DEF_HOM_HARD_ID_CUTOFF)+"\n"+
		"  [-q <int>]   :  maximum number of sequences to keep for calculation of conservation \n" +
		"                  scores. Default: "+DEF_MAX_NUM_SEQUENCES+"\n"+
		"  [-H <string>]:  homologs search mode: one of \"local\" (only Uniprot region covered\n" +
		"                  by PDB structure will be used to search homologs), \"global\" (full\n" +
		"                  Uniprot entry will be used to search homologs) or \"auto\" (global\n" +
		"                  will be used except if coverage is under "+String.format("%3.1f",DEF_PDB2UNIPROT_MAX_SCOV_FOR_LOCAL)+").\n" +
		"                  Default "+DEF_HOMOLOGS_SEARCH_MODE.getName() + "\n"+
		"  [-G <string>]:  alignment mode for t-coffee multiple sequence alignment: one of \"full\"\n" +
		"                  (full homolog sequences will be used for alignment) \"hsp\" (only blast\n" +
		"                  HSP matching homolog subsequences will be used) or \"auto\" (one of the\n" +
		"                  2 modes is decided based on the homologs search mode: full if global\n" +
		"                  search mode or hsp if local search mode)\n"+
		"  [-O]         :  restrict homologs search to those within the same domain of life as \n" +
		"                  query\n"+
		"  [-p]         :  use PISA interface enumeration (will be downloaded from web) \n" +
		"                  instead of ours (only possible for existing PDB entries).\n" +
		"  [-A <int>]   :  number of sphere points for ASA calculation, this parameter controls\n" +
		"                  the accuracy of the ASA calculations, the bigger the more accurate \n" +
		"                  (and slower). Default: "+DEF_NSPHEREPOINTS_ASA_CALC+"\n" +
		"  [-I <file>]  :  binary file containing the interface enumeration output of a previous \n" +
		"                  run of "+PROGRAM_NAME+"\n" +
		"  [-C <file>]  :  binary file containing the evolutionary scores for a particular \n" +
		"                  sequence output of a previous run of "+PROGRAM_NAME+"\n" +
		"  [-l]         :  if specified thumbnail images will be generated for each interface \n" +
		"                  (requires pymol)\n" +
		"  [-L <file>]  :  a file where progress log will be written to. Default: progress log \n" +
		"                  written to std output\n" +
		"  [-g <file>]  :  an eppic config file. This will override the existing config \n" +
		"                  file in the user's home directory\n" +
		"  [-u]         :  debug, if specified debug output will be also shown on standard\n" +
		"                  output\n\n";
		
		parseCommandLine(args, PROGRAM_NAME, help);
		checkCommandLineInput();

	}

	public void checkCommandLineInput() throws CRKException {
		
		if (pdbCode==null) {
			throw new CRKException(null, "Missing argument -i", true);
		}
		
		if (inFile!=null && !inFile.exists()){
			throw new CRKException(null, "Given file "+inFile+" does not exist!", true);
		}
		
		if (baseName==null) {
			baseName=pdbCode;
			if (inFile!=null) {
				baseName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
			}
		}
		
		if (progressLogFile!=null) {
			try {
				progressLog = new PrintStream(progressLogFile);
			} catch (FileNotFoundException e) {
				throw new CRKException(e, "Specified log file can not be written to: "+e.getMessage(), true);
			}
		} 
		
		if (configFile!=null && !configFile.exists()) {
			throw new CRKException(null, "Specified config file "+configFile+" doesn't exist",true);
		}
		
		if (!AminoAcid.isValidNumGroupsReducedAlphabet(reducedAlphabet)) {
			throw new CRKException(null, "Invalid number of amino acid groups specified ("+reducedAlphabet+")", true);
		}
		
		if (usePisa && inFile!=null) {
			throw new CRKException(null, "Can only get PISA interface enumeration for a PDB code. Can't use '-p' if the PDB given is a file", true);
		}
		
		if (homSoftIdCutoff<homHardIdCutoff) {
			homHardIdCutoff = homSoftIdCutoff;
		}


	}
	
	public void checkConfigFileInput() throws CRKException {
		
		if (!isInputAFile()) {
			if (localCifDir==null || ! new File(localCifDir).isDirectory()) {
				throw new CRKException(null,
				"To be able to use PDB codes as input with -i option a valid LOCAL_CIF_DIR must be set in config file. " +
				"It must contain the PDB mmCIF compressed file repository as in "+DEF_PDB_FTP_CIF_URL,true);
			}
			
		}
		
		if (isDoScoreEntropies()) {
			if (blastDbDir==null || ! new File(blastDbDir).isDirectory()) {
				throw new CRKException(null,"BLAST_DB_DIR has not been set to a valid value in config file",true);
			}
			if (blastDb==null) {
				throw new CRKException(null,"BLAST_DB has not been set to a valid value in config file",true);
			} else {
				// .00.xxx or .xxx with xxx one of phr, pin, psd, psi, psq
				File dbFile1 = new File(blastDbDir,blastDb+".00.phr"); 
				File dbFile2 = new File(blastDbDir,blastDb+".phr");
				if (!dbFile1.exists() && !dbFile2.exists()){
					throw new CRKException(null,dbFile1+" or "+dbFile2+" blast index files not present. Please set correct values of BLAST_DB_DIR and BLAST_DB in config file",true);
				}
				
			}
			if (! new File(blastBinDir).isDirectory()) {
				throw new CRKException(null,"BLAST_BIN_DIR must be set to a valid value in config file. Directory "+blastBinDir+" doesn't exist.",true);
			} 
			else if (!new File(blastBinDir,BlastRunner.BLASTALL_PROG).exists() || 
					 !new File(blastBinDir,BlastRunner.BLASTCLUST_PROG).exists()) {
				throw new CRKException(null,"BLAST_BIN_DIR parameter in config file must be set to a dir containing the "+
						BlastRunner.BLASTALL_PROG+" and "+BlastRunner.BLASTCLUST_PROG+" executables. " +
						"Either "+BlastRunner.BLASTALL_PROG+" or "+BlastRunner.BLASTCLUST_PROG+" are not in given dir "+blastBinDir,true);				
			}
			if (! new File(blastDataDir).isDirectory()) {
				throw new CRKException(null,"BLAST_DATA_DIR must be set to a valid value in config file. Directory "+blastDataDir+ " doesn't exist.",true);
			} else if (! new File(blastDataDir,"BLOSUM62").exists()) {
				throw new CRKException(null, "BLAST_DATA_DIR parameter in config file must be set to a dir containing a blast BLOSUM62 file. No BLOSUM62 file in "+blastDataDir, true);
			}
			if (! tcoffeeBin.exists()) {
				throw new CRKException(null,"TCOFFEE_BIN must be set to a valid value in config file. File "+tcoffeeBin+" doesn't exist.",true);
			}
		}
	}
	
	/**
	 * Returns the job name of this CRK parameter set. The job name will be the PDB 
	 * code given or if a file given the file name without the extension (everything
	 * after last dot).
	 * @return
	 */
	public String getJobName() {
		if (jobName!=null) {
			return jobName;
		}
		jobName = pdbCode; // the name to be used in many of the output files
		if (inFile!=null) {
			jobName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
		}
		return jobName;
	}
	
	public boolean isInputAFile() {
		return inFile!=null;
	}
	
	public File getInFile() {
		return inFile;
	}
	
	public File getOutputFile(String suffix) {
		return new File(outDir,baseName+suffix);
	}
	
	public String getPdbCode() {
		return pdbCode;
	}
	
	public void setPdbCode(String pdbCode) {
		inFile = new File(pdbCode);
		Matcher m = PDBCODE_PATTERN.matcher(pdbCode);
		if (m.matches()) {
			inFile = null;
		}
		this.pdbCode = pdbCode.toLowerCase();
	}
	
	public boolean isDoScoreEntropies() {
		return doScoreEntropies;
	}
	
	public double getHomSoftIdCutoff() {
		return homSoftIdCutoff;
	}
	
	public void setHomSoftIdCutoff(double homSoftIdCutoff) {
		this.homSoftIdCutoff = homSoftIdCutoff;
	}
	
	public double getHomHardIdCutoff() {
		return homHardIdCutoff;
	}
	
	public void setHomHardCutoff(double homHardIdCutoff) {
		this.homHardIdCutoff = homHardIdCutoff;
	}
	
	public double getHomIdStep() {
		return homIdStep;
	}

	public String getBaseName() {
		return baseName;
	}
	
	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}
	
	public File getOutDir() {
		return outDir;
	}
	
	public void setOutDir(File outDir) {
		this.outDir = outDir;
	}
	
	public int getNumThreads() {
		return numThreads;
	}
	
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
	
	public int getReducedAlphabet() {
		return reducedAlphabet;
	}
	
	public void setReducedAlphabet(int reducedAlphabet) {
		this.reducedAlphabet = reducedAlphabet;
	}
	
	/**
	 * Returns the maximum theoretical value that entropy can have for an 
	 * alignment column (for entropies defined with log base 2). 
	 * The value is log(number of aminoacid classes)
	 * @return
	 */
	public double getMaxEntropy() {
		return Math.log(reducedAlphabet)/Math.log(2);
	}
	
	public boolean isUseTcoffeeVeryFastMode() {
		return useTcoffeeVeryFastMode;
	}
	
	public void setUseTcoffeeVeryFastMode(boolean useTcoffeeVeryFastMode) {
		this.useTcoffeeVeryFastMode = useTcoffeeVeryFastMode;
	}

	public double getCAcutoffForGeom() {
		return caCutoffForGeom;
	}
	
	public double getCAcutoffForRimCore(){
		return caCutoffForRimCore;
	}
	
	public double getCAcutoffForZscore() {
		return caCutoffForZscore;
	}
	
	public double getMinAsaForSurface() {
		return minAsaForSurface;
	}
	
	public int getMinSizeCofactorForAsa() {
		return minSizeCofactorForAsa;
	}
	
	public int getMinCoreSizeForBio() {
		return minCoreSizeForBio;
	}
	
	public double getSelectonEpsilon() {
		return selectonEpsilon;
	}
	
	public void setSelectonEpsilon(double selectonEpsilon) {
		this.selectonEpsilon = selectonEpsilon;
	}
	
	public int getMaxNumSeqs() {
		return maxNumSeqs;
	}
	
	public void setMaxNumSeqs(int maxNumSeqs) {
		this.maxNumSeqs = maxNumSeqs;
	}
	
	public boolean isUsePisa() {
		return usePisa;
	}
	public void setUsePisa(boolean usePisa) {
		this.usePisa = usePisa;
	}

	public int getnSpherePointsASAcalc() {
		return nSpherePointsASAcalc;
	}

	public void setnSpherePointsASAcalc(int nSpherePointsASAcalc) {
		this.nSpherePointsASAcalc = nSpherePointsASAcalc;
	}
	
	public double getEntrCallCutoff() {
		return entrCallCutoff;
	}

	public void setEntrCallCutoff(double entrCallCutoff) {
		this.entrCallCutoff = entrCallCutoff;
	}
	
	public double getZscoreCutoff(){
		return zScoreCutoff;
	}

	public File getInterfSerFile() {
		return interfSerFile;
	}


	public void setInterfSerFile(File interfSerFile) {
		this.interfSerFile = interfSerFile;
	}


	public File getChainEvContextSerFile() {
		return chainEvContextSerFile;
	}


	public void setChainEvContextSerFile(File chainEvContextSerFile) {
		this.chainEvContextSerFile = chainEvContextSerFile;
	}

	public void setInFile(File inFile) {
		this.inFile = inFile;
	}
	
	public boolean isGenerateThumbnails() {
		return generateThumbnails;
	}
	
	public PrintStream getProgressLog() {
		return progressLog;
	}
	
	public File getProgressLogFile() {
		return progressLogFile;
	}
	
	public File getConfigFile() {
		return configFile;
	}
	
	public boolean getDebug() {
		return debug;
	}
	
	public HomologsSearchMode getHomologsSearchMode() {
		return homologsSearchMode;
	}
	
	public void setHomologsSearchMode(HomologsSearchMode homologsSearchMode) {
		this.homologsSearchMode = homologsSearchMode;
	}
	
	public AlignmentMode getAlignmentMode() {
		return alignmentMode;
	}
	
	public void setAlignmentMode(AlignmentMode alignmentMode) {
		this.alignmentMode = alignmentMode;
	}
	
	public boolean isFilterByDomain() {
		return filterByDomain;
	}
	
	public void setIsFilterByDomain(boolean filterByDomain) {
		this.filterByDomain = filterByDomain;
	}

	public void readConfigFile(File file) throws FileNotFoundException, IOException { 
		Properties p = new Properties();
		p.load(new FileInputStream(file));

		try {
			// variables without defaults
			blastDbDir    	= p.getProperty("BLAST_DB_DIR");
			blastDb        	= p.getProperty("BLAST_DB");
			
			localUniprotDbName = p.getProperty("LOCAL_UNIPROT_DB_NAME");
			localTaxonomyDbName = p.getProperty("LOCAL_TAXONOMY_DB_NAME");

			localCifDir   	= p.getProperty("LOCAL_CIF_DIR", DEF_LOCAL_CIF_DIR);
			pdbFtpCifUrl 	= p.getProperty("PDB_FTP_URL", DEF_PDB_FTP_CIF_URL);
			useOnlinePdb  	= Boolean.parseBoolean(p.getProperty("USE_ONLINE_PDB", new Boolean(DEF_USE_ONLINE_PDB).toString()));
			
			siftsFile       = p.getProperty("SIFTS_FILE", DEF_SIFTS_FILE);
			useSifts        = Boolean.parseBoolean(p.getProperty("USE_SIFTS", new Boolean(DEF_USE_SIFTS).toString()));
			
			blastBinDir     = p.getProperty("BLAST_BIN_DIR", DEF_BLAST_BIN_DIR);
			
			blastDataDir    = p.getProperty("BLAST_DATA_DIR", DEF_BLAST_DATA_DIR);
			
			tcoffeeBin 		= new File(p.getProperty("TCOFFEE_BIN", DEF_TCOFFE_BIN.toString()));
			
			useTcoffeeVeryFastMode = Boolean.parseBoolean(p.getProperty("USE_TCOFFEE_VERY_FAST_MODE",new Boolean(DEF_USE_TCOFFEE_VERY_FAST_MODE).toString()));
			
			pymolExe		= new File(p.getProperty("PYMOL_EXE", DEF_PYMOL_EXE.toString()));
			
			minAsaForSurface = Double.parseDouble(p.getProperty("MIN_ASA_FOR_SURFACE", new Double(DEF_MIN_ASA_FOR_SURFACE).toString()));
			
			minSizeCofactorForAsa = Integer.parseInt(p.getProperty("MIN_SIZE_COFACTOR_FOR_ASA", new Integer(DEF_MIN_SIZE_COFACTOR_FOR_ASA).toString()));

			queryCoverageCutoff = Double.parseDouble(p.getProperty("QUERY_COVERAGE_CUTOFF", new Double(DEF_QUERY_COVERAGE_CUTOFF).toString()));
			minNumSeqs = Integer.parseInt(p.getProperty("MIN_NUM_SEQUENCES", new Integer(DEF_MIN_NUM_SEQUENCES).toString()));
			
			minQueryCovForIdenticalsRemoval = Double.parseDouble(p.getProperty("MIN_QUERY_COV_FOR_IDENTICALS_REMOVAL", new Double(DEF_MIN_QUERY_COV_FOR_IDENTICALS_REMOVAL).toString()));
			
			homIdStep = Double.parseDouble(p.getProperty("HOM_ID_STEP",new Double(DEF_HOM_ID_STEP).toString()));
			
			pdb2uniprotIdThreshold = Double.parseDouble(p.getProperty("PDB2UNIPROT_ID_THRESHOLD", new Double(DEF_PDB2UNIPROT_ID_THRESHOLD).toString()));
			pdb2uniprotQcovThreshold = Double.parseDouble(p.getProperty("PDB2UNIPROT_QCOV_THRESHOLD", new Double(DEF_PDB2UNIPROT_QCOV_THRESHOLD).toString()));
			
			pdb2uniprotMaxScovForLocal = Double.parseDouble(p.getProperty("PDB2UNIPROT_MAX_SCOV_FOR_LOCAL", new Double(DEF_PDB2UNIPROT_MAX_SCOV_FOR_LOCAL).toString()));
					
			blastCacheDir    = p.getProperty("BLAST_CACHE_DIR", DEF_BLAST_CACHE_DIR);
			
			useUniparc       = Boolean.parseBoolean(p.getProperty("USE_UNIPARC",new Boolean(DEF_USE_UNIPARC).toString()));
			
		} catch (NumberFormatException e) {
			System.err.println("A numerical value in the config file was incorrectly specified: "+e.getMessage()+".\n" +
					"Please check the config file.");
			System.exit(1);
		}
	}

	public String getLocalCifDir() {
		return localCifDir;
	}

	public String getPdbFtpCifUrl() {
		return pdbFtpCifUrl;
	}

	public boolean isUseOnlinePdb() {
		return useOnlinePdb;
	}

	public String getSiftsFile() {
		return siftsFile;
	}
	
	public boolean isUseSifts() {
		return useSifts;
	}

	public String getBlastBinDir() {
		return blastBinDir;
	}

	public String getBlastDataDir() {
		return blastDataDir;
	}
	
	public File getTcoffeeBin() {
		return tcoffeeBin;
	}

	public File getSelectonBin() {
		return selectonBin;
	}

	public File getPymolExe() {
		return pymolExe;
	}

	public double getQueryCoverageCutoff() {
		return queryCoverageCutoff;
	}

	public int getMinNumSeqs() {
		return minNumSeqs;
	}

	public double getMinQueryCovForIdenticalsRemoval() {
		return minQueryCovForIdenticalsRemoval;
	}
	
	public double getPdb2uniprotIdThreshold() {
		return pdb2uniprotIdThreshold;
	}

	public double getPdb2uniprotQcovThreshold() {
		return pdb2uniprotQcovThreshold;
	}

	public double getPdb2uniprotMaxScovForLocal() {
		return pdb2uniprotMaxScovForLocal;
	}
	
	public String getBlastCacheDir() {
		return blastCacheDir;
	}

	public String getBlastDbDir() {
		return blastDbDir;
	}

	public String getBlastDb() {
		return blastDb;
	}
	
	public String getLocalUniprotDbName() {
		return localUniprotDbName;
	}
	
	public String getLocalTaxonomyDbName() {
		return localTaxonomyDbName;
	}
	
	public boolean isUseUniparc() {
		return useUniparc;
	}
	

}
