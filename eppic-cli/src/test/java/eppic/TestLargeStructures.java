package eppic;

//import org.junit.Ignore;
import org.junit.Test;

import eppic.model.ChainClusterDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

/**
 * An integration test that makes sure that large structures are correctly handled
 * through an entire run of eppic.
 * 
 * See https://github.com/eppic-team/eppic/issues/23
 * 
 * @author Jose Duarte
 *
 */
public class TestLargeStructures {
	
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * 4v9e has 1 protein entity (36 chains) and 1 nucleic acid entity (6 chains).
	 * All author chain ids are 2 chars long.
	 * @throws IOException
	 */
	// can be long and memory hungry, ignore if needed
	//@Ignore
	@Test
	public void test4v9e() throws IOException {
		
		File outDir = new File(TMPDIR, "eppicTestLargeStructures");
		
		outDir.mkdir();
		
		assertTrue(outDir.isDirectory());
		
				
		String pdbId = "4v9e";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);
		
		Main m = new Main();
		
		m.run(params);
		
		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();
		
		ChainClusterDB cc = pdbInfo.getChainCluster("AA");
		assertEquals(36, cc.getNumMembers());
		
		String[] chains = cc.getMemberChains().split(",");
		
		assertEquals(36, chains.length);
		
		for (String chain:chains) {
			assertEquals(2, chain.length());
		}
		
		
		for (InterfaceClusterDB icdb : pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB idb : icdb.getInterfaces()) {
				assertEquals(2, idb.getChain1().length());
				assertEquals(2, idb.getChain2().length());
				
				// checking the first residue burial
				assertEquals(2, idb.getResidueBurials().get(0).getResidueInfo().getRepChain().length());
			}
		}
		
		outDir.delete();
		
	}

}
