package eppic.commons.sequence;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class TestSequenceClusterer {

	private static final String AMINO_ACIDS = "ACDEFGHIKLMNPQRSTVWY";

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
	 * Letters other than the 20 standard amino acids are taken as X, and sequences with nothing in
	 * common or with nothing at all are clusters of their own
	 */
	@Test
	public void testUnusualSequences() {
		String seq = "MTKPVLQDGETVVCQGTHAAWYRSFNEMTKPVLQDGETVVCQGTHAAWYRSFNE";
		String unusual = seq.substring(0, 20) + "UBZXOJ*" + seq.substring(27);
		List<String> ids = List.of("unusual", "lower", "x", "standard", "unrelated", "empty");
		List<String> seqs = List.of(unusual, unusual.toLowerCase(), seq.substring(0, 20) + "XXXXXXX" + seq.substring(27), seq,
				"GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG", "");

		assertEquals(List.of(List.of("unusual", "lower", "x"), List.of("standard"), List.of("unrelated"), List.of("empty")),
				new SequenceClusterer(0.95, 0.9).cluster(ids, seqs, 1));
		assertEquals(List.of(List.of("unusual", "lower", "x", "standard"), List.of("unrelated"), List.of("empty")),
				new SequenceClusterer(0.85, 0.9).cluster(ids, seqs, 1));
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
