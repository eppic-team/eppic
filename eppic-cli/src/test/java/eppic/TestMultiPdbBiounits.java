package eppic;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eppic.model.AssemblyDB;
import eppic.model.AssemblyScoreDB;
import eppic.model.PdbInfoDB;

public class TestMultiPdbBiounits {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * 3gzh has multiple non-redundant pdb biounit annotations.
	 * See https://github.com/eppic-team/eppic/issues/139
	 * @throws IOException
	 */
	@Test
	public void test3gzh() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestLargeStructures");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
		
		String[] args = {"-i", "3gzh", "-o", outDir.toString()};
		
		Main m = new Main();
		
		m.run(args);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		// assembly 6 is a tetramer and has a PDB biounit annotation (pdb1)
		AssemblyDB assembly6 = pdbInfo.getAssemblies().get(6);		
		assertEquals(4, assembly6.getAssemblyContents().get(0).getMmSize());
		
		List<AssemblyScoreDB> pdbAssemblyScores = getPdbBiounitAnnotations(assembly6.getAssemblyScores());
		
		assertEquals(1, pdbAssemblyScores.size());

		AssemblyScoreDB pdbBiounit1 = getPdbBiounit(pdbAssemblyScores, 1);		
		assertNotNull(pdbBiounit1);
		AssemblyScoreDB pdbBiounit2 = getPdbBiounit(pdbAssemblyScores, 2);
		assertNull(pdbBiounit2);
		
		// assembly 7 is a tetramer and has a PDB biounit annotation (pdb2)
		AssemblyDB assembly7 = pdbInfo.getAssemblies().get(7);
		assertEquals(4, assembly7.getAssemblyContents().get(0).getMmSize());
		
		pdbAssemblyScores = getPdbBiounitAnnotations(assembly7.getAssemblyScores());
		
		assertEquals(1, pdbAssemblyScores.size());
		
		pdbBiounit1 = getPdbBiounit(pdbAssemblyScores, 1);
		assertNull(pdbBiounit1);
		pdbBiounit2 = getPdbBiounit(pdbAssemblyScores, 2);		
		assertNotNull(pdbBiounit2);
		
		
		outDir.delete();
		
	}
	
	private List<AssemblyScoreDB> getPdbBiounitAnnotations(List<AssemblyScoreDB> assemblyScores) {
		List<AssemblyScoreDB> pdbAssemblies = new ArrayList<>();
		for (AssemblyScoreDB assemblyScore : assemblyScores) {
			if (assemblyScore.getMethod().startsWith(DataModelAdaptor.PDB_BIOUNIT_METHOD_PREFIX) && assemblyScore.getCallName().equals("bio")) { 
				pdbAssemblies.add(assemblyScore);
			}		
		}
		return pdbAssemblies;
	}
	
	private AssemblyScoreDB getPdbBiounit(List<AssemblyScoreDB> pdbAssemblyScores, int pdbBiounitNumber) {
		for (AssemblyScoreDB pdbAssemblyScore : pdbAssemblyScores) {
			if (pdbAssemblyScore.getMethod().equals(DataModelAdaptor.PDB_BIOUNIT_METHOD_PREFIX+pdbBiounitNumber) && pdbAssemblyScore.getCallName().equals("bio")) 
				return pdbAssemblyScore;
		}
		
		return null;		
	}

}
