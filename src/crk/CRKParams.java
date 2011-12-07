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

import owl.core.structure.AminoAcid;

public class CRKParams {
	
	// CONSTANTS
	private static final String    PROGRAM_NAME = "crk";
	public static final String	   PROGRAM_VERSION = "2.0.0-alpha1";
	private static final Pattern   PDBCODE_PATTERN = Pattern.compile("^\\d\\w\\w\\w$");
	protected static final String  CONFIG_FILE_NAME = ".crk.conf";
	protected static final String  GEOMETRY_FILE_SUFFIX = ".geometry";
	protected static final String  ENTROPIES_FILE_SUFFIX = ".entropies";
	protected static final String  KAKS_FILE_SUFFIX = ".kaks";
	protected static final String  ZSCORES_FILE_SUFFIX = ".zscores";
	protected static final String  COMBINED_FILE_SUFFIX = ".combined";
	public static final double     INTERFACE_DIST_CUTOFF = 5.9;
	// shorter chains will be considered peptides
	public static final int	       PEPTIDE_LENGTH_CUTOFF = 20; 
	// 10% maximum allowed unreliable residues for calling nopred
	public static final double     MAX_ALLOWED_UNREL_RES = 0.1; 
	// minimum number of core residues per interface member to calculate evol score (if fewer 
	// we don't calculate anything becase it would be too unreliable statistically)
	public static final int        MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE = 4;
	// value to use when core/rim ratio is infinity (rim score=0), some arbitrary large value, unlikely to happen in realistic cases
	public static final double     SCORERATIO_INFINITY_VALUE = 1000;

	// PROPERTY FILES
	protected static final InputStream COLORS_PROPERTIES_IS = CRKParams.class.getResourceAsStream("/resources/chain_colors.dat");
	protected static final InputStream PYMOL_COLOR_MAPPINGS_IS = CRKParams.class.getResourceAsStream("/resources/pymol.colors");
	
	// DEFAULTS FOR COMMAND LINE PARAMETERS
	private static final double   DEF_HOM_SOFT_ID_CUTOFF = 0.6;
	private static final double   DEF_HOM_HARD_ID_CUTOFF = 0.5;

	private static final int      DEF_NUMTHREADS = Runtime.getRuntime().availableProcessors();
	
	private static final String   DEF_OUT_DIR = ".";
	
	// default entropy calculation 
	private static final int      DEF_ENTROPY_ALPHABET = 10;

	// default cutoffs for the final bio/xtal call
	private static final double   DEF_ENTR_CALL_CUTOFF = 0.85;
	private static final double   DEF_KAKS_CALL_CUTOFF = 0.85;
	private static final double   DEF_ZSCORE_CUTOFF = -1.0;
	
	// default core assignment thresholds
	private static final double   DEF_CA_CUTOFF_FOR_GEOM = 0.95;
	private static final int      DEF_MIN_CORE_SIZE_FOR_BIO = 6;
	private static final double   DEF_CA_CUTOFF_FOR_RIMCORE = 0.70;
	private static final double   DEF_CA_CUTOFF_FOR_ZSCORE = 0.70;

	private static final int      DEF_MAX_NUM_SEQUENCES = 60;
	
	private static final int      DEF_NSPHEREPOINTS_ASA_CALC = 9600;
	
	private static final HomologsSearchMode DEF_HOMOLOGS_SEARCH_MODE = HomologsSearchMode.AUTO;
	
	// DEFAULTS FOR CONFIG FILE ASSIGNABLE CONSTANTS
	// defaults for pdb data location
	private static final String   DEF_LOCAL_CIF_DIR = "/pdbdata/pdb/data/structures/all/mmCIF";
	private static final String   DEF_PDB_FTP_CIF_URL = "ftp://ftp.wwpdb.org/pub/pdb/data/structures/all/mmCIF/";
	private static final boolean  DEF_USE_ONLINE_PDB = false;

	// default sifts file location
	private static final String   DEF_SIFTS_FILE = "ftp://ftp.ebi.ac.uk/pub/databases/msd/sifts/text/pdb_chain_uniprot.lst";	
	
	// default blast settings
	private static final String   DEF_BLAST_BIN_DIR = "/usr/bin";
	
