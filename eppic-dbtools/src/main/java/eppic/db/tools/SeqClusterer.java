package eppic.db.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import eppic.commons.blast.BlastException;
import eppic.commons.blast.MmseqsRunner;
import eppic.model.db.ChainClusterDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeqClusterer {

	private static final Logger logger = LoggerFactory.getLogger(SeqClusterer.class);

	private static final String BASENAME = "eppic_seq_clustering";
	private static final String IN_FASTA_SUFFIX = "all_seqs.fa";

	private static final double CLUSTERING_COVERAGE = 0.9;

	private File inFastaFile;
	private int numThreads;

	private File mmseqsBin;


	public SeqClusterer(Map<Integer, ChainClusterDB> allChains, int numThreads, File mmseqsBin)
			throws IOException {

		this.mmseqsBin = mmseqsBin;
		inFastaFile = File.createTempFile(BASENAME, IN_FASTA_SUFFIX);
		writeFastaFile(allChains);
		logger.info("Wrote FASTA file " + inFastaFile);

		this.numThreads = numThreads;

		if (!logger.isDebugEnabled()) {
			inFastaFile.deleteOnExit();
		}

	}

	public List<List<String>> clusterThem(int clusteringId) throws IOException,
			InterruptedException, BlastException {

		// we need a unique file name to pass to mmseqs but we don't want the file to exist when we run mmseqs,
		// thus we create and remove immediately
		File outClustFilePrefix = File.createTempFile(BASENAME, "");
		boolean couldDelete = outClustFilePrefix.delete();
		if (!couldDelete)
			logger.warn("Could not delete temp file " + outClustFilePrefix +". mmseqs will probably fail");

		logger.info("Running mmseqs for identity "+clusteringId+"% , this will take long...");

		long start = System.currentTimeMillis();

		List<List<String>> clusterslist = MmseqsRunner.runMmseqsEasyCluster(mmseqsBin,
				this.inFastaFile, outClustFilePrefix, clusteringId,
				CLUSTERING_COVERAGE, numThreads);

		long end = System.currentTimeMillis();
		logger.info("Run mmseqs at identity "+clusteringId+" in "
				+ ((end - start) / 1000) + "s ");

		logger.info("Clustering with " + clusteringId
				+ "% id resulted in " + clusterslist.size() + " clusters");

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

	private void writeFastaFile(Map<Integer, ChainClusterDB> allChains)
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

}
