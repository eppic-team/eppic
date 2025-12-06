package eppic;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eppic.model.db.AssemblyDB;
import eppic.model.db.AssemblyScoreDB;
import eppic.model.db.PdbInfoDB;

public class TestMultiPdbBiounits {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * 3gzh has multiple non-redundant pdb biounit annotations.
	 * See https://github.com/eppic-team/eppic/issues/139
	 * @throws IOException
	 */
	@Test
	public void test3gzh() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestMultiPdbBiounits");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
		String pdbId = "3gzh";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);
		Main m = new Main();
		
		m.run(params, false);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		// assembly 7 is a tetramer and has a PDB biounit annotation (pdb1)
		AssemblyDB assembly7 = pdbInfo.getValidAssemblies().get(6);
		assertEquals(4, assembly7.getAssemblyContents().get(0).getMmSize());
		assertEquals("{1,2,3}", assembly7.getInterfaceClusterIds());
		
		List<AssemblyScoreDB> pdbAssemblyScores = getPdbBiounitAnnotations(assembly7.getAssemblyScores());
		
		assertEquals(1, pdbAssemblyScores.size());

		AssemblyScoreDB pdbBiounit1 = getPdbBiounit(pdbAssemblyScores, 1);		
		assertNotNull(pdbBiounit1);
		AssemblyScoreDB pdbBiounit2 = getPdbBiounit(pdbAssemblyScores, 2);
		assertNull(pdbBiounit2);
		
		// assembly 8 is a tetramer and has a PDB biounit annotation (pdb2)
		AssemblyDB assembly8 = pdbInfo.getValidAssemblies().get(7);
		assertEquals(4, assembly8.getAssemblyContents().get(0).getMmSize());
		assertEquals("{2,4,8}", assembly8.getInterfaceClusterIds());
		
		pdbAssemblyScores = getPdbBiounitAnnotations(assembly8.getAssemblyScores());
		
		assertEquals(1, pdbAssemblyScores.size());
		
		pdbBiounit1 = getPdbBiounit(pdbAssemblyScores, 1);
		assertNull(pdbBiounit1);
		pdbBiounit2 = getPdbBiounit(pdbAssemblyScores, 2);		
		assertNotNull(pdbBiounit2);
		
		
		outDir.delete();
		
	}
	
	/**
	 * 3p3f has multiple redundant pdb biounit annotations, all mapping to 1 eppic assembly
	 * See https://github.com/eppic-team/eppic/issues/139
	 * @throws IOException
	 */
	@Test
	public void test3p3f() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestMultiPdbBiounits");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
				
		String pdbId = "3p3f";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);
		
		Main m = new Main();
		
		m.run(params, false);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		// assembly 2 is a dimer and has several PDB biounits redundant annotation (pdb1, pdb2, pdb3)
		AssemblyDB assembly2 = pdbInfo.getValidAssemblies().get(1);
		assertEquals(2, assembly2.getAssemblyContents().get(0).getMmSize());
		assertEquals("{1}", assembly2.getInterfaceClusterIds());
		
		List<AssemblyScoreDB> pdbAssemblyScores = getPdbBiounitAnnotations(assembly2.getAssemblyScores());
		
		assertEquals(3, pdbAssemblyScores.size());

		AssemblyScoreDB pdbBiounit1 = getPdbBiounit(pdbAssemblyScores, 1);		
		assertNotNull(pdbBiounit1);
		AssemblyScoreDB pdbBiounit2 = getPdbBiounit(pdbAssemblyScores, 2);
		assertNotNull(pdbBiounit2);
		AssemblyScoreDB pdbBiounit3 = getPdbBiounit(pdbAssemblyScores, 3);
		assertNotNull(pdbBiounit3);
		AssemblyScoreDB pdbBiounit4 = getPdbBiounit(pdbAssemblyScores, 4);
		assertNull(pdbBiounit4);

		
		
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
