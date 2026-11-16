package eppic;

import eppic.commons.blast.BlastHit;
import eppic.commons.blast.BlastHsp;
import eppic.commons.sequence.UnirefEntry;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Tests the redundancy reduction of a list of homologs when they are few, which is done
 * without mmseqs2.
 */
public class TestHomologListRedundancy {

	private static final String QUERY =
			"MTKPVLQDGETVVCQGTHAAWYRSFNEDLKHGIPMCQAVTSWERTYLKHDGFNACVPMQSTREWYIKLHGDFNMACVPQSTREWKLYHG";

	@Test
	public void testReduceRedundancy() throws Exception {
		UnirefEntry ref = new UnirefEntry();
		ref.setUniprotId("QUERY0");
		ref.setSequence(QUERY);
		HomologList homologs = new HomologList(ref);

		List<Homolog> list = new ArrayList<>();
		// in the order of a sequence search: the closest to the query first
		list.add(createHomolog("CLOSE1", substitute(QUERY, 10, 20, 30, 40, 50, 60, 70, 80)));
		list.add(createHomolog("CLOSE2", substitute(QUERY, 10, 20, 30, 40, 50, 60, 70, 80, 85))); // 1 mismatch to CLOSE1
		list.add(createHomolog("OTHER1", substitute(QUERY, 1, 6, 11, 16, 21, 26, 31, 36, 41, 46, 51, 56, 61, 66, 71, 76, 81)));
		list.add(createHomolog("CLOSE3", substitute(QUERY, 10, 20, 30, 40, 50, 60, 70, 80, 84))); // 1 mismatch to CLOSE1, 2 to CLOSE2
		list.add(createHomolog("OTHER2", substitute(QUERY, 3, 8, 13, 18, 23, 28, 33, 38, 43, 48, 53, 58, 63, 68, 73, 78, 83)));
		// only the second half of the sequence of this one matches the query, and it is identical to that of CLOSE1
		String halfMatching = "GSHMASNDYTQQATQSYGAYPTQPGQGYSQQSSQPYGQQSYSGYSQ" + list.get(0).getSequence().substring(45);
		Homolog partial = createHomolog("PARTIAL", halfMatching);
		partial.getBlastHsp().setSubjectStart(47);
		partial.getBlastHsp().setQueryStart(46);
		partial.getBlastHsp().setAliLength(QUERY.length() - 45);
		partial.getBlastHsp().setIdentities(QUERY.length() - 45 - 4);
		list.add(partial);

		Field listField = HomologList.class.getDeclaredField("list");
		listField.setAccessible(true);
		listField.set(homologs, list);

		homologs.filterToMinIdAndCoverage(0.5, 0.4);
		assertEquals(6, homologs.getSizeFilteredSubset());

		File scratchDir = Files.createTempDirectory("testHomologList").toFile();
		// the mmseqs2 executable is not needed for so few homologs
		homologs.reduceRedundancy(100, new File("/nonexistent/mmseqs"), scratchDir, 2);
		scratchDir.delete();

		// of the 3 redundant ones the closest to the query is kept. The one matching in half of
		// its sequence only is not redundant with them: what is compared is the matching part
		// and it is half of theirs
		assertEquals(List.of("CLOSE1_1-89", "OTHER1_1-89", "OTHER2_1-89", "PARTIAL_47-90"),
				homologs.getFilteredSubset().stream().map(Homolog::getIdentifier).collect(Collectors.toList()));

		// and again, as is done for every identity cutoff tried
		homologs.filterToMinIdAndCoverage(0.5, 0.6);
		assertEquals(5, homologs.getSizeFilteredSubset());
		homologs.reduceRedundancy(100, new File("/nonexistent/mmseqs"), scratchDir, 1);
		assertEquals(List.of("CLOSE1_1-89", "OTHER1_1-89", "OTHER2_1-89"),
				homologs.getFilteredSubset().stream().map(Homolog::getIdentifier).collect(Collectors.toList()));
	}

	/**
	 * A homolog matching the query over the whole of both sequences
	 */
	private static Homolog createHomolog(String uniId, String sequence) {
		BlastHit hit = new BlastHit();
		hit.setQueryId("QUERY0");
		hit.setQueryLength(QUERY.length());
		hit.setSubjectId(uniId);
		hit.setSubjectLength(sequence.length());
		BlastHsp hsp = new BlastHsp(hit);
		hsp.setQueryStart(1);
		hsp.setQueryEnd(QUERY.length());
		hsp.setSubjectStart(1);
		hsp.setSubjectEnd(sequence.length());
		hsp.setAliLength(QUERY.length());
		int identities = 0;
		for (int i = 0; i < Math.min(QUERY.length(), sequence.length()); i++) {
			if (QUERY.charAt(i) == sequence.charAt(i)) identities++;
		}
		hsp.setIdentities(identities);
		UnirefEntry uniref = new UnirefEntry();
		uniref.setUniprotId(uniId);
		uniref.setSequence(sequence);
		return new Homolog(hsp, uniref);
	}

	/**
	 * Substitutes the residues at the given positions by different ones
	 */
	private static String substitute(String seq, int... positions) {
		StringBuilder sb = new StringBuilder(seq);
		for (int pos : positions) {
			sb.setCharAt(pos, seq.charAt(pos) == 'W' ? 'P' : 'W');
		}
		return sb.toString();
	}
}
