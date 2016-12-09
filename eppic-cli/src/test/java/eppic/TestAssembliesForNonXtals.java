package eppic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import eppic.model.AssemblyDB;
import eppic.model.PdbInfoDB;

public class TestAssembliesForNonXtals {

private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * 1tlh: NMR heterodimer without space group or cell defined
	 * See https://github.com/eppic-team/eppic/issues/50
	 * @throws IOException
	 */
	@Test
	public void test1tlh() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestAssembliesForNonXtals");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
		
		
		String pdbId = "1tlh";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);

		
		Main m = new Main();
		
		m.run(params);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		// 2 assemblies: the monomeric (2 disjoint monomers) and the heterodimeric
		assertEquals(2, pdbInfo.getAssemblies().size());
		
		AssemblyDB assembly1 = pdbInfo.getAssemblies().get(0);
		
		assertEquals(1, assembly1.getAssemblyContents().get(0).getMmSize());
		assertEquals(1, assembly1.getAssemblyContents().get(1).getMmSize());
		assertEquals("C1", assembly1.getAssemblyContents().get(0).getSymmetry());
		
		
		AssemblyDB assembly2 = pdbInfo.getAssemblies().get(1);
		
		assertEquals(2, assembly2.getAssemblyContents().get(0).getMmSize());
		assertEquals("C1", assembly2.getAssemblyContents().get(0).getSymmetry());
		
		outDir.delete();
		
	}
	
	
	/**
	 * 5a7u: EM monomer
	 * See https://github.com/eppic-team/eppic/issues/50
	 * @throws IOException
	 */
	@Test
	public void test5a7u() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestAssembliesForNonXtals");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
				
		String pdbId = "5a7u";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);

		
		Main m = new Main();
		
		m.run(params);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		// 1 assembly: monomeric
		assertEquals(1, pdbInfo.getAssemblies().size());
		
		AssemblyDB assembly1 = pdbInfo.getAssemblies().get(0);
		
		assertEquals(1, assembly1.getAssemblyContents().get(0).getMmSize());
		assertEquals("C1", assembly1.getAssemblyContents().get(0).getSymmetry());
		
		
		
		outDir.delete();
		
	}
	
	/**
	 * 5h1q: EM octamer
	 * See https://github.com/eppic-team/eppic/issues/50
	 * @throws IOException
	 */
	@Test
	public void test5h1q() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestAssembliesForNonXtals");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
		
		String pdbId = "5h1q";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);

		
		Main m = new Main();
		
		m.run(params);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		// 2 assemblies: monomer and octameric
		assertEquals(2, pdbInfo.getAssemblies().size());
		
		AssemblyDB assembly1 = pdbInfo.getAssemblies().get(0);
		
		assertEquals(1, assembly1.getAssemblyContents().get(0).getMmSize());
		assertEquals("C1", assembly1.getAssemblyContents().get(0).getSymmetry());
		
		AssemblyDB assembly2 = pdbInfo.getAssemblies().get(1);
		
		assertEquals(8, assembly2.getAssemblyContents().get(0).getMmSize());
		assertEquals("C8", assembly2.getAssemblyContents().get(0).getSymmetry());
		
		outDir.delete();
		
	}
}
