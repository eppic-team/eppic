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
		Assembly a1 = new Assembly(b1);
		
		boolean[] b2 = {true,false,false,false};
		Assembly a2 = new Assembly(b2);
		
		assertTrue(a2.isChild(a1));
		
		boolean[] b3 = {true,false,true,false};
		Assembly a3 = new Assembly(b3);
		
		assertTrue(a3.isChild(a2));
		assertTrue(a3.isChild(a1));
		
		boolean[] b4 = {false,false,true,false};
		Assembly a4 = new Assembly(b4);
		
		assertTrue(a4.isChild(a1));
		assertFalse(a4.isChild(a2));
		assertFalse(a4.isChild(a3));
		
		boolean[] b5 = {true,true,true,false};
		Assembly a5 = new Assembly(b5);

		assertTrue(a5.isChild(a1));
		assertTrue(a5.isChild(a4));
		assertTrue(a5.isChild(a3));
		
		boolean[] b6 = {false,true,false,true};
		Assembly a6 = new Assembly(b6);

		assertTrue(a6.isChild(a1));
		assertFalse(a6.isChild(a2));
		assertFalse(a6.isChild(a3));
		assertFalse(a6.isChild(a4));
		assertFalse(a6.isChild(a5));
	}

	@Test
	public void testGetChildren() {

		int size = 6;
		
		Assembly emptyAssembly = new Assembly(new boolean[size]);
		
		Set<Assembly> prevLevel = new HashSet<Assembly>();
		prevLevel.add(emptyAssembly);
		Set<Assembly> nextLevel = null;

		
		for (int k = 1; k<=size;k++) {


			nextLevel = new HashSet<Assembly>();

			for (Assembly p:prevLevel) {
				List<Assembly> children = p.getChildren(new ArrayList<Assembly>());

				for (Assembly c:children) {
					
					// testing that all members of the current children list is a child of the parent
					assertTrue(c.isChild(p));
					
					nextLevel.add(c);
					
				}
			}
			prevLevel = new HashSet<Assembly>(nextLevel);
			

		}
	}
}
