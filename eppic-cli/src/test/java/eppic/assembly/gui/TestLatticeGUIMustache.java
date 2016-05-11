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
 
package eppic.assembly.gui;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
 
public class TestLatticeGUIMustache {

	@Test
	public void testKnownTemplates() throws IOException {
		List<String> knownTemplates = LatticeGUIMustache.getKnownTemplates().collect(Collectors.toList());
		assertTrue(knownTemplates.size()>0);
		assertTrue(knownTemplates.stream().anyMatch(
				t -> t.endsWith(LatticeGUIMustache.TEMPLATE_3DMOL))
				);
		assertTrue(knownTemplates.stream().anyMatch(
				t -> t.endsWith(LatticeGUIMustache.TEMPLATE_ASSEMBLY_DIAGRAM_FULL))
				);
	}
	
	@Test
	public void testParseInterfaceList() {
		List<Integer> result;
		
		result = LatticeGUIMustache.parseInterfaceList(null);
		assertNull(result);
		
		result = LatticeGUIMustache.parseInterfaceList("*");
		assertNull(result);

		result = LatticeGUIMustache.parseInterfaceList("");
		assertEquals(0,result.size());
		
		result = LatticeGUIMustache.parseInterfaceList("1");
		assertEquals(1,result.size());
		assertEquals(1,(int)result.get(0));
		
		result = LatticeGUIMustache.parseInterfaceList("2,3");
		assertEquals(2,result.size());
		assertEquals(2,(int)result.get(0));
		assertEquals(3,(int)result.get(1));

		result = LatticeGUIMustache.parseInterfaceList("4-6");
		assertEquals(3,result.size());
		assertEquals(4,(int)result.get(0));
		assertEquals(5,(int)result.get(1));
		assertEquals(6,(int)result.get(2));
		
		result = LatticeGUIMustache.parseInterfaceList(" 2,\n  3\t");
		assertEquals(2,result.size());
		assertEquals(2,(int)result.get(0));
		assertEquals(3,(int)result.get(1));

	}

}
