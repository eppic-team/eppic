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
	private static final String   PROGRAM_NAME = "crk";
	private static final Pattern  PDBCODE_PATTERN = Pattern.compile("^\\d\\w\\w\\w$");
	protected static final InputStream COLORS_PROPERTIES_IS = CRKParams.class.getResourceAsStream("/resources/chain_colors.dat");
	
	// DEFAULTS FOR COMMAND LINE PARAMETERS
	private static final double   DEF_IDENTITY_CUTOFF = 0.6;

	private static final int      DEF_NUMTHREADS = Runtime.getRuntime().availableProcessors();
	
	// default entropy calculation default
	private static final int      DEF_ENTROPY_ALPHABET = 10;

	// default cutoffs for the final bio/xtal call
	private static final double   DEF_GRAY_ZONE_WIDTH = 0.01;
	private static final double   DEF_ENTR_CALL_CUTOFF = 0.85;
	private static final double   DEF_KAKS_CALL_CUTOFF = 0.85;
	
	// default crk core assignment thresholds
	private static final double   DEF_SOFT_CUTOFF_CA = 0.95;
	private static final double   DEF_HARD_CUTOFF_CA = 0.82;
	private static final double   DEF_RELAX_STEP_CA = 0.01;	
	private static final double   DEF_CA_CUTOFF = 0.95;
	private static final int      DEF_MIN_NUM_RES_CA = 6;
	private static final int      DEF_MIN_NUM_RES_MEMBER_CA = 3; 

	private static final boolean  DEF_USE_TCOFFEE_VERYFAST_MODE = true;
	
	private static final int      DEF_MAX_NUM_SEQUENCES_SELECTON = 60;
	
	private static final int      DEF_NSPHEREPOINTS_ASA_CALC = 9600;
	
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
	
	
	// FIELDS
	
	// the parameters
	private String pdbCode;
	private boolean doScoreEntropies;
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
	
	private double cutoffCA;
	
	private int      minNumResCA;
	private int      minNumResMemberCA; 
	
	private double selectonEpsilon;

	private int maxNumSeqsSelecton;
	
	private boolean usePisa;

	private boolean useNaccess;
	
	private int nSpherePointsASAcalc;

	private double grayZoneWidth;
	private double entrCallCutoff;
	private double kaksCallCutoff;
	
	private File interfSerFile;
	private File chainEvContextSerFile;
	
	private boolean generateThumbnails;
	
	private File progressLogFile;
	private PrintStream progressLog;
	
	private boolean debug;
	
	
	// some other fields
	private File inFile;
	private String jobName;
	
	// fields assignable from config file
	private String   localCifDir;
	private String   pdbFtpCifUrl;
	private boolean  useOnlinePdb;
	
	private String   pisaInterfacesUrl;
	
	private String   siftsFile;
	
	private String   blastBinDir;
	
	private File     tcoffeeBin;
	
	private File     selectonBin;

	private File     naccessExe;
	
	private File	 pymolExe;
	
	private double   queryCoverageCutoff;
	private int      minHomologsCutoff;
	private double   minInterfAreaReporting; 
	
	private double   pdb2uniprotIdThreshold;
	private double   pdb2uniprotQcovThreshold;
			
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
		this.doScoreCRK = false;
		this.idCutoff = DEF_IDENTITY_CUTOFF;
		this.baseName = null;
		this.outDir = new File(".");
		this.numThreads = DEF_NUMTHREADS;
		this.reducedAlphabet = DEF_ENTROPY_ALPHABET;
		this.useTcoffeeVeryFastMode = DEF_USE_TCOFFEE_VERYFAST_MODE;
		this.zooming = false;
		this.bsaToAsaSoftCutoff = DEF_SOFT_CUTOFF_CA;
		this.bsaToAsaHardCutoff = DEF_HARD_CUTOFF_CA;
		this.relaxationStep = DEF_RELAX_STEP_CA;
		this.cutoffCA = DEF_CA_CUTOFF;
		this.minNumResCA = DEF_MIN_NUM_RES_CA;
		this.minNumResMemberCA = DEF_MIN_NUM_RES_MEMBER_CA;
		this.selectonEpsilon = DEF_SELECTON_EPSILON;
		this.maxNumSeqsSelecton = DEF_MAX_NUM_SEQUENCES_SELECTON;
		this.usePisa = false;
		this.useNaccess = false;
		this.nSpherePointsASAcalc = DEF_NSPHEREPOINTS_ASA_CALC;
		this.grayZoneWidth = DEF_GRAY_ZONE_WIDTH;
		this.entrCallCutoff = DEF_ENTR_CALL_CUTOFF;
		this.kaksCallCutoff = DEF_KAKS_CALL_CUTOFF;
		this.interfSerFile = null;
		this.chainEvContextSerFile = null;
		this.generateThumbnails = false;
		this.progressLog = System.out;
		this.debug = false;
	}
	
	public void parseCommandLine(String[] args, String programName, String help) {
	

		Getopt g = new Getopt(programName, args, "i:skd:a:b:o:r:tc:zZ:m:M:x:X:g:e:q:pnA:I:C:lL:uh?");
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
				cutoffCA = Double.parseDouble(g.getOptarg());
				break;
			case 'z':
				zooming = true;
				break;
			case 'Z':
				String[] ztokens = g.getOptarg().split(",");
				//cutoffsCA = new double[ztokens.length];
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
				entrCallCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'X':
				kaksCallCutoff = Double.parseDouble(g.getOptarg());
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
		"  [-s]         :  score based on entropies \n"+
		"  [-k]         :  score based on ka/ks ratios. Slower than entropies, \n" +
		"                  requires running of the selecton external program\n" +
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
		"  [-c <float>] :  the BSA cutoff for core assignment. Default: "+String.format("%4.2f",DEF_CA_CUTOFF)+"\n" +
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
		"  [-x <float>]:   entropy score cutoff for calling BIO/XTAL.\n" +
		"                  Default: " + String.format("%4.2f",DEF_ENTR_CALL_CUTOFF)+"\n"+
		"  [-X <float>]:   ka/ks score cutoff for calling BIO/XTAL.\n"+
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
	public boolean isDoScoreCRK() {
		return doScoreCRK;
	}
	public void setDoScoreCRK(boolean doScoreCRK) {
		this.doScoreCRK = doScoreCRK;
	}
	public double getIdCutoff() {
		return idCutoff;
	}
	public void setIdCutoff(double idCutoff) {
		this.idCutoff = idCutoff;
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
	public boolean isZooming() {
		return zooming;
	}
	public void setZooming(boolean zooming) {
		this.zooming = zooming;
	}
	public double getBsaToAsaSoftCutoff() {
		return bsaToAsaSoftCutoff;
	}
	public void setBsaToAsaSoftCutoff(double bsaToAsaSoftCutoff) {
		this.bsaToAsaSoftCutoff = bsaToAsaSoftCutoff;
	}
	public double getBsaToAsaHardCutoff() {
		return bsaToAsaHardCutoff;
	}
	public void setBsaToAsaHardCutoff(double bsaToAsaHardCutoff) {
		this.bsaToAsaHardCutoff = bsaToAsaHardCutoff;
	}
	public double getRelaxationStep() {
		return relaxationStep;
	}
	public void setRelaxationStep(double relaxationStep) {
		this.relaxationStep = relaxationStep;
	}
	public double getCutoffCA() {
		return cutoffCA;
	}
	public void setCutoffCA(double cutoffCA) {
		this.cutoffCA = cutoffCA;
	}
	public int getMinNumResCA() {
		return minNumResCA;
	}
	public void setMinNumResCA(int minNumResCA) {
		this.minNumResCA = minNumResCA;
	}
	public int getMinNumResMemberCA() {
		return minNumResMemberCA;
	}
	public void setMinNumResMemberCA(int minNumResMemberCA) {
		this.minNumResMemberCA = minNumResMemberCA;
	}
	public double getSelectonEpsilon() {
		return selectonEpsilon;
	}
	public void setSelectonEpsilon(double selectonEpsilon) {
		this.selectonEpsilon = selectonEpsilon;
	}
	public int getMaxNumSeqsSelecton() {
		return maxNumSeqsSelecton;
	}
	public void setMaxNumSeqsSelecton(int maxNumSeqsSelecton) {
		this.maxNumSeqsSelecton = maxNumSeqsSelecton;
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
	public double getGrayZoneWidth() {
		return grayZoneWidth;
	}
	public void setGrayZoneWidth(double grayZoneWidth) {
		this.grayZoneWidth = grayZoneWidth;
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
			
			pisaInterfacesUrl = p.getProperty("PISA_INTERFACES_URL", DEF_PISA_INTERFACES_URL);
			
			siftsFile       = p.getProperty("SIFTS_FILE", DEF_SIFTS_FILE);
			
			blastBinDir     = p.getProperty("BLAST_BIN_DIR", DEF_BLAST_BIN_DIR);
			
			tcoffeeBin 		= new File(p.getProperty("TCOFFEE_BIN", DEF_TCOFFE_BIN.toString()));
			
			selectonBin 	= new File(p.getProperty("SELECTON_BIN", DEF_SELECTON_BIN.toString()));
			
			naccessExe      = new File(p.getProperty("NACCESS_EXE", DEF_NACCESS_EXE.toString()));
			
			pymolExe		= new File(p.getProperty("PYMOL_EXE", DEF_PYMOL_EXE.toString()));

			queryCoverageCutoff = Double.parseDouble(p.getProperty("QUERY_COVERAGE_CUTOFF", new Double(DEF_QUERY_COVERAGE_CUTOFF).toString()));
			minHomologsCutoff = Integer.parseInt(p.getProperty("MIN_HOMOLOGS_CUTOFF", new Integer(DEF_MIN_HOMOLOGS_CUTOFF).toString()));
			minInterfAreaReporting = Double.parseDouble(p.getProperty("MIN_INTERF_AREA_REPORTING", new Double(DEF_MIN_INTERF_AREA_REPORTING).toString()));
			
			pdb2uniprotIdThreshold = Double.parseDouble(p.getProperty("PDB2UNIPROT_ID_THRESHOLD", new Double(DEF_PDB2UNIPROT_ID_THRESHOLD).toString()));
			pdb2uniprotQcovThreshold = Double.parseDouble(p.getProperty("PDB2UNIPROT_QCOV_THRESHOLD", new Double(DEF_PDB2UNIPROT_QCOV_THRESHOLD).toString()));
					
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

	public String getPisaInterfacesUrl() {
		return pisaInterfacesUrl;
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

	public double getMinInterfAreaReporting() {
		return minInterfAreaReporting;
	}

	public double getPdb2uniprotIdThreshold() {
		return pdb2uniprotIdThreshold;
	}

	public double getPdb2uniprotQcovThreshold() {
		return pdb2uniprotQcovThreshold;
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