	// default tcoffee settings
	private static final File     DEF_TCOFFE_BIN = new File("/usr/bin/t_coffee");
	private static final boolean  DEF_USE_TCOFFEE_VERY_FAST_MODE = false;

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
	private static final double	  DEF_HOM_ID_STEP = 0.05;;
	// default pdb2uniprot mapping blast thresholds
	private static final double   DEF_PDB2UNIPROT_ID_THRESHOLD = 0.75;
	private static final double   DEF_PDB2UNIPROT_QCOV_THRESHOLD = 0.85;
	// default pdb2uniprot max subject (uniprot) coverage: below this value we do local blast search instead of global (see HomologsSearchMode) 
	private static final double   DEF_PDB2UNIPROT_MAX_SCOV_FOR_LOCAL = 0.4;
		
	// default cache dirs
	private static final String   DEF_EMBL_CDS_CACHE_DIR = null;
	private static final String   DEF_BLAST_CACHE_DIR = null;
	
	
	// FIELDS
	
	// the parameters
	private String pdbCode;
	private boolean doScoreEntropies;
	private boolean doScoreKaks;
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

	private boolean useNaccess;
	
	private int nSpherePointsASAcalc;

	private double entrCallCutoff;
	private double kaksCallCutoff;
	
	private double zScoreCutoff;
	
	private File interfSerFile;
	private File chainEvContextSerFile;
	
	private boolean generateThumbnails;
	
	private File progressLogFile;
	private PrintStream progressLog;
	
	private boolean debug;
	
	private HomologsSearchMode homologsSearchMode;
	
	// some other fields
	private File inFile;
	private String jobName;
	
	// fields assignable from config file
	private String   localCifDir;
	private String   pdbFtpCifUrl;
	private boolean  useOnlinePdb;
	
	private String   siftsFile;
	
	private String   blastBinDir;
	
	private File     tcoffeeBin;
	private boolean useTcoffeeVeryFastMode;
	
	private File     selectonBin;
	private double   selectonEpsilon;

	private File     naccessExe;
	
	private File	 pymolExe;
	
	private double   queryCoverageCutoff;
	private int      minHomologsCutoff;
	
	private double   pdb2uniprotIdThreshold;
	private double   pdb2uniprotQcovThreshold;
	private double   pdb2uniprotMaxScovForLocal;
			
	private String   emblCdsCacheDir;
	private String   blastCacheDir;
	
	// and finally the ones with no defaults
	private String   blastDbDir; // no default
	private String   blastDb;    // no default
	
	/**
	 * 
	 */
	public CRKParams() {
		setDefaults();
	}
	
	private void setDefaults() {
		
		this.pdbCode = null;
		this.doScoreEntropies = false;
		this.doScoreKaks = false;
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
		this.useNaccess = false;
		this.nSpherePointsASAcalc = DEF_NSPHEREPOINTS_ASA_CALC;
		this.entrCallCutoff = DEF_ENTR_CALL_CUTOFF;
		this.kaksCallCutoff = DEF_KAKS_CALL_CUTOFF;
		this.zScoreCutoff = DEF_ZSCORE_CUTOFF;
		this.interfSerFile = null;
		this.chainEvContextSerFile = null;
		this.generateThumbnails = false;
		this.progressLog = System.out;
		this.debug = false;
		this.homologsSearchMode = DEF_HOMOLOGS_SEARCH_MODE;
	}
	
