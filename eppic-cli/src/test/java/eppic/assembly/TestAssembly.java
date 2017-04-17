package eppic.assembly;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAssembly {
	
	private static final Logger logger = LoggerFactory.getLogger(TestAssembly.class);

	@Test
	public void testIsChild() {
		
		boolean[] b1 = {false,false,false,false};
		PowerSet a1 = new PowerSet(b1);
		
		boolean[] b2 = {true,false,false,false};
		PowerSet a2 = new PowerSet(b2);
		
		assertTrue(a2.isChild(a1));
		
		boolean[] b3 = {true,false,true,false};
		PowerSet a3 = new PowerSet(b3);
		
		assertTrue(a3.isChild(a2));
		assertTrue(a3.isChild(a1));
		
		boolean[] b4 = {false,false,true,false};
		PowerSet a4 = new PowerSet(b4);
		
		assertTrue(a4.isChild(a1));
		assertFalse(a4.isChild(a2));
		assertFalse(a4.isChild(a3));
		
		boolean[] b5 = {true,true,true,false};
		PowerSet a5 = new PowerSet(b5);

		assertTrue(a5.isChild(a1));
		assertTrue(a5.isChild(a4));
		assertTrue(a5.isChild(a3));
		
		boolean[] b6 = {false,true,false,true};
		PowerSet a6 = new PowerSet(b6);

		assertTrue(a6.isChild(a1));
		assertFalse(a6.isChild(a2));
		assertFalse(a6.isChild(a3));
		assertFalse(a6.isChild(a4));
		assertFalse(a6.isChild(a5));
	}

	@Test
	public void testGetChildren() {

		int size = 6;
		
		PowerSet emptyPowerSet = new PowerSet(new boolean[size]);
		
		Set<PowerSet> prevLevel = new HashSet<PowerSet>();
		prevLevel.add(emptyPowerSet);
		Set<PowerSet> nextLevel = null;

		
		for (int k = 1; k<=size;k++) {


			nextLevel = new HashSet<PowerSet>();

			for (PowerSet p:prevLevel) {
				List<PowerSet> children = p.getChildren(new ArrayList<PowerSet>());

				for (PowerSet c:children) {
					
					// testing that all members of the current children list is a child of the parent
					assertTrue(c.isChild(p));
					
					nextLevel.add(c);
					
				}
			}
			prevLevel = new HashSet<PowerSet>(nextLevel);
			

		}
	}
	
	@Test
	public void testInfiniteLoopIssue() {
		// This test case corresponds to assembly scoring of 1y1w, assembly 2: {1,2,3,4,5,6,7,8,9,12,15,17,20,27}
		// There are 41 interface clusters in total
		int MAX_NUM_ENGAGED_IFACES_SCORING = 10;
		int id = 2;
		// values of the probabilities for all interface clusters extracted from db
		double[] probs = 
			{1,	0.999207829709885, 0.99917914246944, 0.999591174239929,
				0.985298570766897, 0.977379187494439, 0.542933939208556,
				0.999962547627599, 0.993578709814513, 0.0369461982241252,
				0.945557605607499, 0.605003437421438, 0.448653155359492,
				0.11484403384865, 0.162465062580696, 0.162465062580696,
				0.209159365213063, 0.329598840191131, 0.0226783427611989,
				0, 0.279286511563528, 0,
				0.5, 0.209159365213063,	0,
				0, 0.5, 0.162465062580696,
				0, 0, 0,
				0.162465062580696, 0, 0,
				0, 0, 0,
				0, 0, 0,
				0
		};
		assertEquals(41, probs.length);
		PowerSet reducedSet = new PowerSet(41);
		int[] engagedInterfClusters = {1,2,3,4,5,6,7,8,9,12,15,17,20,27};
		for (int toEngage : engagedInterfClusters) {
			reducedSet.switchOn(toEngage-1);
		}
		
		if (reducedSet.sizeOn() > MAX_NUM_ENGAGED_IFACES_SCORING) {
			logger.warn("There are {} engaged interface clusters in assembly {}. "
					+ "They will be reduced to compute assembly scoring.", 
					reducedSet.sizeOn(), id);
			
			int numIterations = 0;
			
			while (reducedSet.sizeOn() > MAX_NUM_ENGAGED_IFACES_SCORING) {
				
				// Find the lowest probability cluster
				int index = 0;
				double probability = 1;
				for (int i = 1; i < reducedSet.size() + 1; i++) {
					if (reducedSet.isOff(i-1)) continue; // we want to loop only over the engaged interfaces
					double p = probs[i-1];
					if (p <= probability) {
						index = i - 1;
						probability = p;
					}
				}
				// in cases like 3unb, this log line can fill gigabytes of logs... making it debug
				logger.info("Disengaging interface cluster {} for assembly {} scoring",
						index + 1, id);
				if (probability > 0.1) {
					logger.warn("Disengaging interface cluster {} of assembly {} "
							+ "scoring, with probability {} of being biologically "
							+ "relevant. Significant probability density might be "
							+ "missing for the score of this assembly.", index + 1,
							id, String.format("%.2f", probability));
				}
				
				reducedSet.switchOff(index);
				numIterations++;
				// this test case is to catch a problem with an infinite loop
				assertTrue(numIterations<1000);
			}
		}
	}
}
