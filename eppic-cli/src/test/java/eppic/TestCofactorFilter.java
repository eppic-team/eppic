package eppic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.HetatomImpl;
import org.junit.Test;

public class TestCofactorFilter {

	private static final CofactorFilter FILTER = new CofactorFilter(
			EppicParams.DEF_MIN_SIZE_COFACTOR_FOR_ASA, EppicParams.DEF_MIN_SIZE_METAL_COFACTOR_FOR_ASA);

	/**
	 * Makes a group with the given chemical component id and the given number of atoms of each element,
	 * passed as element, count, element, count...
	 */
	private static Group group(String ccdId, Object... elementCounts) {
		Group g = new HetatomImpl();
		g.setPDBName(ccdId);
		int serial = 1;
		for (int i = 0; i < elementCounts.length; i += 2) {
			Element e = (Element) elementCounts[i];
			int count = (Integer) elementCounts[i + 1];
			for (int j = 0; j < count; j++) {
				Atom a = new AtomImpl();
				a.setElement(e);
				a.setName(e.name().toUpperCase() + j);
				a.setPDBserial(serial++);
				g.addAtom(a);
			}
		}
		return g;
	}

	@Test
	public void testPdbeCofactorList() {
		Set<String> codes = CofactorFilter.getPdbeCofactors();
		assertEquals(364, codes.size());
		for (String code : new String[] {"HEM", "HEC", "FAD", "FMN", "NAD", "PLP", "TPP", "SAM", "BTN", "B12", "F43", "MGD"}) {
			assertTrue(code + " should be in the PDBe cofactor list", codes.contains(code));
		}
		for (String code : new String[] {"SF4", "FES", "CLA", "ATP", "ZN", "HG"}) {
			assertFalse(code + " should not be in the PDBe cofactor list", codes.contains(code));
		}
	}

	@Test
	public void testCuratedRule() {
		// PLP, 15 atoms: below both size cutoffs
		assertEquals(CofactorFilter.Rule.CURATED, FILTER.getCofactorRule(group("PLP", Element.C, 8, Element.N, 1, Element.O, 5, Element.P, 1)));
		// heme is in the list too, the curated rule comes first
		assertEquals(CofactorFilter.Rule.CURATED, FILTER.getCofactorRule(group("HEM", Element.C, 34, Element.N, 4, Element.O, 4, Element.Fe, 1)));
	}

	@Test
	public void testMetalRule() {
		// P-cluster Fe8S7: 15 atoms
		assertEquals(CofactorFilter.Rule.METAL, FILTER.getCofactorRule(group("CLF", Element.Fe, 8, Element.S, 7)));
		// Mn4CaO5 cluster of photosystem II: 10 atoms
		assertEquals(CofactorFilter.Rule.METAL, FILTER.getCofactorRule(group("OEX", Element.Mn, 4, Element.Ca, 1, Element.O, 5)));
		// Fe4S4 cluster: 8 atoms, below the metal cofactor size
		assertNull(FILTER.getCofactorRule(group("SF4", Element.Fe, 4, Element.S, 4)));
		// an Fe4S4 cluster is a cofactor with a lower cutoff
		assertEquals(CofactorFilter.Rule.METAL, new CofactorFilter(40, 8).getCofactorRule(group("SF4", Element.Fe, 4, Element.S, 4)));
		// metal ions
		assertNull(FILTER.getCofactorRule(group("ZN", Element.Zn, 1)));
		assertNull(new CofactorFilter(40, 0).getCofactorRule(group("ZN", Element.Zn, 1)));
	}

	@Test
	public void testNonBiologicalMetals() {
		// a mercury compound of 18 atoms (like ethylmercurithiosalicylate)
		assertNull(FILTER.getCofactorRule(group("EMC", Element.C, 9, Element.O, 2, Element.S, 1, Element.Hg, 1, Element.Na, 1)));
		// a gadolinium complex of 27 atoms
		assertNull(FILTER.getCofactorRule(group("GD3", Element.C, 17, Element.N, 4, Element.O, 5, Element.Gd, 1)));
		// a large one is still a cofactor by size: e.g. a heme analog with a non-biological metal
		assertEquals(CofactorFilter.Rule.SIZE, FILTER.getCofactorRule(group("XXX", Element.C, 34, Element.N, 4, Element.O, 4, Element.Ru, 1)));
	}

	@Test
	public void testPolyoxometalates() {
		// Keggin phosphotungstate PW12O40: biological metals only, but an additive
		Group keggin = group("KEG", Element.P, 1, Element.W, 12, Element.O, 40);
		assertTrue(CofactorFilter.isPolyoxometalateLike(keggin));
		// not a cofactor even if above the size cutoff
		assertNull(FILTER.getCofactorRule(keggin));
		// decavanadate V10O28: not a cofactor
		assertNull(FILTER.getCofactorRule(group("DVT", Element.V, 10, Element.O, 28)));
		// FeMoco Fe7MoS9C is not a polyoxometalate
		Group femoco = group("ICS", Element.Fe, 7, Element.Mo, 1, Element.S, 9, Element.C, 1);
		assertFalse(CofactorFilter.isPolyoxometalateLike(femoco));
		assertEquals(CofactorFilter.Rule.METAL, FILTER.getCofactorRule(femoco));
	}

	@Test
	public void testSizeRule() {
		assertEquals(CofactorFilter.Rule.SIZE, FILTER.getCofactorRule(group("XXX", Element.C, 30, Element.O, 10)));
		assertNull(FILTER.getCofactorRule(group("XXX", Element.C, 30, Element.O, 9)));
		// Hydrogens don't count
		assertNull(FILTER.getCofactorRule(group("XXX", Element.C, 30, Element.O, 9, Element.H, 30)));
	}

	@Test
	public void testDisabled() {
		CofactorFilter disabled = new CofactorFilter(-1, EppicParams.DEF_MIN_SIZE_METAL_COFACTOR_FOR_ASA);
		assertTrue(disabled.isDisabled());
		assertNull(disabled.getCofactorRule(group("HEM", Element.C, 34, Element.N, 4, Element.O, 4, Element.Fe, 1)));
		assertNull(disabled.getCofactorRule(group("CLF", Element.Fe, 8, Element.S, 7)));
	}
}