	public void parseCommandLine(String[] args, String programName, String help) {
	

		Getopt g = new Getopt(programName, args, "i:ska:b:o:r:e:c:z:m:x:X:y:d:D:q:H:pnA:I:C:lL:uh?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				setPdbCode(g.getOptarg());
				break;
			case 's':
				doScoreEntropies = true;
				break;
			case 'k':
				doScoreKaks = true;
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
			case 'X':
				kaksCallCutoff = Double.parseDouble(g.getOptarg());
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
			case 'p':
				usePisa = true;
				break;
			case 'n':
				useNaccess = true;
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
		PROGRAM_NAME+"\n" +
		"   -i          :  input PDB code or PDB file or mmCIF file\n" +
		"  [-s]         :  score based on entropies. Using the entropy values both a core/rim\n" +
		"                  score and a core/surface z-score are calculated \n"+
		"  [-k]         :  score based on ka/ks ratios. Slower than entropies, \n" +
		"                  requires running of the selecton external program\n" +
		"  [-a <int>]   :  number of threads for blast and ASA calculation. Default: "+DEF_NUMTHREADS+"\n"+
		"  [-b <str>]   :  basename for output files. Default: PDB code \n"+
		"  [-o <dir>]   :  output dir, where output files will be written. Default: current\n" +
		"                  dir \n" +
		"  [-r <int>]   :  specify the number of groups of aminoacids (reduced alphabet) to\n" +
		"                  be used for entropy calculations.\n" +
		"                  Valid values are 2, 4, 6, 8, 10, 15 and 20. Default: "+DEF_ENTROPY_ALPHABET+"\n" +
		"  [-e <float>] :  the BSA/ASA cutoff for core assignment in geometry predictor.\n" +
		"                  Default: "+String.format("%4.2f",DEF_CA_CUTOFF_FOR_GEOM)+"\n" +
		"  [-c <float>] :  the BSA/ASA cutoff for core assignment in rim/core evolutionary \n" +
		"                  predictor. Default: "+String.format("%4.2f",DEF_CA_CUTOFF_FOR_RIMCORE)+"\n" +
		"  [-z <float>] :  the BSA/ASA cutoff for core assignment in core vs surface z-score \n" +
		"                  evolutionary predictor. Default: "+String.format("%4.2f",DEF_CA_CUTOFF_FOR_ZSCORE)+"\n" +
		"  [-m <int>]   :  geometry scoring cutoff for number of interface core residues, if \n" +
		"                  below this value the geometry call will be crystal, if equals or \n" +
		"                  higher the geometry call is bio. Default "+DEF_MIN_CORE_SIZE_FOR_BIO+"\n" +
		"  [-x <float>] :  entropy score cutoff for calling BIO/XTAL.\n" +
		"                  Default: " + String.format("%4.2f",DEF_ENTR_CALL_CUTOFF)+"\n"+
		"  [-X <float>] :  ka/ks score cutoff for calling BIO/XTAL.\n"+
		"                  Default: " + String.format("%4.2f",DEF_KAKS_CALL_CUTOFF)+"\n"+
		"  [-y <float>] :  z-score cutoff to call BIO/XTAL. If below this z-score interface \n" +
		"                  is BIO. Default: " + String.format("%4.2f",DEF_ZSCORE_CUTOFF)+"\n"+
		"  [-d <float>] :  sequence identity soft cut-off, if enough homologs ("+DEF_MIN_HOMOLOGS_CUTOFF+") above this threshold\n" +
		"                  the search for homologs stops, default: "+String.format("%3.1f",DEF_HOM_SOFT_ID_CUTOFF)+"\n"+
		"  [-D <float>] :  sequence identity hard cut-off, if after applying the soft cut-off (see -d), not\n" +
		"                  enough homologs ("+DEF_MIN_HOMOLOGS_CUTOFF+") are found then the threshold is lowered \n"+
		"                  in "+String.format("%4.2f",DEF_HOM_ID_STEP)+" steps until this hard cut-off is reached. \n" +
		"                  Default: "+String.format("%3.1f",DEF_HOM_HARD_ID_CUTOFF)+"\n"+
		"  [-q <int>]   :  maximum number of sequences to keep for calculation of conservation \n" +
		"                  scores. Default: "+DEF_MAX_NUM_SEQUENCES+". This is especially important when using \n" +
		"                  the -k option, with too many sequences, selecton will run too long\n" +
		"                  (and inaccurately because of ks saturation)\n" +
		"  [-H <string>]:  homologues search mode: one of local (only Uniprot region covered by PDB entry \n" +
		"                  will be used to search homologues), global (full Uniprot entry will be used \n" +
		"                  to search homologues) or auto (global will be used except if coverage is under \n"+
		"                  "+String.format("%3.1f",DEF_PDB2UNIPROT_MAX_SCOV_FOR_LOCAL)+"). Default "+DEF_HOMOLOGS_SEARCH_MODE.getName() + "\n"+
		"  [-p]         :  use PISA interface enumeration (will be downloaded from web) \n" +
		"                  instead of ours (only possible for existing PDB entries).\n" +
		"  [-n]         :  use NACCESS for ASA/BSA calculations, otherwise area calculations \n" +
		"                  done with the internal rolling ball algorithm implementation \n" +
		"                  (multi-threaded using number of CPUs specified in -a)\n" +
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
		
		if (!AminoAcid.isValidNumGroupsReducedAlphabet(reducedAlphabet)) {
			throw new CRKException(null, "Invalid number of amino acid groups specified ("+reducedAlphabet+")", true);
		}
		
		if (usePisa && inFile!=null) {
			throw new CRKException(null, "Can only get PISA interface enumeration for a PDB code. Can't use '-p' if the PDB given is a file", true);
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
	public boolean isDoScoreKaks() {
		return doScoreKaks;
	}
	public void setDoScoreKaks(boolean doScoreKaks) {
		this.doScoreKaks = doScoreKaks;
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

	public boolean isUseNaccess() {
		return useNaccess;
	}

	public void setUseNaccess(boolean useNaccess) {
		this.useNaccess = useNaccess;
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
	
	public double getKaksCallCutoff() {
		return kaksCallCutoff;
	}

	public void setKaksCallCutoffs(double kaksCallCutoff) {
		this.kaksCallCutoff = kaksCallCutoff;
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
	
	public boolean getDebug() {
		return debug;
	}
	
	public HomologsSearchMode getHomologsSearchMode() {
		return homologsSearchMode;
	}

	public void readConfigFile(File file) throws FileNotFoundException, IOException { 
		Properties p = new Properties();
		p.load(new FileInputStream(file));

		try {
			// variables without defaults
			blastDbDir    	= p.getProperty("BLAST_DB_DIR");
			blastDb        	= p.getProperty("BLAST_DB");

			localCifDir   	= p.getProperty("LOCAL_CIF_DIR", DEF_LOCAL_CIF_DIR);
			pdbFtpCifUrl 	= p.getProperty("PDB_FTP_URL", DEF_PDB_FTP_CIF_URL);
			useOnlinePdb  	= Boolean.parseBoolean(p.getProperty("USE_ONLINE_PDB", new Boolean(DEF_USE_ONLINE_PDB).toString()));
			
			siftsFile       = p.getProperty("SIFTS_FILE", DEF_SIFTS_FILE);
			
			blastBinDir     = p.getProperty("BLAST_BIN_DIR", DEF_BLAST_BIN_DIR);
			
			tcoffeeBin 		= new File(p.getProperty("TCOFFEE_BIN", DEF_TCOFFE_BIN.toString()));
			
			useTcoffeeVeryFastMode = Boolean.parseBoolean(p.getProperty("USE_TCOFFEE_VERY_FAST_MODE",new Boolean(DEF_USE_TCOFFEE_VERY_FAST_MODE).toString()));
			
			selectonBin 	= new File(p.getProperty("SELECTON_BIN", DEF_SELECTON_BIN.toString()));
			
			selectonEpsilon = Double.parseDouble(p.getProperty("SELECTON_EPSILON", new Double(DEF_SELECTON_EPSILON).toString()));
			
			naccessExe      = new File(p.getProperty("NACCESS_EXE", DEF_NACCESS_EXE.toString()));
			
			pymolExe		= new File(p.getProperty("PYMOL_EXE", DEF_PYMOL_EXE.toString()));

			queryCoverageCutoff = Double.parseDouble(p.getProperty("QUERY_COVERAGE_CUTOFF", new Double(DEF_QUERY_COVERAGE_CUTOFF).toString()));
			minHomologsCutoff = Integer.parseInt(p.getProperty("MIN_HOMOLOGS_CUTOFF", new Integer(DEF_MIN_HOMOLOGS_CUTOFF).toString()));
			homIdStep = Double.parseDouble(p.getProperty("HOM_ID_STEP",new Double(DEF_HOM_ID_STEP).toString()));
			
			pdb2uniprotIdThreshold = Double.parseDouble(p.getProperty("PDB2UNIPROT_ID_THRESHOLD", new Double(DEF_PDB2UNIPROT_ID_THRESHOLD).toString()));
			pdb2uniprotQcovThreshold = Double.parseDouble(p.getProperty("PDB2UNIPROT_QCOV_THRESHOLD", new Double(DEF_PDB2UNIPROT_QCOV_THRESHOLD).toString()));
			
			pdb2uniprotMaxScovForLocal = Double.parseDouble(p.getProperty("PDB2UNIPROT_MAX_SCOV_FOR_LOCAL", new Double(DEF_PDB2UNIPROT_MAX_SCOV_FOR_LOCAL).toString()));
					
			emblCdsCacheDir  = p.getProperty("EMBL_CDS_CACHE_DIR", DEF_EMBL_CDS_CACHE_DIR);
			blastCacheDir    = p.getProperty("BLAST_CACHE_DIR", DEF_BLAST_CACHE_DIR);

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

	public String getBlastBinDir() {
		return blastBinDir;
	}

	public File getTcoffeeBin() {
		return tcoffeeBin;
	}

	public File getSelectonBin() {
		return selectonBin;
	}

	public File getNaccessExe() {
		return naccessExe;
	}

	public File getPymolExe() {
		return pymolExe;
	}

	public double getQueryCoverageCutoff() {
		return queryCoverageCutoff;
	}

	public int getMinHomologsCutoff() {
		return minHomologsCutoff;
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
	
	public String getEmblCdsCacheDir() {
		return emblCdsCacheDir;
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
	

}
