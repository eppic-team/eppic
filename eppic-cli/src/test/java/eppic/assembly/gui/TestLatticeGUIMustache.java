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

import static org.junit.Assert.assertTrue;

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

}
