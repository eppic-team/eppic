package eppic.assembly;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eppic.FullAnalysis;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.junit.Test;

import eppic.EppicParams;
import eppic.Main;
import eppic.Utils;
import eppic.model.db.AssemblyContentDB;
import eppic.model.db.AssemblyDB;
import eppic.model.db.AssemblyScoreDB;
import eppic.model.db.PdbInfoDB;



public class TestContractedAssemblyEnumeration {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");

	
	@Test
	public void test4nwp() throws IOException, StructureException {
		// 2 entities and a few assemblies, largest assembly is tetrahedral
		
		Structure s = TestLatticeGraph.getStructure("4nwp");
		
		StructureInterfaceList interfaces = TestLatticeGraph.getAllInterfaces(s);
		
		CrystalAssemblies crystalAssemblies = new CrystalAssemblies(s, interfaces, false);
		
		CrystalAssemblies crystalAssembliesC = new CrystalAssemblies(s, interfaces, true);
		
		
		System.out.printf("%d assemblies found using full graph: \n", crystalAssemblies.size());
		
		boolean gotTetrahedralAssembly = false;
		
		for (Assembly a : crystalAssemblies) {
			
			System.out.println("assembly "+a.toString() + " -- " + getDescription(a));
			
			if (a.toString().equals("{1,2,3,8}")) {
				gotTetrahedralAssembly = true;
				assertEquals("T", a.getDescription().iterator().next().getSymmetry());
			}
		}
		
		assertTrue("A tetrahedral assembly should have been present", gotTetrahedralAssembly);

		System.out.printf("%d assemblies found using contracted graph: \n", crystalAssembliesC.size());
		
		// there's a tetrahedral assembly that we should get
		gotTetrahedralAssembly = false;
		
		for (Assembly a : crystalAssembliesC.getUniqueAssemblies()) {
			
			System.out.println("assembly "+a.toString() + " -- " + getDescription(a));
			
			// in the assembly reconstruction from the contracted graph to the full graph (see CrystalAssemblies.convertToFullGraph() ), 
			// we lose interface 8 (an induced interface). That's why the assembly here is 1,2,3
			if (a.toString().equals("{1,2,3}")) {
				gotTetrahedralAssembly = true;
				assertEquals("T", a.getDescription().iterator().next().getSymmetry());
			}
			
		}
		
		assertTrue("A tetrahedral assembly should have been present", gotTetrahedralAssembly);
		
		
	}
	

	@Test
	public void test4hnw() throws IOException, StructureException {
		// a simple case with 2 entities and few assemblies (largest is a C2)

		
		Structure s = TestLatticeGraph.getStructure("4hnw");
		
		StructureInterfaceList interfaces = TestLatticeGraph.getAllInterfaces(s);
		
		CrystalAssemblies crystalAssemblies = new CrystalAssemblies(s, interfaces, false);
		
		CrystalAssemblies crystalAssembliesC = new CrystalAssemblies(s, interfaces, true);
		
		
		System.out.printf("%d assemblies found using full graph: \n", crystalAssemblies.size());
		
		for (Assembly a : crystalAssemblies) {
			System.out.println("assembly "+a.toString());
		}

		assertEquals(8, crystalAssemblies.getAllAssemblies().size());
		
		
		System.out.printf("%d assemblies found using contracted graph: \n", crystalAssembliesC.size());
		
		for (Assembly a : crystalAssembliesC) {
			System.out.println("assembly "+a.toString());
			
		}
		
		// we've got 2 assemblies plus 1 (the trivial assembly added after the contracted enumeration)
		assertEquals(3, crystalAssembliesC.getAllAssemblies().size());
		assertEquals(3, crystalAssembliesC.getUniqueAssemblies().size());
		
		assertEquals("C1", crystalAssembliesC.getUniqueAssemblies().get(1).getDescription().iterator().next().getSymmetry());
		assertEquals("C2", crystalAssembliesC.getUniqueAssemblies().get(2).getDescription().iterator().next().getSymmetry());		
		

		
	}
	
	private static String getDescription(Assembly a) {
		
		StringBuilder sb = new StringBuilder();
		int i = -1;
		for (List<SubAssembly> sas : a.getAssemblyGraph().getSubAssembliesGroupedByStoichiometries()) {
			SubAssembly sa = sas.get(0);
			i++;
			sb.append(sa.getStoichiometry()+"/"+sa.getSymmetry());
			if (i!=a.getAssemblyGraph().getSubAssemblies().size()-1) sb.append(",");
		}
		return sb.toString();
	}
	
	@Test
	public void testIssue148() {
		File outDir = new File(TMPDIR, "eppicTestContractedAssemblyEnumeration");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
		String pdbId = "4hnw";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);
		params.setForceContractedAssemblyEnumeration(true);
		
		Main m = new Main();
		
		FullAnalysis fullAnalysis = m.run(params);
				
