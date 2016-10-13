package eppic.assembly;

import static org.junit.Assert.*;

import java.io.IOException;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.junit.Test;

public class TestContractedAssemblyEnumeration {

	@Test
	public void test4nwp() throws IOException, StructureException {
		// 2 entities and a few assemblies, largest assembly is tetrahedral
		
//		Structure s = TestLatticeGraph.getStructure("4nwp");
//		
//		StructureInterfaceList interfaces = TestLatticeGraph.getAllInterfaces(s);
//		
//		CrystalAssemblies crystalAssemblies = new CrystalAssemblies(s, interfaces, false);
//		
//		CrystalAssemblies crystalAssembliesC = new CrystalAssemblies(s, interfaces, true);
//		
//		
//		System.out.printf("%d assemblies found using full graph: \n", crystalAssemblies.size());
//		
//		for (Assembly a : crystalAssemblies) {
//			System.out.println("assembly "+a.toString());
//		}
//
//		System.out.printf("%d assemblies found using contracted graph: \n", crystalAssembliesC.size());
//		
//		for (Assembly a : crystalAssembliesC) {
//			System.out.println("assembly "+a.toString());
//			
//		}
		
		
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

}
