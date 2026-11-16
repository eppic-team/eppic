package eppic.commons.sequence;

import eppic.commons.sequence.SequenceClusterer.LocalAlignment;
import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.PairwiseSequenceAligner;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * The class tested is in eppic-util: the test is here because in the tests of that module
 * the BioJava sequence classes, which this needs to check the alignments, can't be loaded.
 */
public class TestSequenceClusterer {

	private static final String AMINO_ACIDS = "ACDEFGHIKLMNPQRSTVWY";

	/**
	 * The alignment must be an optimal one: checked against the Smith-Waterman of BioJava
	 */
	@Test
	public void testAlignmentAgainstBiojava() throws CompoundNotFoundException {
		Random random = new Random(12345);
		List<String> seqs = new ArrayList<>();
		for (int family = 0; family < 4; family++) {
			String parent = randomSequence(random, 60 + 40 * family);
			seqs.add(parent);
			// members from very close to very far, with substitutions, insertions and deletions
			for (double rate : new double[] {0.02, 0.05, 0.1, 0.2, 0.4}) {
				seqs.add(mutate(random, parent, rate));
			}
		}
		// and a sequence with letters other than the 20 standard amino acids
		seqs.add(seqs.get(0).substring(0, 20) + "XBZU" + seqs.get(0).substring(24));

		int numPairs = 0;
		for (int i = 0; i < seqs.size(); i++) {
			for (int j = i + 1; j < seqs.size(); j++) {
				String seq1 = seqs.get(i);
				String seq2 = seqs.get(j);
				LocalAlignment aln = SequenceClusterer.align(seq1, seq2);

				PairwiseSequenceAligner<ProteinSequence, AminoAcidCompound> aligner = Alignments.getPairwiseAligner(
						new ProteinSequence(seq1.replaceAll("[^" + AMINO_ACIDS + "]", "X")),
						new ProteinSequence(seq2.replaceAll("[^" + AMINO_ACIDS + "]", "X")),
						PairwiseSequenceAlignerType.LOCAL, new SimpleGapPenalty(11, 1), SubstitutionMatrixHelper.getBlosum62());
				assertEquals("Score of alignment of sequences " + i + " and " + j, (int) aligner.getScore(), aln.score());

				// and it must be consistent
				if (aln.score() > 0) {
					int aligned1 = aln.end1() - aln.start1() + 1;
					int aligned2 = aln.end2() - aln.start2() + 1;
					assertTrue(aln.start1() >= 0 && aln.end1() < seq1.length() && aligned1 > 0);
					assertTrue(aln.start2() >= 0 && aln.end2() < seq2.length() && aligned2 > 0);
					assertTrue(aln.length() >= Math.max(aligned1, aligned2));
					assertTrue(aln.length() <= aligned1 + aligned2);
					assertTrue(aln.numIdenticals() <= Math.min(aligned1, aligned2));
					// a local alignment starts and ends with identical or similar residues
					assertTrue(aln.numIdenticals() > 0);
				}
				numPairs++;
			}
		}
		assertEquals(25 * 24 / 2, numPairs);
	}

	@Test
	public void testAlignmentValues() {
		String seq = "MTKPVLQDGETVVCQGTHAAWYRSFNE";
		LocalAlignment aln = SequenceClusterer.align(seq, seq);
		assertEquals(new LocalAlignment(aln.score(), seq.length(), seq.length(), 0, seq.length() - 1, 0, seq.length() - 1), aln);
		assertEquals(1.0, aln.getIdentity(), 0.000001);

		// the second one within the first, with other residues around
		aln = SequenceClusterer.align("GGGGG" + seq + "GGG", seq);
		assertEquals(seq.length(), aln.numIdenticals());
		assertEquals(seq.length(), aln.length());
		assertEquals(5, aln.start1());
		assertEquals(5 + seq.length() - 1, aln.end1());
		assertEquals(0, aln.start2());
		assertEquals(seq.length() - 1, aln.end2());

		// a deletion of 2 residues in the middle of the second: 2 gap positions in the alignment
		aln = SequenceClusterer.align(seq, seq.substring(0, 13) + seq.substring(15));
		assertEquals(seq.length() - 2, aln.numIdenticals());
		assertEquals(seq.length(), aln.length());
		assertEquals(0, aln.start1());
		assertEquals(seq.length() - 1, aln.end1());
		assertEquals(0, aln.start2());
		assertEquals(seq.length() - 3, aln.end2());

		// nothing in common, and nothing at all
		assertEquals(0, SequenceClusterer.align("GGGGGGGG", "WWWWWWWW").length());
		assertEquals(0, SequenceClusterer.align("", seq).length());
		assertEquals(0.0, SequenceClusterer.align("", seq).getIdentity(), 0.000001);
	}

	/**
	 * The same case as in the mmseqs2 test
	 */
	@Test
	public void testClusteringByIdentity() {
		List<String> ids = List.of("0", "1", "2", "3");
		List<String> seqs = List.of(
				"MTKPVLQDGETVVCQGTHAA",
				"MTKPVLQDGETVVCQGTHAA",
				"MTKPVLQDGETVVCQGTHAA",
				"MTKPVLQDGESVVCQGTHAA"); // 1 mismatch out of 20 (5%)

		assertEquals(List.of(List.of("0", "1", "2"), List.of("3")), new SequenceClusterer(1.00, 0.9).cluster(ids, seqs, 1));
		assertEquals(List.of(List.of("0", "1", "2", "3")), new SequenceClusterer(0.94, 0.9).cluster(ids, seqs, 1));
		assertEquals(List.of(List.of("0", "1", "2"), List.of("3")), new SequenceClusterer(0.96, 0.9).cluster(ids, seqs, 1));
	}