		PdbInfoDB pdbInfo = fullAnalysis.getDataModelAdaptor().getPdbInfo();
		
		
		for (AssemblyDB a : pdbInfo.getValidAssemblies()) {
			System.out.print("Assembly "+a.getId()+ ", " + a.getInterfaceClusterIds() + ": ");
			AssemblyContentDB ac = null;
			if (a.getAssemblyContents()!=null) ac = a.getAssemblyContents().get(0);
			System.out.println(" sto " + (ac==null?"null":ac.getStoichiometry()) + ", sym " + (ac==null?"null":ac.getSymmetry()));
		}

		
		// there should be 3 assemblies: {} (trivial), {1} and {1,3} (in full graph notation, not in contracted graph notation)
		assertEquals(3, pdbInfo.getValidAssemblies().size());

		// first assembly: trivial assembly {} (in full graph notation). 2 separate monomers
		AssemblyDB a1 = pdbInfo.getValidAssemblies().get(0);
		assertEquals(2, a1.getAssemblyContents().size());
		assertEquals("C1", a1.getAssemblyContents().get(0).getSymmetry());
		assertEquals("A", a1.getAssemblyContents().get(0).getStoichiometry());		
		assertEquals("C1", a1.getAssemblyContents().get(1).getSymmetry());
		assertEquals("A", a1.getAssemblyContents().get(1).getStoichiometry());
		
		
		// second assembly: heterodimer {1} (in full graph notation), this is the PDB biounit annotation (pdb1)
		AssemblyDB a2 = pdbInfo.getValidAssemblies().get(1);
		assertEquals(1, a2.getAssemblyContents().size());
		assertEquals("C1", a2.getAssemblyContents().get(0).getSymmetry());
		assertEquals("A B", a2.getAssemblyContents().get(0).getStoichiometry());		
		
		
		// third assembly: C2 heterotetramer {1,3} (in full graph notation}
		AssemblyDB a3 = pdbInfo.getValidAssemblies().get(2);
		assertEquals(1, a3.getAssemblyContents().size());
		assertEquals("C2", a3.getAssemblyContents().get(0).getSymmetry());
		assertEquals("A(2) B(2)", a3.getAssemblyContents().get(0).getStoichiometry());		

		// let's see if we mapped the pdb1 biounit correctly
		boolean foundPdb1Annotation = false;
		for (AssemblyScoreDB sc : a2.getAssemblyScores()) {
			if (sc.getMethod().equals("pdb1")) {
				foundPdb1Annotation = true;
			}
		}
		assertTrue(foundPdb1Annotation);

		
		// TODO test that assembly diagram files are correctly produced
		
		outDir.delete();
	}
	
	@Test
	public void testIssue148_3() throws IOException, StructureException {

		// 5J11: 3 entities, fairly small, 3 chains in AU
		String pdbId = "5J11";
		Structure s = TestLatticeGraph.getStructure(pdbId);
		
		// unfortunately interfaces 3 and 4 are very close in area, so we need to 
		// do the slow area calculation or otherwise the areas differ from the default eppic params
		// and the labels 3 and 4 are swapped, making debugging this very confusing
		StructureInterfaceList interfaces = TestLatticeGraph.getAllInterfaces(s, false);
		
		CrystalAssemblies crystalAssemblies = new CrystalAssemblies(s, interfaces, false);
		
		CrystalAssemblies crystalAssembliesC = new CrystalAssemblies(s, interfaces, true);
		
		
		System.out.printf("%d assemblies found using full graph: \n", crystalAssemblies.size());
		
		for (Assembly a : crystalAssemblies) {
			System.out.println("assembly "+a.toString());
		}

		//assertEquals(11, crystalAssemblies.getUniqueAssemblies().size());
		
		
		System.out.printf("%d assemblies found using contracted graph: \n", crystalAssembliesC.size());
		
		// in this case edges 1, 2 are contracted
		// at the minimum we need {}, {1,2,3}
		boolean gotTrimerAssembly = false;
		for (Assembly a : crystalAssembliesC) {
			System.out.println("assembly "+a.toString());
			
			if (a.toString().equals("{1,2,3}")) {
				gotTrimerAssembly = true;
			}
		}
		
		assertTrue(gotTrimerAssembly);
				
		
	}
	
	/**
	 * See issue #197. 2ian wasn't enumerated correctly with contraction 
	 * because some non-isomorphic heteromeric edges were chosen for contraction.
	 */
	@Test
	public void test2ian() throws Exception {
		Structure s = TestLatticeGraph.getStructure("2ian");
		
		StructureInterfaceList interfaces = TestLatticeGraph.getAllInterfaces(s);
		
		CrystalAssemblies crystalAssembliesC = new CrystalAssemblies(s, interfaces, true);
		
		
		System.out.printf("%d assemblies found using contracted graph: \n", crystalAssembliesC.size());
		
		for (Assembly a : crystalAssembliesC) {
			System.out.println("assembly "+a.toString());
			
		}
		
		// we've got 2 assemblies 
		assertEquals(2, crystalAssembliesC.getAllAssemblies().size());
		assertEquals(2, crystalAssembliesC.getUniqueAssemblies().size());
		
		assertEquals("C1", crystalAssembliesC.getUniqueAssemblies().get(0).getDescription().iterator().next().getSymmetry());
		assertEquals("C1", crystalAssembliesC.getUniqueAssemblies().get(1).getDescription().iterator().next().getSymmetry());	
		
		assertEquals(5, crystalAssembliesC.getUniqueAssemblies().get(1).getDescription().iterator().next().getSize());
		

	}
}
