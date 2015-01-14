package eppic.db.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import eppic.commons.blast.BlastException;
import eppic.commons.blast.BlastRunner;
import eppic.model.ChainClusterDB;

public class SeqClusterer {

	private static final String BLASTCLUST_BASENAME = "eppic_seq_clustering";
	private static final String BLASTCLUST_OUT_SUFFIX = ".blastclust.out";
	private static final String BLASTCLUST_SAVE_SUFFIX = ".blastclust.save";
	private static final String IN_FASTA_SUFFIX = "all_seqs.fa";

	private static final boolean DEBUG = true;

	// the wwww.pdb.org parameters for blastclust: -p T -b T -S 95 -L 0.9
	private static final double BLASTCLUST_CLUSTERING_COVERAGE = 0.9;
	public static final String CONFIG_FILE_NAME = ".eppic.conf";
	private static final File     DEF_BLASTCLUST_BIN = new File("/usr/bin/blastclust"); // from legacy blast package
	private static final String   DEF_BLAST_DATA_DIR = "/usr/share/blast";

	private File inFastaFile;
	private int numThreads;

	private BlastRunner blastRunner;

	private boolean saveFileComputed;

	private File saveFile;
	private File blastclustBin;
	private String blastDataDir;

	public SeqClusterer(Map<Integer, ChainClusterDB> allChains, int numThreads)
			throws IOException {

		inFastaFile = File.createTempFile(BLASTCLUST_BASENAME, IN_FASTA_SUFFIX);
		writeFastaFile(allChains);
		System.out.println("Wrote FASTA file " + inFastaFile);

		this.numThreads = numThreads;
		loadConfigFile();
		this.blastRunner = new BlastRunner(null);
		this.saveFileComputed = false;

		this.saveFile = File.createTempFile(BLASTCLUST_BASENAME,
				BLASTCLUST_SAVE_SUFFIX);

		if (!DEBUG) {
			// saveFile.deleteOnExit();
			inFastaFile.deleteOnExit();
		}

	}

	public SeqClusterer(Map<Integer, ChainClusterDB> allChains, File saveFile)
			throws IOException {

		loadConfigFile();
		this.blastRunner = new BlastRunner(null);
		this.saveFileComputed = true;

		this.saveFile = saveFile;
	}

	public File getBlastclustSaveFile() {
		return saveFile;
	}

	public List<List<String>> clusterThem(int clusteringId) throws IOException,
			InterruptedException, BlastException {

		File outblastclustFile = File.createTempFile(BLASTCLUST_BASENAME,
				BLASTCLUST_OUT_SUFFIX);

		List<List<String>> clusterslist = null;

		// first the real run of blastclust (we save neighbors with -s and reuse
		// them after)

		if (!saveFileComputed) {

			System.out
					.println("Running initial blastclust, this will take long...");

			long start = System.currentTimeMillis();

			clusterslist = blastRunner.runBlastclust(blastclustBin,
					this.inFastaFile, outblastclustFile, true, clusteringId,
					BLASTCLUST_CLUSTERING_COVERAGE, blastDataDir, saveFile,
					numThreads);

			long end = System.currentTimeMillis();
			System.out.println("Run initial blastclust ("
					+ ((end - start) / 1000) + "s): "
					+ blastRunner.getLastBlastCommand());
			saveFileComputed = true;

			System.out.println("Clustering with " + clusteringId
					+ "% id resulted in " + clusterslist.size() + " clusters");

		} else {

			clusterslist = blastRunner.runBlastclust(blastclustBin,
					outblastclustFile, true, clusteringId,
					BLASTCLUST_CLUSTERING_COVERAGE, saveFile, numThreads);

			System.out.println("Run blastclust from saved neighbors: "
					+ blastRunner.getLastBlastCommand());

			System.out.println("Clustering with " + clusteringId
					+ "% id resulted in " + clusterslist.size() + " clusters");

		}

		if (!DEBUG) {
			// note that if blastclust throws an exception then this is not
			// reached and thus files not removed on exit
			outblastclustFile.deleteOnExit();

		}

		return clusterslist;
	}

	public void writeClustersToFile(List<List<String>> clustersList,
			File outFile) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(outFile);
		for (int i = 0; i < clustersList.size(); i++) {
			pw.printf("%5d: ", i + 1);
			for (String member : clustersList.get(i)) {
				pw.print(member + " ");
			}
			pw.println();
		}
		pw.close();
	}

	public void writeFastaFile(Map<Integer, ChainClusterDB> allChains)
			throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(inFastaFile);

		for (int uid : allChains.keySet()) {
			ChainClusterDB chain = allChains.get(uid);
			String seq = chain.getPdbAlignedSeq().replaceAll("-", "");

			pw.println(">" + uid);
			pw.println(seq);

		}
		pw.close();

	}

	private Properties loadConfigFile() {
		Properties params = new Properties();
		// loading settings from config file
		File userConfigFile = new File(System.getProperty("user.home"),
				CONFIG_FILE_NAME);
		try {

			System.out.println("Loading user configuration file "
					+ userConfigFile);
			params.load(new FileInputStream(userConfigFile));
			blastclustBin   = new File(params.getProperty("BLASTCLUST_BIN", DEF_BLASTCLUST_BIN.toString()));
			blastDataDir = params.getProperty("BLAST_DATA_DIR", DEF_BLAST_DATA_DIR);
		} catch (IOException e) {
			System.err.println("Error while reading from config file: "
					+ e.getMessage());
			System.exit(1);
		}
		return params;
	}

}