	@Test
	public void testClusteringByCoverage() {
		String seq = "MTKPVLQDGETVVCQGTHAAWYRSFNEMTKPVLQDGETVVCQGTHAAWYRSFNE";
		List<String> ids = List.of("full", "part");
		// identical, but one is only 80% of the other
		List<String> seqs = List.of(seq, seq.substring(0, (int) (seq.length() * 0.8)));

		assertEquals(2, new SequenceClusterer(0.9, 0.9).cluster(ids, seqs, 1).size());
		assertEquals(1, new SequenceClusterer(0.9, 0.7).cluster(ids, seqs, 1).size());
	}

	/**
	 * The clusters are the connected components: a and c are not similar enough, but both are
	 * to b. Members and clusters come in the order given.
	 */
	@Test
	public void testClustersAreConnectedComponents() {
		String b = "MTKPVLQDGETVVCQGTHAAWYRSFNEMTKPVLQDGETVVCQGTHAAWYRSFNEMTKPVL"; // 60 residues
		String a = substitute(substitute(substitute(b, 10), 20), 30); // 3 mismatches to b: 95%
		String c = substitute(substitute(substitute(b, 15), 25), 35); // 3 mismatches to b, 6 to a: 90%
		String other = "GSHMASNDYTQQATQSYGAYPTQPGQGYSQQSSQPYGQQSYSGYSQSTDTSGYGQSSYSS";

		SequenceClusterer clusterer = new SequenceClusterer(0.93, 0.9);
		assertEquals(List.of(List.of("a"), List.of("c")), clusterer.cluster(List.of("a", "c"), List.of(a, c), 1));
		assertEquals(List.of(List.of("other"), List.of("a", "c", "b")),
				clusterer.cluster(List.of("other", "a", "c", "b"), List.of(other, a, c, b), 1));
		assertEquals(List.of(List.of("c", "b", "a"), List.of("other")),
				clusterer.cluster(List.of("c", "b", "other", "a"), List.of(c, b, other, a), 1));
	}

	@Test
	public void testClusteringWithThreads() {
		Random random = new Random(6789);
		List<String> ids = new ArrayList<>();
		List<String> seqs = new ArrayList<>();
		for (int family = 0; family < 6; family++) {
			String parent = randomSequence(random, 150);
			for (int member = 0; member < 12; member++) {
				ids.add(family + "_" + member);
				seqs.add(mutate(random, parent, member * 0.01));
			}
		}
		List<List<String>> clusters = new SequenceClusterer(0.9, 0.95).cluster(ids, seqs, 1);
		// the families have nothing to do with each other, and their closest members are redundant
		assertTrue(clusters.size() >= 6);
		assertTrue(clusters.size() < ids.size());
		for (List<String> cluster : clusters) {
			for (String member : cluster) {
				assertEquals(cluster.get(0).charAt(0), member.charAt(0));
			}
		}
		assertEquals(ids.size(), clusters.stream().mapToInt(List::size).sum());

		for (int numThreads : new int[] {2, 4, 8}) {
			assertEquals(clusters, new SequenceClusterer(0.9, 0.95).cluster(ids, seqs, numThreads));
		}

		// an instance that has seen the sequences before gives the same clusters, also for a subset
		SequenceClusterer clusterer = new SequenceClusterer(0.9, 0.95);
		assertEquals(clusters, clusterer.cluster(ids, seqs, 4));
		assertEquals(clusters, clusterer.cluster(ids, seqs, 1));
		assertEquals(new SequenceClusterer(0.9, 0.95).cluster(ids.subList(5, 50), seqs.subList(5, 50), 1),
				clusterer.cluster(ids.subList(5, 50), seqs.subList(5, 50), 1));
	}

	@Test
	public void testEstimateCost() {
		char[] residues = new char[100];
		Arrays.fill(residues, 'A');
		String seq100 = new String(residues);
		String seq95 = seq100.substring(0, 95);
		String seq50 = seq100.substring(0, 50);
		// the pair of 100 and 50 can't have 90% identity over 99% of both: it is not counted
		assertEquals(100 * 95, SequenceClusterer.estimateCost(List.of(seq100, seq95, seq50), 0.9, 0.99));
		assertEquals(100 * 95 + 100 * 50 + 95 * 50, SequenceClusterer.estimateCost(List.of(seq100, seq95, seq50), 0.5, 0.5));
		assertEquals(0, SequenceClusterer.estimateCost(List.of(seq100), 0.9, 0.99));
	}

	/**
	 * Substitutes the residue at the given position by a different one
	 */
	private static String substitute(String seq, int pos) {
		char residue = seq.charAt(pos) == 'W' ? 'P' : 'W';
		return seq.substring(0, pos) + residue + seq.substring(pos + 1);
	}

	private static String randomSequence(Random random, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(AMINO_ACIDS.charAt(random.nextInt(AMINO_ACIDS.length())));
		}
		return sb.toString();
	}

	/**
	 * Mutates the given sequence: every position has the given probability of being changed,
	 * most of the times substituted and sometimes deleted or with an insertion after it.
	 */
	private static String mutate(Random random, String seq, double rate) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seq.length(); i++) {
			if (random.nextDouble() >= rate) {
				sb.append(seq.charAt(i));
				continue;
			}
			int type = random.nextInt(10);
			if (type < 8) {
				sb.append(AMINO_ACIDS.charAt(random.nextInt(AMINO_ACIDS.length())));
			} else if (type == 8) {
				sb.append(seq.charAt(i)).append(randomSequence(random, 1 + random.nextInt(4)));
			}
			// else deleted
		}
		return sb.toString();
	}
}
