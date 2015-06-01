package eppic.assembly;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestAssembly {

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
}
