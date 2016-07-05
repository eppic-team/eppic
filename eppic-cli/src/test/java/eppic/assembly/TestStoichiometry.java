/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on May 2, 2016
 * Author: blivens 
 *
 */
 
package eppic.assembly;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.biojava.nbio.structure.contact.Pair;
import org.junit.Test;
 
public class TestStoichiometry {

	@Test
	public void testMerge() {
		Stoichiometry<Integer> a = new Stoichiometry<Integer>(
				Arrays.asList(1,2,3,2,3,3),null);
		Stoichiometry<Integer> b = new Stoichiometry<Integer>(
				Arrays.asList(2,3,4,4,4,4,5,5,5,5,5,2),null);
		
		Pair<Stoichiometry<Integer>> merged = Stoichiometry.mergeValues(a, b);
		Stoichiometry<Integer> a2 = merged.getFirst();
		Stoichiometry<Integer> b2 = merged.getSecond();
		
		assertEquals(Arrays.asList(1,2,3,4,5),a2.getValues());
		assertEquals(Arrays.asList(1,2,3,4,5),b2.getValues());
		
		assertArrayEquals(new int[]{1,2,3,0,0},a2.getStoichiometry());
		assertArrayEquals(new int[]{0,2,1,4,5},b2.getStoichiometry());
	}

}
