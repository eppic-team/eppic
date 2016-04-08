package eppic.assembly;

import static org.junit.Assert.*;

import java.io.IOException;

import org.biojava.nbio.structure.StructureException;
import org.junit.Test;

public class TestContractedAssemblyEnumeration {

	@Test
	public void test4nwp() throws IOException, StructureException {
		// 2 entities and a few assemblies, largest assembly is tetrahedral
		
		CrystalAssemblies crystAssemblies = TestLatticeGraph.getCrystalAssemblies("4nwp", true);
		
		
		
		
		//crystAssemblies.
		
	}
	

	@Test
	public void test4hnw() throws IOException, StructureException {
		// a simple case with 2 entities and few assemblies (largest is a C2)
		
		CrystalAssemblies crystAssemblies = TestLatticeGraph.getCrystalAssemblies("4nwp", true);
		
		
		
		
		//crystAssemblies.
		
	}

}
