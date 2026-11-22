package eppic;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;

import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureImpl;
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

		assertTrue(NcsOpsUtils.findInvalidNcsOps(s).isEmpty());
	}

	/**
	 * The values of _struct_ncs_oper in the 3zfg mmCIF are in MTRIX order (translation after each
	 * row), so none of the operators read are rotations except the identity.
	 */
	@Test
	public void test3zfgInvalidNcsOps() throws Exception {
		Structure s = TestLatticeGraph.getStructure("3zfg");

		int numOps = s.getCrystallographicInfo().getNcsOperators().length;
		List<String> invalid = NcsOpsUtils.findInvalidNcsOps(s);

		assertFalse(invalid.isEmpty());
		assertTrue(invalid.size() <= numOps);
	}

	@Test
	public void testFindInvalidNcsOps() {
		Structure s = new StructureImpl();
		PDBCrystallographicInfo xtalInfo = new PDBCrystallographicInfo();
		s.setCrystallographicInfo(xtalInfo);

		// no operators
		assertTrue(NcsOpsUtils.findInvalidNcsOps(s).isEmpty());

		// a proper rotation (90 degrees about z) with a translation, rounded to 6 decimals as deposited
		Matrix4d rotation = new Matrix4d(
				0.000001, -1, 0, 10.5,
				1, 0.000001, 0, -3.2,
				0, 0, 1, 100,
				0, 0, 0, 1);
		// a mirror: orthonormal but determinant -1
		Matrix4d mirror = new Matrix4d(
				-1, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1);
		// a rotation whose last column got shifted into the matrix, as in 3zfg
		Matrix4d shifted = new Matrix4d(
				0.809017, 0.5, -0.309017, 0,
				0.5, -0.309017, 0.809017, 0,
				0.809017, 0, 0.5, 0,
				0, 0, 0, 1);

		xtalInfo.setNcsOperators(new Matrix4d[] {rotation});
		assertTrue(NcsOpsUtils.findInvalidNcsOps(s).isEmpty());

		xtalInfo.setNcsOperators(new Matrix4d[] {rotation, mirror, shifted});
		List<String> invalid = NcsOpsUtils.findInvalidNcsOps(s);
		assertEquals(2, invalid.size());
		assertTrue(invalid.get(0).startsWith("NCS operator 2 of 3"));
		assertTrue(invalid.get(0).contains("determinant"));
		assertTrue(invalid.get(1).startsWith("NCS operator 3 of 3"));
	}
}
