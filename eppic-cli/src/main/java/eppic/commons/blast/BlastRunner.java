package eppic.commons.blast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eppic.commons.sequence.FileFormatException;
import eppic.commons.sequence.Sequence;

/**
 * A class to run blast programs
 *
 */
public class BlastRunner {

	// constants
	public static final int		BLAST_CLASSIC_OUTPUT_TYPE = 0;  // classic blast output, default in legacy blast and blast+
	
	public static final int 	BLASTPLUS_XML_OUTPUT_TYPE = 5;  // xml output 
	public static final int 	BLASTPLUS_TAB_OUTPUT_TYPE = 6;
	
	public static final int 	LEGACYBLAST_XML_OUTPUT_TYPE = 7;
	public static final int		LEGACYBLAST_TAB_OUTPUT_TYPE = 8;


	public static final int		BLAST_DEFAULT_MAX_HITS = 500;
	
	public static final String 	BLASTALL_PROG = "blastall";
	public static final String 	BLASTPGP_PROG = "blastpgp";
	public static final String 	MAKEMAT_PROG  = "makemat_withcd"; // customised makemat script that cds to working dir before running makemat
	
	private static final String BLASTP_PROGRAM_NAME = "blastp";	

	
	// legacy blast programs
	private String blastDbDir;
	private String blastallProg;
	private String blastpgpProg;
	private String makematProg;
		
	private String cmdLine;
	
	/**
	 * Constructs a BlastRunner object given a blastDbDir
	 * Use then the setters to set the blast programs paths 
	 * @param blastDbDir
	 */
	public BlastRunner(String blastDbDir) {
		this.blastDbDir = blastDbDir;

	}
	
	public void setLegacyBlastBinDir(String blastBinDir) {
		this.blastallProg = new File(blastBinDir,BLASTALL_PROG).getAbsolutePath();
		this.blastpgpProg = new File(blastBinDir,BLASTPGP_PROG).getAbsolutePath();
		this.makematProg = new File(blastBinDir,MAKEMAT_PROG).getAbsolutePath();
	}
	
	private String getCommonOptionsStr(File queryFile, String db, File outFile, int outputType, boolean noFiltering, int numThreads, int maxNumHits) { 
		String dbFullPath = new File(blastDbDir,db).getAbsolutePath();
		String filter = "";
		if (noFiltering) filter = " -F F ";
		String maxHits = "";
		// default blast max num hits is 500, only if we require more, we pass the -v option
		if (maxNumHits>BLAST_DEFAULT_MAX_HITS) maxHits = " -v "+maxNumHits+" ";
		String options = filter +
						 maxHits +
						 " -a "+numThreads+
						 " -m "+outputType+
						 " -i "+queryFile.getAbsolutePath()+
						 " -d "+dbFullPath+
						 " -o "+outFile.getAbsolutePath() + " ";
		return options;
	}
	
	private void checkIO(File queryFile, String db) throws IOException{
		if (!queryFile.canRead()) throw new IOException("Can't read query file "+queryFile);
		String dbFullPath = new File(blastDbDir,db).getAbsolutePath();
		if (! (new File(dbFullPath+".pal").canRead() || new File(dbFullPath+".psq").canRead()))
			throw new IOException("Can't read the database files for database in "+dbFullPath);
	}
	
	/**
	 * Runs legacy psi-blast for given query file against given db
	 * @param queryFile
	 * @param db the identifier of the blast database to run against
	 * @param outFile the blast output file
	 * @param maxIter
	 * @param outProfileFile the output profile file for the -C option, null if 
	 * not required. Must have .chk extension
	 * @param inProfileFile the input profile file for the -R option, null if a 
	 * psi-blast from scratch wanted. Must have .chk extension
	 * @param outputType 0 classic, 7 XML, 8 tabular, 9 tabular with comments
	 * @param noFiltering if true no filtering of query sequence will be done (-F F). Note 
	 * that the default for this parameter varies from blastall to blastpgp and also from one 
	 * output type to another. So it is recommended to use it to get consistent results if one
	 * wants no filtering.
	 * @throws IOException
	 * @throws BlastException if exit status of the program is not 0
	 * @throws InterruptedException
	 */
	public void runLegacyPsiBlast(File queryFile, String db, File outFile, int maxIter, File outProfileFile, File inProfileFile, int outputType, boolean noFiltering, int numThreads) 
	throws IOException, BlastException, InterruptedException {
		
		checkIO(queryFile, db);
		
		String outProfileOpt = "";
		String inProfileOpt = "";
		if (outProfileFile!=null) outProfileOpt = " -C "+outProfileFile.getAbsolutePath();
		if (inProfileFile!=null) inProfileOpt = " -R "+inProfileFile.getAbsolutePath();
		cmdLine = blastpgpProg + getCommonOptionsStr(queryFile, db, outFile, outputType, noFiltering, numThreads, 500) +
						" -j " + maxIter+
						outProfileOpt + inProfileOpt;
		Process blastpgpProc = Runtime.getRuntime().exec(cmdLine);


		int exitValue = blastpgpProc.waitFor();
		if (exitValue>0) {
			throw new BlastException(BLASTALL_PROG + " exited with error value " + exitValue);
		}
	}
	
