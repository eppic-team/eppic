package crk;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.structure.AminoAcid;

public class CRKParams {

	private static final Pattern  PDBCODE_PATTERN = Pattern.compile("^\\d\\w\\w\\w$");
	
	// the parameters
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
	
	private int      minNumResCA;
	private int      minNumResMemberCA; 
	
	private double selectonEpsilon;

	private int maxNumSeqsSelecton;
	
	private boolean usePisa;

	private boolean useNaccess;
	
	private int nSpherePointsASAcalc;

	private double grayZoneWidth;
	private double[] entrCallCutoffs;
	private double[] kaksCallCutoffs;
	
	private File interfSerFile;
	private File chainEvContextSerFile;
	
	private boolean generateThumbnails;
	
	private File progressLogFile;
	private PrintStream progressLog;
	
	
	// some other fields
	private File inFile;
	private String jobName;
	
	public CRKParams() {
		
	}
	
	public CRKParams(String pdbCode, boolean doScoreCRK, double idCutoff, String baseName, File outDir, int numThreads, int reducedAlphabet,boolean useTcoffeeVeryFastMode,
			boolean zooming, double bsaToAsaSoftCutoff, double bsaToAsaHardCutoff, double relaxationStep,
			double[] cutoffsCA, 
			int minNumResCA, int minNumResMemberCA,
			double selectonEpsilon, int maxNumSeqsSelecton,
			boolean usePisa, boolean useNaccess,
			int nSpherePointsASAcalc,
			double grayZoneWidth,
			double[] entrCallCutoff, double[] kaksCallCutoff,
			File interfSerFile, File chainEvContextSerFile,
			boolean generateThumbnails,
			PrintStream progressLog) {
		
		this.pdbCode = pdbCode;
		this.doScoreCRK = doScoreCRK;
		this.idCutoff = idCutoff;
		this.baseName = baseName;
		this.outDir = outDir;
		this.numThreads = numThreads;
		this.reducedAlphabet = reducedAlphabet;
		this.useTcoffeeVeryFastMode = useTcoffeeVeryFastMode;
		this.zooming = zooming;
		this.bsaToAsaSoftCutoff = bsaToAsaSoftCutoff;
		this.bsaToAsaHardCutoff = bsaToAsaHardCutoff;
		this.relaxationStep = relaxationStep;
		this.cutoffsCA = cutoffsCA;
		this.minNumResCA = minNumResCA;
		this.minNumResMemberCA = minNumResMemberCA;
		this.selectonEpsilon = selectonEpsilon;
		this.maxNumSeqsSelecton = maxNumSeqsSelecton;
		this.usePisa = usePisa;
		this.useNaccess = useNaccess;
		this.nSpherePointsASAcalc = nSpherePointsASAcalc;
		this.grayZoneWidth = grayZoneWidth;
		this.entrCallCutoffs = entrCallCutoff;
		this.kaksCallCutoffs = kaksCallCutoff;
		this.interfSerFile = interfSerFile;
		this.chainEvContextSerFile = chainEvContextSerFile;
		this.generateThumbnails = generateThumbnails;
		this.progressLog = progressLog;
	}
	
	public void parseCommandLine(String[] args, String programName, String help) {
	

		Getopt g = new Getopt(programName, args, "i:kd:a:b:o:r:tc:zZ:m:M:x:X:g:e:q:pnA:I:C:lL:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				setPdbCode(g.getOptarg());
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
				entrCallCutoffs = new double[xtokens.length];
				for (int i=0;i<xtokens.length;i++){
					entrCallCutoffs[i] = Double.parseDouble(xtokens[i]);
				}
				break;
			case 'X':
				String[] Xtokens = g.getOptarg().split(",");
				kaksCallCutoffs = new double[Xtokens.length];
				for (int i=0;i<Xtokens.length;i++){
					kaksCallCutoffs[i] = Double.parseDouble(Xtokens[i]);
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
	public double[] getCutoffsCA() {
		return cutoffsCA;
	}
	public void setCutoffsCA(double[] cutoffsCA) {
		this.cutoffsCA = cutoffsCA;
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
	
	public double[] getEntrCallCutoffs() {
		return entrCallCutoffs;
	}
	public double getEntrCallCutoff(int i) {
		return entrCallCutoffs[i];
	}
	public void setEntrCallCutoffs(double[] entrCallCutoffs) {
		this.entrCallCutoffs = entrCallCutoffs;
	}
	public double[] getKaksCallCutoffs() {
		return kaksCallCutoffs;
	}
	public double getKaksCallCutoff(int i) {
		return kaksCallCutoffs[i];
	}
	public void setKaksCallCutoffs(double[] kaksCallCutoffs) {
		this.kaksCallCutoffs = kaksCallCutoffs;
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
	
}
