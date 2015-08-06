package eppic.assembly;

import static org.junit.Assert.*;

import java.io.IOException;

import org.biojava.nbio.structure.StructureException;
import org.junit.Test;

/**
 * Test for symmetry detection issues.
 * 
 * See https://github.com/eppic-team/eppic/issues/55 :the problem is that 
 * symmetry detection can't be done with counting multiplicity of edges only,
 * we need to find the size of the cycles
 * 
 * Also testing graph contraction and detection for heteromers 
 * 
 * @author duarte_j
 *
 */
public class TestSymmetryDetection {

	// commented out because it is too slow, uncomment to also test this structure
	//@Test
	public void test3hbxD3Assembly() throws IOException, StructureException { 

		// 3hbx {1,2,4} is D3
		// e.g. 3hbx assembly {1,2,4} is a D3, but interface 4 is multiplicity 6

		
		CrystalAssemblies assemblies = TestLatticeGraph.getCrystalAssemblies("3hbx");
		
		for (Assembly a:assemblies) {
			
			if (a.toString().equals("{1,2,4}")) {
				assertEquals("D3",a.getDescription().get(0).getSymmetry());
			}
		}
		
	}
	
	@Test
	public void test1s3fD3Assembly() throws IOException, StructureException { 
		
		CrystalAssemblies assemblies = TestLatticeGraph.getCrystalAssemblies("1s3f");
		
		for (Assembly a:assemblies) {
			
			if (a.toString().equals("{1,2,4,5}")) {
				assertEquals("D3",a.getDescription().get(0).getSymmetry());
			}
		}
		
	}
	
	@Test
	public void test1j1jD4Assembly() throws IOException, StructureException {

		CrystalAssemblies assemblies = TestLatticeGraph.getCrystalAssemblies("1j1j");

		for (Assembly a:assemblies) {

			if (a.toString().equals("{1,2}")) {
				assertEquals("D4",a.getDescription().get(0).getSymmetry());
			}
		}
	}

	@Test
	public void test4hi5C4Assembly() throws IOException, StructureException {

		// 4hi5, assembly {1,3}: a C4 assembly with cross-interfaces
		
		CrystalAssemblies assemblies = TestLatticeGraph.getCrystalAssemblies("4hi5");

		for (Assembly a:assemblies) {

			if (a.toString().equals("{1,3}")) {
				assertEquals("C4",a.getDescription().get(0).getSymmetry());
			}
		}
	}
	
	@Test
	public void test3r93D4HeteromericAssembly() throws IOException, StructureException {

		// 3r93, heteromer with one entity a peptide: interfaces are tangled, graph contraction is needed
		
		CrystalAssemblies assemblies = TestLatticeGraph.getCrystalAssemblies("3r93");

		for (Assembly a:assemblies) {

			if (a.toString().equals("{1,2,3}")) {
				assertEquals("D4",a.getDescription().get(0).getSymmetry());
			}
		}
	}
}