	/**
	 * Runs the legacy blastall program given by prog, for given query file against database db
	 * @param queryFile
	 * @param db the identifier of the blast database to run against
	 * @param outFile the blast output file
	 * @param prog the blast program: one of blastp, blastn, blastx, tblastn, tblastx 
	 * @param outputType 0 classic, 7 XML, 8 tabular, 9 tabular with comments
	 * @param noFiltering if true no filtering of query sequence will be done (-F F). Note 
	 * that the default for this parameter varies from blastall to blastpgp and also from one 
	 * output type to another. So it is recommended to use it to get consistent results if one
	 * wants no filtering.
	 * @param numThreads
	 * @param maxNumHits the maximum number of hits to report, only used when >500, then passed 
	 * to blast with option -v 
	 * @param matrix the name of the substitution matrix to use, e.g. BLOSUM62
	 * @throws IOException
	 * @throws BlastException if exit status of the program is not 0
	 * @throws InterruptedException
	 */
	private void runBlast(File queryFile, String db, File outFile, String prog, int outputType, boolean noFiltering, int numThreads, int maxNumHits, String matrix) 
	throws IOException, BlastException, InterruptedException {
		
		checkIO(queryFile, db);
		
		cmdLine = blastallProg + " -p " + prog + " -M " + matrix + " " +
				getCommonOptionsStr(queryFile, db, outFile, outputType, noFiltering, numThreads, maxNumHits);
		Process blastallProc = Runtime.getRuntime().exec(cmdLine);
		

		int exitValue = blastallProc.waitFor();
		if (exitValue>0) {
			throw new BlastException(BLASTALL_PROG + " exited with error value " + exitValue);
		}
	}
	
	/**
	 * Runs legacy protein blast against given db for given input query file 
	 * The substitution matrix used will be adjusted automatically depending on query 
	 * length (following http://www.ncbi.nlm.nih.gov/blast/html/sub_matrix.html):
	 *   <35   : PAM30
	 *   35-50 : PAM70
	 *   50-85 : BLOSUM80
	 *   >85   : BLOSUM62
	 * 
	 * @param queryFile
	 * @param db
	 * @param outFile
	 * @param outputType use one of the constants: {@link #LEGACYBLAST_TAB_OUTPUT_TYPE}, {@link #LEGACYBLAST_XML_OUTPUT_TYPE}
	 * @param noFiltering if true no filtering of query sequence will be done (-F F). Note 
	 * that the default for this parameter varies from blastall to blastpgp and also from one 
	 * output type to another. So it is recommended to use it to get consistent results if one
	 * wants no filtering.
	 * @param numThreads
	 * @param maxNumHits the maximum number of hits to report, only used when >500, then passed 
	 * to blast with option -v 
	 * @throws IOException
	 * @throws BlastException
	 */
	public void runLegacyBlastp(File queryFile, String db, File outFile, int outputType, boolean noFiltering, int numThreads, int maxNumHits) 
	throws IOException, BlastException, InterruptedException {
		int length = 0;
		try {
			List<Sequence> readseqs = Sequence.readSeqs(queryFile, null);
			if (readseqs.size()>0) {
				length = readseqs.get(0).getLength();
			} else {
				throw new BlastException("The given FASTA file for blasting "+queryFile+" contains no sequences");
			}
		} catch (FileFormatException e) {
			throw new BlastException("The given FASTA file for blasting "+queryFile+" does not seem to have the right format");
		}
		
		String mat = getMatrixFromLength(length);		
			
			
		runBlast(queryFile, db, outFile, BLASTP_PROGRAM_NAME, outputType, noFiltering, numThreads, maxNumHits, mat);
	}
	
