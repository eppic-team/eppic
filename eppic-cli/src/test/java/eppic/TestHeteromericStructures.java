package eppic;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import eppic.model.db.PdbInfoDB;

/**
 * An integration test to make sure that large heteromeric structures work properly through the whole 
 * pipeline. 
 * 
 * @author Jose Duarte
 *
 */
public class TestHeteromericStructures {
	
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * 3ulu is a 7 entity structure for which the assembly enumeration needs to go through 
	 * the contracted graph (or otherwise there's too many assemblies).
	 * See https://github.com/eppic-team/eppic/issues/148
	 * @throws IOException
	 */
	@Test
	public void test3ulu() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestHeteromericStructures");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
				
		String pdbId = "3ulu";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);
		
		Main m = new Main();
		
		// at the moment this fails with an ArrayOutOfBoundsException (see #148)
		m.run(params);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		System.out.println("Number of assemblies: " + pdbInfo.getAssemblies().size());
		
		
		outDir.delete();
		
	}

}
