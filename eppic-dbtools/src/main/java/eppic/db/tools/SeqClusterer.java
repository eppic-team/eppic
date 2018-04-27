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
import eppic.commons.blast.MmseqsRunner;
import eppic.model.ChainClusterDB;

public class SeqClusterer {

	private static final String BASENAME = "eppic_seq_clustering";
	private static final String IN_FASTA_SUFFIX = "all_seqs.fa";

	private static final boolean DEBUG = true;

	private static final double CLUSTERING_COVERAGE = 0.9;
	private static final String CONFIG_FILE_NAME = ".eppic.conf";
	private static final File DEF_MMSEQS_BIN = new File("/usr/bin/mmseqs");

	private File inFastaFile;
	private int numThreads;

	private File mmseqsBin;


	public SeqClusterer(Map<Integer, ChainClusterDB> allChains, int numThreads)
			throws IOException {

		inFastaFile = File.createTempFile(BASENAME, IN_FASTA_SUFFIX);
		writeFastaFile(allChains);
		System.out.println("Wrote FASTA file " + inFastaFile);

		this.numThreads = numThreads;
		loadConfigFile();

		if (!DEBUG) {
			// saveFile.deleteOnExit();
			inFastaFile.deleteOnExit();
		}

	}

	public SeqClusterer(Map<Integer, ChainClusterDB> allChains, File saveFile)
			throws IOException {

		loadConfigFile();

	}

	public List<List<String>> clusterThem(int clusteringId) throws IOException,
			InterruptedException, BlastException {

		File outClustFilePrefix = File.createTempFile(BASENAME, "");

		System.out
				.println("Running initial blastclust, this will take long...");

		long start = System.currentTimeMillis();

		List<List<String>> clusterslist = MmseqsRunner.runMmseqsEasyCluster(mmseqsBin,
				this.inFastaFile, outClustFilePrefix, clusteringId,
				CLUSTERING_COVERAGE, numThreads);

		long end = System.currentTimeMillis();
		System.out.println("Run mmseqs at identity "+clusteringId+" in "
				+ ((end - start) / 1000) + "s ");

		System.out.println("Clustering with " + clusteringId
				+ "% id resulted in " + clusterslist.size() + " clusters");


		if (!DEBUG) {
			File outFile = new File(outClustFilePrefix.getParent(), outClustFilePrefix.getName() + MmseqsRunner.MMSEQS_TSV_SUFFIX);
			// note that if mmseqs throws an exception then this is not
			// reached and thus files not removed on exit
			outFile.deleteOnExit();

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
			mmseqsBin = new File(params.getProperty("MMSEQS_BIN", DEF_MMSEQS_BIN.toString()));
		} catch (IOException e) {
			System.err.println("Error while reading from config file: "
					+ e.getMessage());
			System.exit(1);
		}
		return params;
	}

}