	/**
	 * Runs blast+ blastp program (the succesor of blastall -p blastp)
	 * The substitution matrix used will be adjusted automatically depending on query 
	 * length (following http://www.ncbi.nlm.nih.gov/blast/html/sub_matrix.html):
	 *   <35   : PAM30
	 *   35-50 : PAM70
	 *   50-85 : BLOSUM80
	 *   >85   : BLOSUM62
	 *     
	 * @param blastPlusBlastp
	 * @param queryFile
	 * @param db
	 * @param outFile
	 * @param outputType use one of the constants: {@link #BLASTPLUS_TAB_OUTPUT_TYPE}, {@link #BLASTPLUS_XML_OUTPUT_TYPE}
	 * @param noFiltering
	 * @param numThreads
	 * @param maxNumHits
	 * @throws IOException
	 * @throws BlastException
	 * @throws InterruptedException
	 */
	public void runBlastp(File blastPlusBlastp, File queryFile, String db, File outFile, int outputType, boolean noFiltering, int numThreads, int maxNumHits) 
		throws IOException, BlastException, InterruptedException {
		int length = 0;
		try {
			List<Sequence> readseqs = Sequence.readSeqs(queryFile, null);
			if (readseqs.size()>0) {
				length = readseqs.get(0).getLength();
			} else {
				throw new BlastException("The given FASTA file for blasting "+queryFile+" contains no sequences");
			}
		} catch (FileFormatException e) {
			throw new BlastException("The given FASTA file for blasting "+queryFile+" does not seem to have the right format");
		}
		
		String mat = getMatrixFromLength(length);		
			
		checkIO(queryFile, db);

		String dbFullPath = new File(blastDbDir,db).getAbsolutePath();
		
		
		// default blast max num hits is 500, only if we require more, we pass the -v option
		String maxHits = "";
		if (maxNumHits>BLAST_DEFAULT_MAX_HITS) maxHits = " -max_target_seqs "+maxNumHits+" ";
		
		cmdLine = blastPlusBlastp.getAbsolutePath() + 
				" -matrix " + mat + 
				" -db "+dbFullPath+
				" -query "+queryFile.getAbsolutePath()+
				" -out "+outFile.getAbsolutePath()+
				" -num_threads "+numThreads+
				" -outfmt "+outputType+
				" -seg "+(noFiltering?"no":"yes") + 
				maxHits;

		Process blastallProc = Runtime.getRuntime().exec(cmdLine);


		int exitValue = blastallProc.waitFor();
		if (exitValue>0) {
			throw new BlastException(blastPlusBlastp.getName() + " exited with error value " + exitValue);
		}

		
	}
	
	/**
	 * Returns a blast substitution matrix depending on given sequenceLength
	 * (following http://www.ncbi.nlm.nih.gov/blast/html/sub_matrix.html):
	 *   <35   : PAM30
	 *   35-50 : PAM70
	 *   50-85 : BLOSUM80
	 *   >85   : BLOSUM62
	 * This is what most blast web servers do by default.
	 * @param sequenceLength
	 * @return
	 */
	private String getMatrixFromLength(int sequenceLength) {
		String mat;
		if (sequenceLength<35) {
			mat = "PAM30";
		} else if (sequenceLength<50) {
			mat = "PAM70"; 
		} else if (sequenceLength<85) {
			mat = "BLOSUM80";
		} else {
			mat = "BLOSUM62";
		}
		return mat;
	}
	
	/**
	 * 
	 * @param workDir
	 * @param basename
	 */
	public void runMakemat(String workDir, String basename) throws IOException, BlastException, InterruptedException {
		cmdLine = makematProg + " "+workDir+" -P " + basename;
		Process makematProc = Runtime.getRuntime().exec(cmdLine);
		
		int exitValue = makematProc.waitFor();
		if (exitValue>0) {
			throw new BlastException(MAKEMAT_PROG + " exited with error value " + exitValue);
		}
	}
		
