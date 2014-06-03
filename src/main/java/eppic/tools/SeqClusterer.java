package eppic.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import eppic.EppicException;
import eppic.EppicParams;
import eppic.model.ChainClusterDB;
import owl.core.runners.blast.BlastException;
import owl.core.runners.blast.BlastRunner;

public class SeqClusterer {
	
	
	private static final String     BLASTCLUST_BASENAME = "eppic_seq_clustering";
	private static final String     BLASTCLUST_OUT_SUFFIX = ".blastclust.out";
	private static final String 	BLASTCLUST_SAVE_SUFFIX = ".blastclust.save";
	private static final String		IN_FASTA_SUFFIX = "all_seqs.fa";
	
	
	
	private static final boolean	DEBUG = true;
	
	// the wwww.pdb.org parameters for blastclust:  -p T -b T -S 95 -L 0.9
	private static final double 	BLASTCLUST_CLUSTERING_COVERAGE = 0.9;
		
	private File inFastaFile;
	private int numThreads;
	
	private EppicParams params;
	
	private BlastRunner blastRunner;
	
	private boolean saveFileComputed;
	
	private File saveFile;
	
	public SeqClusterer(Map<Integer,ChainClusterDB> allChains, int numThreads) throws IOException {
		
		inFastaFile = File.createTempFile(BLASTCLUST_BASENAME, IN_FASTA_SUFFIX);
		writeFastaFile(allChains);
		System.out.println("Wrote FASTA file "+inFastaFile);
		
		this.numThreads = numThreads;
		this.params = loadConfigFile();
		this.blastRunner = new BlastRunner(null);
		this.saveFileComputed = false;
		
		this.saveFile = File.createTempFile(BLASTCLUST_BASENAME,BLASTCLUST_SAVE_SUFFIX);
		
		if (!DEBUG) {
			saveFile.deleteOnExit();
			inFastaFile.deleteOnExit();
		}
		
	}
	
	public SeqClusterer(Map<Integer,ChainClusterDB> allChains, File saveFile) throws IOException {
		
		this.params = loadConfigFile();
		this.blastRunner = new BlastRunner(null);
		this.saveFileComputed = true;
		
		this.saveFile = saveFile;
	}
	
	
	public List<List<String>> clusterThem(int clusteringId) throws IOException, InterruptedException, BlastException {
		
		File outblastclustFile = File.createTempFile(BLASTCLUST_BASENAME,BLASTCLUST_OUT_SUFFIX);
				
		List<List<String>> clusterslist = null;
		
		// first the real run of blastclust (we save neighbors with -s and reuse them after)
		

		if (!saveFileComputed) {
			
			System.out.println("Running initial blastclust, this will take long...");
			
			long start = System.currentTimeMillis();
			
			clusterslist = blastRunner.runBlastclust(
					params.getBlastclustBin(), this.inFastaFile, outblastclustFile, true, clusteringId, 
					BLASTCLUST_CLUSTERING_COVERAGE, params.getBlastDataDir(), saveFile, numThreads);
			
			long end = System.currentTimeMillis();
			System.out.println("Run initial blastclust ("+((end-start)/1000)+"s): "+blastRunner.getLastBlastCommand());
			saveFileComputed = true;
			
			System.out.println("Clustering with "+clusteringId+"% id resulted in "+clusterslist.size()+" clusters");


		} else {
			
			clusterslist = blastRunner.runBlastclust(
					params.getBlastclustBin(), outblastclustFile, true, clusteringId, 
					BLASTCLUST_CLUSTERING_COVERAGE, saveFile, numThreads);
			
			System.out.println("Run blastclust from saved neighbors: "+blastRunner.getLastBlastCommand());

			System.out.println("Clustering with "+clusteringId+"% id resulted in "+clusterslist.size()+" clusters");

		
		}

		if (!DEBUG) {
			// note that if blastclust throws an exception then this is not reached and thus files not removed on exit
			outblastclustFile.deleteOnExit();
			
		}
		
		return clusterslist;
	}
	
	public void writeClustersToFile(List<List<String>> clustersList, File outFile) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(outFile);
		for (int i=0;i<clustersList.size();i++) {
			pw.printf("%5d: ",i+1);
			for (String member:clustersList.get(i)) {
				pw.print(member+" ");
			}
			pw.println();
		}
		pw.close();
	}
	
	public void writeFastaFile(Map<Integer, ChainClusterDB> allChains) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(inFastaFile);
		
		for (int uid:allChains.keySet()) {
			ChainClusterDB chain = allChains.get(uid);
			String seq = chain.getPdbAlignedSeq().replaceAll("-", "");
			
			pw.println(">"+uid);
			pw.println(seq);
		
			
		}
		pw.close();
		
		
	}
	
	private EppicParams loadConfigFile() {
		
		EppicParams params = new EppicParams();
		
		// loading settings from config file
		File userConfigFile = new File(System.getProperty("user.home"),EppicParams.CONFIG_FILE_NAME);  
		try {
			if (userConfigFile.exists()) {
				System.out.println("Loading user configuration file " + userConfigFile);
				params.readConfigFile(userConfigFile);
				params.checkConfigFileInput();
			} else if (!params.isInputAFile() || params.isDoEvolScoring()) {
				System.err.println("No config file could be read at "+userConfigFile+
						". Please set one to give the blastclust paths.");
				System.exit(1);
			}
		} catch (IOException e) {
			System.err.println("Error while reading from config file: " + e.getMessage());
			System.exit(1);
		} catch (EppicException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return params;
	}
	
	

}
