package eppic.commons.sequence;

import org.biojava.nbio.alignment.PairwiseAlignmentStatistics;
import org.biojava.nbio.alignment.PairwiseStatisticsAligner;
import org.biojava.nbio.alignment.PairwiseStatisticsAligner.EncodedSequence;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * Clustering of protein sequences by sequence identity, done exactly: with a Smith-Waterman
 * alignment of every pair of sequences that could be similar enough.
 * <p>
 * It does what the clustering of mmseqs2 does (as run by {@link eppic.commons.blast.MmseqsRunner}):
 * 2 sequences are linked if their local alignment (BLOSUM62, gap open 11, gap extension 1) has at
 * least the required identity and covers at least the required fraction of both of them, and the
 * clusters are the connected components of the links. Letters that are not one of the 20 standard
 * amino acids are taken as X. Being all against all it is only meant
 * for small sets of sequences, for which it takes a small fraction of the time that it takes
 * mmseqs2 just to start: {@link #estimateCost(List, double, double)} tells how much work a set is.
 * <p>
 * An instance remembers the pairs that it has aligned, by their identifiers. So clustering again
 * a set that shares sequences with a previous one does not align those again, provided that an
 * identifier is always given with the same sequence and that the cutoffs don't change.
 */
public class SequenceClusterer {

	private static final String STANDARD_AMINO_ACIDS = "ACDEFGHIKLMNPQRSTVWY";
	private static final char UNKNOWN = 'X';

	/**
	 * The local alignment (Smith-Waterman) with BLOSUM62, 11 for opening a gap and 1 for every
	 * position of it. It gives only the figures of the alignment, which is all that is needed
	 * and a lot faster than getting the alignment itself.
	 */
	private static final PairwiseStatisticsAligner<AminoAcidCompound> ALIGNER = new PairwiseStatisticsAligner<>(
			new SimpleGapPenalty(11, 1), SubstitutionMatrixHelper.getBlosum62(), true);

	private final double minSeqId;
	private final double minCoverage;

	/**
	 * Whether 2 sequences are linked, by their 2 identifiers
	 */
	private final Map<String, Boolean> linked = new ConcurrentHashMap<>();

	/**
	 * @param minSeqId the minimum sequence identity for 2 sequences to be in the same cluster, as
	 * a fraction of the length of their alignment
	 * @param minCoverage the minimum fraction of both sequences that their alignment must cover
	 */
	public SequenceClusterer(double minSeqId, double minCoverage) {
		this.minSeqId = minSeqId;
		this.minCoverage = minCoverage;
	}

	/**
	 * Clusters the given sequences.
	 * @param ids the identifiers of the sequences, all different
	 * @param seqs the sequences, in the same order as the identifiers
	 * @param numThreads the number of threads to use
	 * @return the clusters, each as the list of the identifiers of its members. Members are in
	 * the order given, as are the clusters by their first member. Every sequence is in a
	 * cluster, possibly on its own
	 */
	public List<List<String>> cluster(List<String> ids, List<String> seqs, int numThreads) {
		if (ids.size() != seqs.size()) {
			throw new IllegalArgumentException("There must be as many identifiers as sequences");
		}
		int n = ids.size();
		List<EncodedSequence<AminoAcidCompound>> encoded = new ArrayList<>(n);
		for (String seq : seqs) {
			encoded.add(encode(seq));
		}

		// The clusters are the connected components, kept as a union-find structure. A pair that
		// is in the same cluster already can't change anything, so it is not aligned: which pairs
		// those are depends on the order in which the threads go, but the clusters don't.
		int[] parent = new int[n];
		for (int i = 0; i < n; i++) {
			parent[i] = i;
		}
		IntConsumer alignRow = i -> {
			for (int j = i + 1; j < n; j++) {
				if (!canBeLinked(encoded.get(i).getLength(), encoded.get(j).getLength(), minSeqId, minCoverage)) {
					continue;
				}
				synchronized (parent) {
					if (find(parent, i) == find(parent, j)) {
						continue;
					}
				}
				if (isLinked(ids.get(i), encoded.get(i), ids.get(j), encoded.get(j))) {
					synchronized (parent) {
						parent[find(parent, i)] = find(parent, j);
					}
				}
			}
		};
		if (numThreads <= 1) {
			IntStream.range(0, n).forEach(alignRow);
		} else {
			// a pool of its own, for the parallel stream to use the given number of threads
			ForkJoinPool pool = new ForkJoinPool(numThreads);
			try {
				pool.submit(() -> IntStream.range(0, n).parallel().forEach(alignRow)).join();
			} finally {
				pool.shutdown();
			}
		}

		Map<Integer, List<String>> clusters = new LinkedHashMap<>();
		for (int i = 0; i < n; i++) {
			clusters.computeIfAbsent(find(parent, i), root -> new ArrayList<>()).add(ids.get(i));
		}
		return new ArrayList<>(clusters.values());
	}

	private boolean isLinked(String id1, EncodedSequence<AminoAcidCompound> seq1, String id2, EncodedSequence<AminoAcidCompound> seq2) {
		String key = id1.compareTo(id2) < 0 ? id1 + "\t" + id2 : id2 + "\t" + id1;
		Boolean isLinked = linked.get(key);
		if (isLinked == null) {
			PairwiseAlignmentStatistics aln = ALIGNER.align(seq1, seq2);
			// with length 0 there is no alignment: no pair of residues scores above 0
			isLinked = aln.getLength() > 0
					&& aln.getNumIdenticals() >= minSeqId * aln.getLength()
					&& aln.getQueryEnd() - aln.getQueryStart() + 1 >= minCoverage * seq1.getLength()
					&& aln.getTargetEnd() - aln.getTargetStart() + 1 >= minCoverage * seq2.getLength();
			linked.put(key, isLinked);
		}
		return isLinked;
	}

	/**
	 * Estimates the work that it is to cluster the given sequences with the given cutoffs: the
	 * number of cells of the alignment matrices of all the pairs that need to be aligned. As a
	 * rough guide a thread goes through some 10^8 cells per second. The actual work is usually
	 * less, a lot less if many of the sequences are redundant.
	 * @param seqs the sequences
	 * @param minSeqId the minimum sequence identity, see {@link #SequenceClusterer(double, double)}
	 * @param minCoverage the minimum coverage, see {@link #SequenceClusterer(double, double)}
	 * @return the number of cells
	 */
	public static long estimateCost(List<String> seqs, double minSeqId, double minCoverage) {
		long cost = 0;
		for (int i = 0; i < seqs.size(); i++) {
			for (int j = i + 1; j < seqs.size(); j++) {
				if (canBeLinked(seqs.get(i).length(), seqs.get(j).length(), minSeqId, minCoverage)) {
					cost += (long) seqs.get(i).length() * seqs.get(j).length();
				}
			}
		}
		return cost;
	}

	/**
	 * Whether 2 sequences of the given lengths can at all be linked. The alignment must cover the
	 * required fraction of the longer one, and can't have more identical positions than the
	 * length of the shorter one: that limits the identity that sequences of different lengths
	 * can have.
	 */
	private static boolean canBeLinked(int length1, int length2, double minSeqId, double minCoverage) {
		int shorter = Math.min(length1, length2);
		int longer = Math.max(length1, length2);
		return shorter > 0 && shorter >= minSeqId * minCoverage * longer;
	}

	private static int find(int[] parent, int i) {
		while (parent[i] != i) {
			parent[i] = parent[parent[i]];
			i = parent[i];
		}
		return i;
	}

	private static EncodedSequence<AminoAcidCompound> encode(String seq) {
		StringBuilder sb = new StringBuilder(seq.length());
		for (int i = 0; i < seq.length(); i++) {
			char letter = Character.toUpperCase(seq.charAt(i));
			sb.append(STANDARD_AMINO_ACIDS.indexOf(letter) >= 0 ? letter : UNKNOWN);
		}
		try {
			return ALIGNER.encode(new ProteinSequence(sb.toString()));
		} catch (CompoundNotFoundException e) {
			// can't happen: only the standard amino acids and X are left
			throw new IllegalStateException(e);
		}
	}
}
