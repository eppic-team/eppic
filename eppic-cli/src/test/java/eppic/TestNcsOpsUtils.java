package eppic;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4d;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.junit.Test;

import eppic.assembly.TestLatticeGraph;

public class TestNcsOpsUtils {

	/**
	 * 3zxa is a viral capsid in I 2 2 2 sitting on the 222 site, with the full set of icosahedral
	 * operators given as NCS operators: only 15 of the 60 copies are unique in the crystal.
	 */
	@Test
	public void test3zxa() throws Exception {
		Structure s = TestLatticeGraph.getStructure("3zxa");

		assertEquals(59, s.getCrystallographicInfo().getNcsOperators().length);

		assertEquals(45, NcsOpsUtils.removeCrystallographicNcsOps(s));

		assertEquals(14, s.getCrystallographicInfo().getNcsOperators().length);

		// a second call should not remove anything else
		assertEquals(0, NcsOpsUtils.removeCrystallographicNcsOps(s));

		Map<String,String> chainOrigNames = new HashMap<>();
		Map<String, Matrix4d> chainNcsOps = new HashMap<>();
		CrystalBuilder.expandNcsOps(s, chainOrigNames, chainNcsOps);

		assertEquals(15, s.getPolyChains().size());

		// no chains must overlap after expanding
		CrystalBuilder cb = new CrystalBuilder(s, chainOrigNames, chainNcsOps);
		StructureInterfaceList interfaces = cb.getUniqueInterfaces();
		assertFalse(interfaces.size() == 0);
		for (StructureInterface interf : interfaces) {
			assertTrue(interf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size() < EppicParams.NUM_CLASHES_FOR_ERROR);
		}
	}

	/**
	 * 4pb6 has the crystallographic 3-fold of P 21 3 given as NCS operators: all of them are redundant.
	 */
	@Test
	public void test4pb6() throws Exception {
		Structure s = TestLatticeGraph.getStructure("4pb6");

		assertEquals(2, s.getCrystallographicInfo().getNcsOperators().length);

		assertEquals(2, NcsOpsUtils.removeCrystallographicNcsOps(s));

		assertNull(s.getCrystallographicInfo().getNcsOperators());
	}

	/**
	 * 1auy has proper NCS operators: none must be removed.
	 */
	@Test
	public void test1auy() throws Exception {
		Structure s = TestLatticeGraph.getStructure("1auy");

		int numOps = s.getCrystallographicInfo().getNcsOperators().length;

		assertEquals(0, NcsOpsUtils.removeCrystallographicNcsOps(s));

		assertEquals(numOps, s.getCrystallographicInfo().getNcsOperators().length);
	}
}