	/**
	 * Runs blastclust, parsing the output file and returning it as a list of clusters. 
	 * If a saveFile is passed, the neighbors are saved for later reuse (options -s/-r)
	 * (see {@link #runBlastclust(File, boolean, int, double, File, int)}
	 * @param blastClustBin the blastclust executable
	 * @param inFile the input FASTA file with all sequences to be clustered
	 * @param outFile the output file where clusters will be written
	 * @param protein if true sequences are protein, false sequences are nucleotide
	 * @param clusteringPercentId the percentage identity threshold for clustering: the 
	 * cluster members will be all within this id (blastclust option -S)
	 * @param clusteringCoverage the ratio coverage threshold (blastclust option -L) 
	 * We use by default -b T, i.e. the coverage threshold is applied to both members of 
	 * the sequence pairs
	 * @param blastDataDir the path to the blast distribution data files with blosum matrices
	 * @param saveFile a file to save the neighbors (option -s) in order to reuse them with -r, 
	 * if null the neighbors won't be saved
	 * @param numThreads the number of CPUs to be used
	 * @return a list of clusters
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws BlastException 
	 */
	public List<List<String>> runBlastclust(File blastClustBin, File inFile, File outFile, boolean protein, int clusteringPercentId, double clusteringCoverage, String blastDataDir, File saveFile, int numThreads) 
			throws IOException, InterruptedException, BlastException {
		
		// command to run blastclust for 95% clustering over full lengths 
		// (see http://www.ncbi.nlm.nih.gov/Web/Newsltr/Spring04/blastlab.html) 
		// blastclust -i infile -o outfile -p T -L 1 -b T -S 95 -a 1  		
		
		List<String> cmd = new ArrayList<String>();
		
		cmd.add(blastClustBin.getAbsolutePath());
		cmd.add("-i"); cmd.add(inFile.getAbsolutePath());
		cmd.add("-o"); cmd.add(outFile.getAbsolutePath());
		cmd.add("-p"); cmd.add((protein?"T":"F"));
		cmd.add("-S"); cmd.add(Integer.toString(clusteringPercentId));
		cmd.add("-L"); cmd.add(String.format("%4.2f",clusteringCoverage));
		cmd.add("-a"); cmd.add(Integer.toString(numThreads));
		cmd.add("-b"); cmd.add("T");
		if (saveFile!=null) {
			cmd.add("-s");cmd.add(saveFile.getAbsolutePath());
		}		
		
		cmdLine = "";
		for (String token:cmd) {
			cmdLine+=token+" ";
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		// beware blastclust needs either a BLASTMAT env variable defining the 
		// path to the dir with blosum matrices or a ~/.ncbirc file with:
		// [NCBI]
		// data=/path/to/blast/data/dir
		Map<String,String> envVars = pb.environment();
		envVars.put("BLASTMAT",blastDataDir);
		Process blastclustProc = pb.start();		
		
		int exitValue = blastclustProc.waitFor();
		if (exitValue>0) {
			throw new BlastException(blastClustBin.getName() + " exited with error value " + exitValue);
		}
		
		// and we parse the output file		
		
		return parseBlastClustOut(outFile); 
	}
	
	/**
	 * Runs blastclust from a previously saved neighbors file (option -s) in order to save
	 * computation time. The output file is parsed and output returned as a list of clusters
	 * @param blastClustBin the blastclust executable 
	 * @param outFile the output file where clusters will be written
	 * @param protein if true sequences are protein, false sequences are nucleotide
	 * @param clusteringPercentId the percentage identity threshold for clustering: the 
	 * cluster members will be all within this id (blastclust option -S)
	 * @param clusteringCoverage the ratio coverage threshold (blastclust option -L) 
	 * We use by default -b T, i.e. the coverage threshold is applied to both members of 
	 * the sequence pairs
	 * @param savedFile a saved file from running blastclust with -s option 
	 * @param numThreads the number of CPUs to be used
	 * @return a list of clusters
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws BlastException 
	 */
	public List<List<String>> runBlastclust(File blastClustBin, File outFile, boolean protein, int clusteringPercentId, double clusteringCoverage, File savedFile, int numThreads) 
			throws IOException, InterruptedException, BlastException {
		
		List<String> cmd = new ArrayList<String>();
		
		cmd.add(blastClustBin.getAbsolutePath());
		cmd.add("-o"); cmd.add(outFile.getAbsolutePath());
		cmd.add("-p"); cmd.add((protein?"T":"F"));
		cmd.add("-S"); cmd.add(Integer.toString(clusteringPercentId));
		// beware that probably the coverage parameter is already in the neighbors file, so here it might be redundant and probably misleading!
		cmd.add("-L"); cmd.add(String.format("%4.2f",clusteringCoverage));
		cmd.add("-a"); cmd.add(Integer.toString(numThreads));
		cmd.add("-b"); cmd.add("T");		
		cmd.add("-r"); cmd.add(savedFile.getAbsolutePath());				
		
		cmdLine = "";
		for (String token:cmd) {
			cmdLine+=token+" ";
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process blastclustProc = pb.start();		
		
		int exitValue = blastclustProc.waitFor();
		if (exitValue>0) {
			throw new BlastException(blastClustBin.getName() + " exited with error value " + exitValue);
		}
		
		// and we parse the output file		
		
		return parseBlastClustOut(outFile); 
	}	
	
	private List<List<String>> parseBlastClustOut(File blastclustOutFile) throws IOException {
		List<List<String>> clusters = new ArrayList<List<String>>();
		
		BufferedReader br = new BufferedReader(new FileReader(blastclustOutFile));
		String line;
		while ((line = br.readLine())!=null) {
			if (line.trim().isEmpty()) continue;
			List<String> cluster = new ArrayList<String>();
			clusters.add(cluster);
			String[] tokens = line.split(" ");
			for (String token:tokens) {
				cluster.add(token);
			}
		}
		br.close();
		
		return clusters;
	}
	
	/**
	 * Gets the last run blast command. Useful for logging.
	 * @return
	 */
	public String getLastBlastCommand() {
		return cmdLine;
	}
	

}
