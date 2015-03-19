package eppic.assembly;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLatticeGraph {

	private static final Logger logger = LoggerFactory.getLogger(TestLatticeGraph.class);
	
	
	@Test
	public void testInterfaceEdge() throws IOException, StructureException {
		AtomCache cache = new AtomCache();
		FileParsingParameters params = new FileParsingParameters();
		params.setAlignSeqRes(true); 
		cache.setFileParsingParams(params);
		
		Structure s =  StructureIO.getStructure("1smt");
		
		CrystalBuilder cb = new CrystalBuilder(s);
		StructureInterfaceList interfaces = cb.getUniqueInterfaces();
		
		
		// we need InterfaceEdge to be unique per unique object, all the lattice graph code 
		// is implemented with that assumption in mind
		// This test checks for that, in case someone decides to add an equals() and hashCode() in InterfaceEdge
		InterfaceEdge edge1 = new InterfaceEdge(interfaces.get(1),new Point3i(0,0,0));
		InterfaceEdge edge2 = new InterfaceEdge(interfaces.get(1),new Point3i(0,0,0));
		
		InterfaceEdge edge3 = edge1;
		
		assertNotEquals(edge1,edge2);
		
		assertEquals(edge1, edge3);
		
		//LatticeGraph lg = new LatticeGraph(s, interfaces);
		
		//UndirectedGraph<ChainVertex, InterfaceEdge> g = lg.getGraph();		
		
		//for (InterfaceEdge e : g.edgeSet()) {
		//	
		//}
		
		
	}
	
	@Test
	public void testCycleDetection1smt() throws IOException, StructureException {

		// 1smt (P 1 21 1 with 1 entity and 2 molecules A,B)
		AssemblyFinder ab = getLatticeGraph("1smt");
		
		// cluster 1: isologous between A+B
		Assembly a = generateAssembly(ab, 1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		
		// cluster 3: classic infinite A+A on screw axis
		a = generateAssembly(ab, 3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection1r1z() throws IOException, StructureException {

		// 1r1z (P 1 with 1 entity and 4 molecules A,B,C,D)
		// the crystal contains a "hidden" cycle, see issue https://github.com/eppic-team/eppic-science/issues/45
		AssemblyFinder ab = getLatticeGraph("1r1z");
		
		// cluster 1: a C4 cycle
		Assembly a = generateAssembly(ab, 1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection1ble() throws IOException, StructureException {

		// 1ble (P 43 3 2 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations
		AssemblyFinder ab = getLatticeGraph("1ble");
		
		// cluster 1: a C3 cycle
		Assembly a = generateAssembly(ab, 1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		
		// cluster 3: isologous
		a = generateAssembly(ab, 3);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection4mml() throws IOException, StructureException {

		// 4mml (P 6 with 1 entity and 1 molecule A)
		AssemblyFinder ab = getLatticeGraph("4mml");
		
		// cluster 1: a C6 cycle
		Assembly a = generateAssembly(ab, 1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		
		// cluster 2: isologous
		a = generateAssembly(ab, 2);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());

		// cluster 3: classic infinite
		a = generateAssembly(ab, 3);		
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

		// cluster 4: classic infinite
		a = generateAssembly(ab, 4);		
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

		// clusters 1+2: an open cycle
		a = generateAssembly(ab, new int[]{1,2});		
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

	}
	
	@Test
	public void testCycleDetection4jf3() throws IOException, StructureException {

		// 4jf3 (P 3 with 1 entity and 2 molecules A,B): 
		// A,B are related by a pseudo 2-fold screw (actually the lattice is really a P 63 lattice)
		AssemblyFinder ab = getLatticeGraph("4jf3");
		
		// cluster 1: a C3 cycle
		Assembly a = generateAssembly(ab, 1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: a pseudo 3-fold screw
		a = generateAssembly(ab, 2);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());		
		
		// cluster 3: pseudo 2-fold screw
		a = generateAssembly(ab, 3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection3vkx() throws IOException, StructureException {

		// 3vkx (I 21 3 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations  
		AssemblyFinder ab = getLatticeGraph("3vkx");
		
		// cluster 1: a C3 cycle
		Assembly a = generateAssembly(ab, 1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: isologous
		a = generateAssembly(ab, 2);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());		
		
		// cluster 3: isologous
		a = generateAssembly(ab, 3);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());

		// cluster 4: 3-fold screw, classic infinite
		a = generateAssembly(ab, 4);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

	}

	@Test
	public void testCycleDetection4b29() throws IOException, StructureException {

		// 4b29 (I 21 3 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations  
		AssemblyFinder ab = getLatticeGraph("4b29");
		
		// cluster 1: isologous
		Assembly a = generateAssembly(ab, 1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: closed C3 cycle
		a = generateAssembly(ab, 2);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());		
		
		// cluster 3: open C3 cycle (classic infinite)
		a = generateAssembly(ab, 3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());		

	}

	private AssemblyFinder getLatticeGraph(String pdbId) throws IOException, StructureException {
		
		logger.info("Calculating interfaces for "+pdbId);
		
		AtomCache cache = new AtomCache();
		FileParsingParameters params = new FileParsingParameters();
		params.setAlignSeqRes(true); 
		cache.setFileParsingParams(params);
		
		Structure s =  StructureIO.getStructure(pdbId);
		
		CrystalBuilder cb = new CrystalBuilder(s);
		StructureInterfaceList interfaces = cb.getUniqueInterfaces();
		interfaces.calcAsas();
		interfaces.removeInterfacesBelowArea();
		interfaces.getClusters();
		
		AssemblyFinder ab = new AssemblyFinder(s, interfaces);
		
		ab.getLatticeGraph().removeDuplicateEdges();
		
		return ab; 
	}
	
	private Assembly generateAssembly(AssemblyFinder ab, int interfaceClusterId) {
		int[] interfaceClusterIds = new int[] {interfaceClusterId};
		return generateAssembly(ab, interfaceClusterIds);
	}
	
	private Assembly generateAssembly(AssemblyFinder ab, int[] interfaceClusterIds) {
		
		boolean[] engagedSet = new boolean[ab.getNumInterfClusters()];
		
		for (int clusterId:interfaceClusterIds) {
			engagedSet[clusterId-1] = true;
		}
		
		Assembly a = new Assembly(ab.getInterfaces(),ab.getLatticeGraph().getGraph(),engagedSet,ab.getNumEntities());
		
		return a;
	}
}
