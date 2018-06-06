package eppic;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.junit.Test;

import eppic.model.db.AssemblyDB;
import eppic.model.db.AssemblyScoreDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.PdbInfoDB;
import eppic.model.db.ScoringMethod;

import static org.junit.Assert.*;

import java.io.File;

public class TestInterfaceMatching {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * Test interface matching, specifically issues https://github.com/eppic-team/eppic/issues/98 and https://github.com/eppic-team/eppic/issues/74
	 */
	@Test
	public void test4hwd() throws EppicException {
		
		File outDir = new File(TMPDIR, "eppicTestInterfMatching");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
				
		String pdbId = "4hwd";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);
		
		Main m = new Main();
		
		m.run(params);
		
		Structure s = m.getStructure();
		
		for (Chain c : s.getPolyChains()) {
			System.out.println("Chain id "+c.getName() + ", chain asym id "+c.getId());
		}
		
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		for (AssemblyDB assembly : pdbInfo.getAssemblies()) {
			
			System.out.println("Assembly: "+ assemblyDbToString(assembly) + ", valid: "+assembly.isTopologicallyValid());
			
			assertTrue(assembly.isTopologicallyValid());
			
		}
		
		// there should be only 1 assembly
		assertEquals(1, pdbInfo.getAssemblies().size());
		
		// for the 1 assembly there should be 2 scores, one for our own assembly (eppic) and one for the pdb1 assembly
		AssemblyDB assembly = pdbInfo.getAssemblies().get(0);
		
		int eppicAssemblies = 0;
		int pdb1Assemblies = 0;
		for (AssemblyScoreDB asdb : assembly.getAssemblyScores()) {
			if (asdb.getMethod().equals(ScoringMethod.EPPIC_FINAL)) eppicAssemblies ++;
			if (asdb.getMethod().equals(DataModelAdaptor.PDB_BIOUNIT_METHOD_PREFIX +"1")) pdb1Assemblies ++;
			//System.out.println(asdb.getMethod());
		}
		
		assertEquals(1, eppicAssemblies);
		assertEquals(1, pdb1Assemblies);
		
		
		outDir.delete();
		
	}
	
	private String assemblyDbToString(AssemblyDB assembly) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int i = 0;
		for (InterfaceClusterDB icdb : assembly.getInterfaceClusters()) {
			
			sb.append(icdb.getClusterId());
			if (i<assembly.getInterfaceClusters().size()-1) sb.append(",");
			
			i++;
		}
		sb.append("}");
		return assembly.getId() + "-" + sb.toString();
	}
}
