package eppic.commons.sequence;

import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;

import java.util.ArrayList;
import java.util.Arrays;
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
 * clusters are the connected components of the links. Being all against all it is only meant
 * for small sets of sequences, for which it takes a small fraction of the time that it takes
 * mmseqs2 just to start: {@link #estimateCost(List, double, double)} tells how much work a set is.
 * <p>
 * An instance remembers the pairs that it has aligned, by their identifiers. So clustering again
 * a set that shares sequences with a previous one does not align those again, provided that an
 * identifier is always given with the same sequence and that the cutoffs don't change.
 */
public class SequenceClusterer {

	private static final int GAP_OPEN = 11;
	private static final int GAP_EXTEND = 1;

	private static final char UNKNOWN = 'X';
	private static final int NUM_LETTERS = 26;
	/**
	 * BLOSUM62 indexed by letter (A is 0), with anything that is not one of the 20 standard
	 * amino acids scoring as X
	 */
	private static final int[][] MATRIX = initMatrix();

	private final double minSeqId;
	private final double minCoverage;

	/**
	 * Whether 2 sequences are linked, by their 2 identifiers
	 */
	private final Map<String, Boolean> linked = new ConcurrentHashMap<>();

	/**
	 * A local alignment of 2 sequences
	 * @param score the alignment score
	 * @param numIdenticals the number of positions of the alignment with identical residues
	 * @param length the number of positions of the alignment, including those with gaps
	 * @param start1 the first aligned position of the first sequence, starting at 0
	 * @param end1 the last aligned position of the first sequence, starting at 0
	 * @param start2 the first aligned position of the second sequence, starting at 0
	 * @param end2 the last aligned position of the second sequence, starting at 0
	 */
	public record LocalAlignment(int score, int numIdenticals, int length, int start1, int end1, int start2, int end2) {

		/**
		 * @return the fraction of the positions of the alignment (including those with gaps) that
		 * have identical residues
		 */
		public double getIdentity() {
			return length == 0 ? 0 : (double) numIdenticals / length;
		}
	}

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
		byte[][] encoded = new byte[n][];
		for (int i = 0; i < n; i++) {
			encoded[i] = encode(seqs.get(i));
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
				if (!canBeLinked(encoded[i].length, encoded[j].length, minSeqId, minCoverage)) {
					continue;
				}
				synchronized (parent) {
					if (find(parent, i) == find(parent, j)) {
						continue;
					}
				}
				if (isLinked(ids.get(i), encoded[i], ids.get(j), encoded[j])) {
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

	private boolean isLinked(String id1, byte[] seq1, String id2, byte[] seq2) {
		String key = id1.compareTo(id2) < 0 ? id1 + "\t" + id2 : id2 + "\t" + id1;
		Boolean isLinked = linked.get(key);
		if (isLinked == null) {
			LocalAlignment aln = align(seq1, seq2);
			isLinked = aln.getIdentity() >= minSeqId
					&& aln.end1() - aln.start1() + 1 >= minCoverage * seq1.length
					&& aln.end2() - aln.start2() + 1 >= minCoverage * seq2.length;
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

	/**
	 * Aligns locally the 2 given protein sequences with the Smith-Waterman algorithm with affine
	 * gap penalties: BLOSUM62, 11 for opening a gap and 1 for every position of it. Letters that
	 * are not one of the 20 standard amino acids are taken as X.
	 * @param seq1 the first sequence
	 * @param seq2 the second sequence
	 * @return the best scoring local alignment, with length 0 if no alignment has a positive score
	 */
	public static LocalAlignment align(String seq1, String seq2) {
		return align(encode(seq1), encode(seq2));
	}

	/**
	 * What is wanted from the alignment is where it starts and ends, its length and its number
	 * of identical positions, not the alignment itself. So instead of keeping the whole matrix
	 * to trace the alignment back, every cell carries those values for the best alignment that
	 * ends in it, and only 2 rows are kept.
	 */
	private static LocalAlignment align(byte[] seq1, byte[] seq2) {
		int n = seq1.length;
		int m = seq2.length;

		// h: best alignment ending at (i,j) in any way. f: best one ending with a gap in the
		// second sequence. Both for the previous row and, as they get overwritten, the current
		// one. With every score go the identicals, the length and the start of that alignment.
		int[] h = new int[m + 1], hIdent = new int[m + 1], hLen = new int[m + 1], hStart1 = new int[m + 1], hStart2 = new int[m + 1];
		int[] f = new int[m + 1], fIdent = new int[m + 1], fLen = new int[m + 1], fStart1 = new int[m + 1], fStart2 = new int[m + 1];
		Arrays.fill(f, Integer.MIN_VALUE / 2);

		int best = 0, bestIdent = 0, bestLen = 0, bestStart1 = 0, bestStart2 = 0, bestEnd1 = -1, bestEnd2 = -1;

		for (int i = 1; i <= n; i++) {
			int[] scores = MATRIX[seq1[i - 1]];
			// the cell to the upper left of the current one (previous row, previous column)
			int diag = 0, diagIdent = 0, diagLen = 0, diagStart1 = 0, diagStart2 = 0;
			// e: best alignment ending at the current cell with a gap in the first sequence
			int e = Integer.MIN_VALUE / 2, eIdent = 0, eLen = 0, eStart1 = 0, eStart2 = 0;
			// the cell to the left of the current one, which in column 0 is nothing
			int left = 0, leftIdent = 0, leftLen = 0, leftStart1 = 0, leftStart2 = 0;

			for (int j = 1; j <= m; j++) {
				// gap in the first sequence: extend one, or open it from the cell to the left
				if (left - GAP_OPEN > e) {
					e = left - GAP_OPEN - GAP_EXTEND;
					eIdent = leftIdent; eLen = leftLen + 1; eStart1 = leftStart1; eStart2 = leftStart2;
				} else {
					e -= GAP_EXTEND;
					eLen++;
				}
				// gap in the second sequence: extend one, or open it from the cell above, which
				// is what h has at this point
				if (h[j] - GAP_OPEN > f[j]) {
					f[j] = h[j] - GAP_OPEN - GAP_EXTEND;
					fIdent[j] = hIdent[j]; fLen[j] = hLen[j] + 1; fStart1[j] = hStart1[j]; fStart2[j] = hStart2[j];
				} else {
					f[j] -= GAP_EXTEND;
					fLen[j]++;
				}

				boolean identical = seq1[i - 1] == seq2[j - 1];
				int score = diag + scores[seq2[j - 1]];
				int ident, len, start1, start2;
				if (diag > 0) {
					ident = diagIdent + (identical ? 1 : 0); len = diagLen + 1; start1 = diagStart1; start2 = diagStart2;
				} else {
					// nothing worth extending: the alignment starts here
					ident = identical ? 1 : 0; len = 1; start1 = i - 1; start2 = j - 1;
				}
				if (e > score) {
					score = e; ident = eIdent; len = eLen; start1 = eStart1; start2 = eStart2;
				}
				if (f[j] > score) {
					score = f[j]; ident = fIdent[j]; len = fLen[j]; start1 = fStart1[j]; start2 = fStart2[j];
				}
				if (score <= 0) {
					score = 0; ident = 0; len = 0;
				}

				// before overwriting h at this column, it is the upper left cell of the next one
				diag = h[j]; diagIdent = hIdent[j]; diagLen = hLen[j]; diagStart1 = hStart1[j]; diagStart2 = hStart2[j];
				h[j] = score; hIdent[j] = ident; hLen[j] = len; hStart1[j] = start1; hStart2[j] = start2;
				left = score; leftIdent = ident; leftLen = len; leftStart1 = start1; leftStart2 = start2;

				if (score > best) {
					best = score; bestIdent = ident; bestLen = len;
					bestStart1 = start1; bestStart2 = start2; bestEnd1 = i - 1; bestEnd2 = j - 1;
				}
			}
		}
		return new LocalAlignment(best, bestIdent, bestLen, bestStart1, bestEnd1, bestStart2, bestEnd2);
	}

	private static byte[] encode(String seq) {
		byte[] encoded = new byte[seq.length()];
		for (int i = 0; i < encoded.length; i++) {
			int letter = Character.toUpperCase(seq.charAt(i)) - 'A';
			encoded[i] = (byte) (letter >= 0 && letter < NUM_LETTERS ? letter : UNKNOWN - 'A');
		}
		return encoded;
	}

	private static int[][] initMatrix() {
		SubstitutionMatrix<AminoAcidCompound> blosum62 = SubstitutionMatrixHelper.getBlosum62();
		AminoAcidCompoundSet compounds = AminoAcidCompoundSet.getAminoAcidCompoundSet();
		String standard = "ACDEFGHIKLMNPQRSTVWY";
		AminoAcidCompound[] byLetter = new AminoAcidCompound[NUM_LETTERS];
		for (int i = 0; i < NUM_LETTERS; i++) {
			char letter = (char) ('A' + i);
			byLetter[i] = compounds.getCompoundForString(String.valueOf(standard.indexOf(letter) >= 0 ? letter : UNKNOWN));
		}
		int[][] matrix = new int[NUM_LETTERS][NUM_LETTERS];
		for (int i = 0; i < NUM_LETTERS; i++) {
			for (int j = 0; j < NUM_LETTERS; j++) {
				matrix[i][j] = blosum62.getValue(byLetter[i], byLetter[j]);
			}
		}
		return matrix;
	}
}
