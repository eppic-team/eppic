package eppic.assembly;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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

import eppic.EppicParams;

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
		CrystalAssemblies ab = getCrystalAssemblies("1smt");
		
		// cluster 1: isologous between A+B
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		
		// cluster 3: classic infinite A+A on screw axis
		a = ab.generateAssembly(3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection1r1z() throws IOException, StructureException {

		// 1r1z (P 1 with 1 entity and 4 molecules A,B,C,D)
		// the crystal contains a "hidden" cycle, see issue https://github.com/eppic-team/eppic-science/issues/45
		CrystalAssemblies ab = getCrystalAssemblies("1r1z");
		
		// cluster 1: a C4 cycle
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection1ble() throws IOException, StructureException {

		// 1ble (P 43 3 2 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations
		CrystalAssemblies ab = getCrystalAssemblies("1ble");
		
		// cluster 1: a C3 cycle
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		
		// cluster 3: isologous
		a = ab.generateAssembly(3);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection4mml() throws IOException, StructureException {

		// 4mml (P 6 with 1 entity and 1 molecule A)
		CrystalAssemblies ab = getCrystalAssemblies("4mml");
		
		// cluster 1: a C6 cycle
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		
		// cluster 2: isologous
		a = ab.generateAssembly(2);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());

		// cluster 3: classic infinite
		a = ab.generateAssembly(3);		
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

		// cluster 4: classic infinite
		a = ab.generateAssembly(4);		
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

		// clusters 1+2: an open cycle
		a = ab.generateAssembly(new int[]{1,2});		
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

	}
	
	@Test
	public void testCycleDetection4jf3() throws IOException, StructureException {

		// 4jf3 (P 3 with 1 entity and 2 molecules A,B): 
		// A,B are related by a pseudo 2-fold screw (actually the lattice is really a P 63 lattice)
		CrystalAssemblies ab = getCrystalAssemblies("4jf3");
		
		// cluster 1: a C3 cycle
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: a pseudo 3-fold screw
		a = ab.generateAssembly(2);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());		
		
		// cluster 3: pseudo 2-fold screw
		a = ab.generateAssembly(3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());
		
	}
	
	@Test
	public void testCycleDetection3vkx() throws IOException, StructureException {

		// 3vkx (I 21 3 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations  
		CrystalAssemblies ab = getCrystalAssemblies("3vkx");
		
		// cluster 1: a C3 cycle
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: isologous
		a = ab.generateAssembly(2);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());		
		
		// cluster 3: isologous
		a = ab.generateAssembly(3);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());

		// cluster 4: 3-fold screw, classic infinite
		a = ab.generateAssembly(4);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

	}
	
	@Test
	public void testCycleDetection3riq() throws IOException, StructureException {

		// 3riq (I 21 3 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations  
		CrystalAssemblies ab = getCrystalAssemblies("3riq");
		
		// cluster 1: a C3 cycle
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: isologous
		a = ab.generateAssembly(2);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());		
		
		// cluster 3: open cycle, classic infinite
		a = ab.generateAssembly(3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());

		// cluster 4: isologous
		a = ab.generateAssembly(4);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 5: pure fractional translation, classic infinite
		a = ab.generateAssembly(5);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());
		

	}

	@Test
	public void testCycleDetection4b29() throws IOException, StructureException {

		// 4b29 (I 21 3 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations  
		CrystalAssemblies ab = getCrystalAssemblies("4b29");
		
		// cluster 1: isologous
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: closed C3 cycle
		a = ab.generateAssembly(2);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());		
		
		// cluster 3: open C3 cycle (classic infinite)
		a = ab.generateAssembly(3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());		

	}
	
	@Test
	public void testCycleDetection4nla() throws IOException, StructureException {

		// 4nla (I 2 3 with 1 entity and 1 molecule A): high symmetry with lots of in-cell translations  
		CrystalAssemblies ab = getCrystalAssemblies("4nla");
		
		// cluster 1: a C3 cycle
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: open 3 cycle
		a = ab.generateAssembly(2);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());		

		// cluster 3: isologous
		a = ab.generateAssembly(3);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());

		// cluster 4: isologous
		a = ab.generateAssembly(4);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 5: closed C3 cycle
		a = ab.generateAssembly(5);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		

	}
	
	@Test
	public void testCycleDetection3iue() throws IOException, StructureException {

		// 3iue (P 1 21 1 with 1 entity and 2 molecules A,B)
		// see issue https://jira-bsse.ethz.ch/browse/CRK-121
		CrystalAssemblies ab = getCrystalAssemblies("3iue");
		
		// cluster 1: isologous through NCS
		Assembly a = ab.generateAssembly(1);		
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());
		
		// cluster 2: isologous
		a = ab.generateAssembly(2);
		assertTrue(a.isValid());
		assertTrue(a.isClosedSymmetry());		
		
		// cluster 3: classic infinite
		a = ab.generateAssembly(3);
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());
		
		// clusters 1+2: open cycle
		a = ab.generateAssembly(new int[]{1,2});
		assertFalse(a.isValid());
		assertFalse(a.isClosedSymmetry());
		

	}
	
	@Test
	public void testIsomorphicDetection1a99() throws IOException, StructureException {

		// 1a99 
		// see issue https://github.com/eppic-team/eppic-science/issues/14
		CrystalAssemblies ab = getCrystalAssemblies("1a99");
		
		// cluster 1: non-isomorphic (even if isologous!)
		Assembly a = ab.generateAssembly(1);
		assertTrue(a.isClosedSymmetry());
		assertFalse(a.isValid());
		assertFalse(a.isIsomorphic());
		
		// cluster 2: non-isomorphic
		a = ab.generateAssembly(2);
		assertFalse(a.isValid());
		assertFalse(a.isIsomorphic());		
		
		// cluster 3: non-isomorphic
		a = ab.generateAssembly(3);
		assertFalse(a.isValid());
		assertFalse(a.isIsomorphic());
		
		// cluster 4: non-isomorphic
		a = ab.generateAssembly(4);
		assertFalse(a.isValid());
		assertFalse(a.isIsomorphic());
		

	}
	
	@Test
	public void testCellMmcifGeneration() throws IOException, StructureException {
		
		String pdbId = "2trx";
				
		logger.info("Calculating interfaces for "+pdbId);
		
		AtomCache cache = new AtomCache();
		FileParsingParameters params = new FileParsingParameters();
		params.setAlignSeqRes(true); 
		cache.setFileParsingParams(params);
		StructureIO.setAtomCache(cache);
		
		Structure s =  StructureIO.getStructure(pdbId);
		CrystalBuilder cb = new CrystalBuilder(s);
		StructureInterfaceList interfaces = cb.getUniqueInterfaces();
		interfaces.calcAsas();
		interfaces.removeInterfacesBelowArea();
		interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		
		LatticeGraph3D lg3d = new LatticeGraph3D(s);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		PrintWriter out = new PrintWriter(os);
		lg3d.writeCellToMmCifFile(out);
		out.close();
		
		List<String> symmetryFields = new ArrayList<>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray())));
		String line;
		while ( (line = br.readLine())!=null) {
			if (line.startsWith("_symmetry")) {
				System.out.println(line);
				String[] tokens = line.split("\\s+");
				symmetryFields.add(tokens[0]);				
			}
		}
		br.close();
		
		// here we test that the space group field has the right name (with a hyphen between H and M), see https://github.com/biojava/biojava/issues/480
		
		//_symmetry.entry_id                         2TRX 
		//_symmetry.space_group_name_H-M             'C 1 2 1' 
		//_symmetry.pdbx_full_space_group_name_H-M   ? 
		//_symmetry.cell_setting                     ? 
		//_symmetry.Int_Tables_number                ? 

		assertTrue(symmetryFields.contains("_symmetry.entry_id"));
		assertTrue(symmetryFields.contains("_symmetry.cell_setting"));
		assertTrue(symmetryFields.contains("_symmetry.Int_Tables_number"));
		// these are the problematic fields
		assertTrue(symmetryFields.contains("_symmetry.space_group_name_H-M"));
		assertTrue(symmetryFields.contains("_symmetry.pdbx_full_space_group_name_H-M"));
		
	}
	

	/**
	 * Utility to facilitate testing of lattice graph related stuff: gets the CrystalAssemblies object for a given PDB id
	 * @param pdbId
	 * @return
	 * @throws IOException
	 * @throws StructureException
	 */
	public static CrystalAssemblies getCrystalAssemblies(String pdbId) throws IOException, StructureException {
		
		logger.info("Calculating interfaces for "+pdbId);
		
		AtomCache cache = new AtomCache();
		FileParsingParameters params = new FileParsingParameters();
		params.setAlignSeqRes(true); 
		cache.setFileParsingParams(params);
		StructureIO.setAtomCache(cache);
		
		Structure s =  StructureIO.getStructure(pdbId);
		
		CrystalBuilder cb = new CrystalBuilder(s);
		StructureInterfaceList interfaces = cb.getUniqueInterfaces();
		interfaces.calcAsas();
		interfaces.removeInterfacesBelowArea();
		interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		
		CrystalAssemblies crystalAssemblies = new CrystalAssemblies(s, interfaces);
		
		return crystalAssemblies; 
	}
	
	
	
}
