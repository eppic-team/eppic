package eppic;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.biojava.nbio.structure.quaternary.BioAssemblyInfo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eppic.analysis.compare.BiounitMatchResult;
import eppic.analysis.compare.BiounitMatchResult.MatchType;
import eppic.model.db.AssemblyDB;
import eppic.model.db.AssemblyScoreDB;
import eppic.model.db.PdbInfoDB;

/**
 * Tests for the graph-based PDB biounit matcher, {@link eppic.analysis.compare.BiounitGraphMatcher}
 */
public class TestBiounitGraphMatcher {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	private static File outDir;

	@BeforeClass
	public static void setUp() {
		outDir = new File(TMPDIR, "eppicTestBiounitGraphMatcher");
		outDir.mkdir();
		assertTrue(outDir.isDirectory());
	}

	@AfterClass
	public static void tearDown() {
		outDir.delete();
	}

	private static Main run(String pdbId) {
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);
		params.setBiounitMatcher(EppicParams.BiounitMatcher.GRAPH);
		Main m = new Main();
		m.run(params, false);
		return m;
	}

	private static BiounitMatchResult matchGraph(Main m, int biounitId) {
		Map<Integer, BioAssemblyInfo> bus = m.getStructure().getPDBHeader().getBioAssemblies();
		return m.getDataModelAdaptor().matchBioUnitByGraph(bus.get(biounitId), m.getCrystalAssemblies());
	}

	private static BiounitMatchResult matchOperators(Main m, int biounitId) {
		Map<Integer, BioAssemblyInfo> bus = m.getStructure().getPDBHeader().getBioAssemblies();
		return m.getDataModelAdaptor().matchBioUnitByOperators(bus.get(biounitId), m.getCrystalAssemblies());
	}

	/**
	 * 3gzh has 2 non-redundant biounits, both tetramers matching different assemblies, see issue #139.
	 * Same expectations as in {@link TestMultiPdbBiounits#test3gzh()}, with the graph matcher.
	 */
	@Test
	public void test3gzh() {
		Main m = run("3gzh");

		BiounitMatchResult r1 = matchGraph(m, 1);
		assertEquals(MatchType.EXACT, r1.getType());
		assertEquals("{1,2,3}", r1.getAssembly().toString());
		assertEquals(4, r1.getNumNodes());

		BiounitMatchResult r2 = matchGraph(m, 2);
		assertEquals(MatchType.EXACT, r2.getType());
		assertEquals("{2,4,8}", r2.getAssembly().toString());

		// and the data model has the pdb1/pdb2 annotations in the right assemblies
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		AssemblyDB assembly7 = pdbInfo.getValidAssemblies().get(6);
		assertEquals("{1,2,3}", assembly7.getInterfaceClusterIds());
		assertEquals(List.of("pdb1"), getPdbBioCalls(assembly7));
		AssemblyDB assembly8 = pdbInfo.getValidAssemblies().get(7);
		assertEquals("{2,4,8}", assembly8.getInterfaceClusterIds());
		assertEquals(List.of("pdb2"), getPdbBioCalls(assembly8));
	}

	/**
	 * 3cfh: the PDB biounit has a small interface cluster that EPPIC does not engage (issue #47).
	 * The operators matcher needs its "parent" fallback; the graph matcher finds the same assembly
	 * after removing the redundant edges.
	 */
	@Test
	public void test3cfh() {
		Main m = run("3cfh");

		BiounitMatchResult ops = matchOperators(m, 1);
		BiounitMatchResult graph = matchGraph(m, 1);

		assertEquals(MatchType.PARENT, ops.getType());
		assertEquals(MatchType.EXTRA_EDGES, graph.getType());
		assertTrue(graph.getNumRemovedEdges() > 0);
		assertEquals(ops.getAssembly(), graph.getAssembly());
		assertEquals(8, graph.getNumNodes());
	}

	/**
	 * 2a7n: C4 tetramer around a crystallographic 4-fold, interface operators are repeated in the biounit.
	 */
	@Test
	public void test2a7n() {
		Main m = run("2a7n");
		BiounitMatchResult graph = matchGraph(m, 1);
		assertEquals(MatchType.EXACT, graph.getType());
		assertEquals(matchOperators(m, 1).getAssembly(), graph.getAssembly());
		assertEquals(4, graph.getNumNodes());
		assertEquals(4, graph.getNumLabelledEdges());
	}

	/**
	 * 11bg: biounit 1 is a dimer (exact), biounit 2 a tetramer with extra small contacts.
	 */
	@Test
	public void test11bg() {
		Main m = run("11bg");
		BiounitMatchResult r1 = matchGraph(m, 1);
		assertEquals(MatchType.EXACT, r1.getType());
		assertEquals(2, r1.getNumNodes());

		BiounitMatchResult r2 = matchGraph(m, 2);
		assertTrue(r2.getType().isMatch());
		assertEquals(4, r2.getNumNodes());
		assertEquals(matchOperators(m, 2).getAssembly(), r2.getAssembly());
	}

	private static List<String> getPdbBioCalls(AssemblyDB assembly) {
		List<String> methods = new ArrayList<>();
		for (AssemblyScoreDB as : assembly.getAssemblyScores()) {
			if (as.getMethod().startsWith(DataModelAdaptor.PDB_BIOUNIT_METHOD_PREFIX) && as.getCallName().equals("bio")) {
				methods.add(as.getMethod());
			}
		}
		return methods;
	}
}
