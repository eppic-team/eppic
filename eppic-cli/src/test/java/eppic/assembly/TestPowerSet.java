package eppic.assembly;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the functions of {@link PowerSet}.
 * 
 * @author Aleix Lafita
 *
 */
public class TestPowerSet {

	/**
	 * Test that all the possible subsets of the active indices of a set are
	 * returned.
	 */
	@Test
	public void testGetOnPowerSet() {

		PowerSet original = new PowerSet(new boolean[] { false, false, false });

		// Test the empty set
		Set<PowerSet> pss = original.getOnPowerSet(0);
		assertEquals("Empty PowerSet has no subsets", 0, pss.size());

		// If there is only one active index there is only one empty subset
		original.switchOn(0);
		pss = original.getOnPowerSet(0);
		assertEquals("PowerSet of size one has one subset", 1, pss.size());
		for (PowerSet ps : pss) {
			assertEquals("The subset of PowerSet of size 1 is the empty PowerSet", 0, ps.sizeOn());
		}

		// Set of two active indices has two possible subsets
		original.switchOn(1);
		pss = original.getOnPowerSet(0);
		assertEquals("PowerSet of size two has three subsets", 3, pss.size());
		for (PowerSet ps : original.getOnPowerSet(1)) {
			assertEquals("The subsets of PowerSet of size 2 at one distance have one active index.", 1,
					ps.sizeOn());
		}

		// Set of three active indices has eight possible subsets
		original.switchOn(2);
		pss = original.getOnPowerSet(0);
		assertEquals("PowerSet of size three has eight subsets", 7, pss.size());
		for (PowerSet ps : original.getOnPowerSet(2)) {
			assertEquals("The subsets of PowerSet of size 3 at one distance have two active indices.", 2,
					ps.sizeOn());
		}
	}

	/**
	 * Test how long does it take to compute all subsets of a PowerSet of size
	 * 10 and 11, given that the current threshold for maximum is 10.
	 */
	@Test
	@Ignore
	public void testPerformance() {
		
		boolean[] set = new boolean[10];
		Arrays.fill(set, true);
		PowerSet original = new PowerSet(set);

		long time10 = System.currentTimeMillis();
		original.getOnPowerSet(0);
		time10 = (System.currentTimeMillis() - time10) / 1000;
		System.out.print(String.format("Computing PowerSet of size 10 took %d seconds", time10));
		
		set = new boolean[11];
		Arrays.fill(set, true);
		original = new PowerSet(set);

		long time11 = System.currentTimeMillis();
		original.getOnPowerSet(0);
		time11 = (System.currentTimeMillis() - time11) / 1000;
		System.out.print(String.format("Computing PowerSet of size 11 took %d seconds",time11));
		
		assertTrue(time11 > time10);
	}
}
