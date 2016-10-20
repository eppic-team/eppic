package eppic.assembly;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.junit.Test;

public class TestContractedAssemblyEnumeration {

	@Test
	public void test4nwp() throws IOException, StructureException {
		// 2 entities and a few assemblies, largest assembly is tetrahedral
		
		Structure s = TestLatticeGraph.getStructure("4nwp");
		
		StructureInterfaceList interfaces = TestLatticeGraph.getAllInterfaces(s);
		
		//CrystalAssemblies crystalAssemblies = new CrystalAssemblies(s, interfaces, false);
		
		CrystalAssemblies crystalAssembliesC = new CrystalAssemblies(s, interfaces, true);
		
		
		//System.out.printf("%d assemblies found using full graph: \n", crystalAssemblies.size());
		
		boolean gotTetrahedralAssembly = false;
		
		//for (Assembly a : crystalAssemblies) {
		//	
		//	System.out.println("assembly "+a.toString() + " -- " + getDescription(a));
		//	
		//	if (a.toString().equals("{1,2,3,8}")) {
		//		gotTetrahedralAssembly = true;
		//		assertEquals("T", a.getDescription().iterator().next().getSymmetry());
		//	}
		//}
		
		//assertTrue("A tetrahedral assembly should have been present", gotTetrahedralAssembly);

		System.out.printf("%d assemblies found using contracted graph: \n", crystalAssembliesC.size());
		
		// there's a tetrahedral assembly that we should get
		gotTetrahedralAssembly = false;
		
		for (Assembly a : crystalAssembliesC.getUniqueAssemblies()) {
			
			System.out.println("assembly "+a.toString() + " -- " + getDescription(a));
			
			if (a.toString().equals("{1,2}")) {
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
		
		assertEquals(2, crystalAssembliesC.getAllAssemblies().size());
		assertEquals(2, crystalAssembliesC.getUniqueAssemblies().size());
		
		assertEquals("C1", crystalAssembliesC.getUniqueAssemblies().get(0).getDescription().iterator().next().getSymmetry());
		assertEquals("C2", crystalAssembliesC.getUniqueAssemblies().get(1).getDescription().iterator().next().getSymmetry());		
		

		
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

}
