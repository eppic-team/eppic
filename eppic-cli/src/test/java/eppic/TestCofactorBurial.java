package eppic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.ResidueNumber;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.junit.Test;

public class TestCofactorBurial {

	@Test
	public void testIsBuriedByCofactors() {
		double minAsa = 5;
		double cutoff = 0.8;
		// not surface to begin with
		assertFalse(CofactorBurial.isBuriedByCofactors(4, 0, minAsa, cutoff));
		// stops being surface
		assertTrue(CofactorBurial.isBuriedByCofactors(20, 5, minAsa, cutoff));
		// buried fraction at the cutoff
		assertTrue(CofactorBurial.isBuriedByCofactors(100, 20, minAsa, cutoff));
		// buried fraction below the cutoff (a pocket rim residue)
		assertFalse(CofactorBurial.isBuriedByCofactors(100, 21, minAsa, cutoff));
		// not touched by cofactors
		assertFalse(CofactorBurial.isBuriedByCofactors(100, 100, minAsa, cutoff));
	}

	/**
	 * Hemoglobin: the proximal histidines (alpha His87, beta His92) line the heme pocket
	 */
	@Test
	public void testHemoglobin() throws IOException, StructureException {
		Structure s = StructureIO.getStructure("4HHB");

		CofactorBurial cb = CofactorBurial.calculate(s, Arrays.asList("A", "B", "C", "D"), null,
				EppicParams.DEF_MIN_SIZE_COFACTOR_FOR_ASA, EppicParams.DEF_MIN_ASA_FOR_SURFACE, EppicParams.DEF_CA_CUTOFF_FOR_ZSCORE,
				1000, 1);

		for (String chainName : Arrays.asList("A", "B", "C", "D")) {
			assertTrue("Chain " + chainName + " should have residues buried by heme", cb.getNumBuried(chainName) > 0);
		}
		assertTrue(cb.isBuried("A", new ResidueNumber("A", 87, null)));
		assertTrue(cb.isBuried("B", new ResidueNumber("B", 92, null)));

		// no cofactors considered
		CofactorBurial noCofactors = CofactorBurial.calculate(s, Arrays.asList("A", "B", "C", "D"), null,
				-1, EppicParams.DEF_MIN_ASA_FOR_SURFACE, EppicParams.DEF_CA_CUTOFF_FOR_ZSCORE, 1000, 1);
		assertEquals(0, noCofactors.getNumBuried("A"));

		// the filtering removes exactly the buried residues
		List<Group> residues = s.getPolyChainByPDB("A").getAtomGroups();
		assertEquals(residues.size() - cb.getNumBuried("A"), cb.removeBuried("A", residues).size());
	}
}
