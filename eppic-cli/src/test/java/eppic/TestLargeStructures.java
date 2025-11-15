package eppic;

//import org.junit.Ignore;
import eppic.assembly.TestLatticeGraph;
import eppic.model.db.*;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.junit.Test;

import javax.vecmath.Matrix4d;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		
		FullAnalysis fullAnalysis = m.run(params);
		
		PdbInfoDB pdbInfo = fullAnalysis.getDataModelAdaptor().getPdbInfo();

		// the title should be set, this checks that DataModelAdaptor.setPdbMetadata worked
		assertNotNull(pdbInfo.getTitle());
		assertTrue(pdbInfo.getTitle().length()>2);
		
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
				
			}
		}

		// delete all files and then the dir
		File[] files = outDir.listFiles();
		for (File f : files) f.delete();
		outDir.delete();
		
	}

	/**
	 * NCS output needs to be less redundant.
	 * Issue https://github.com/eppic-team/eppic/issues/205
	 * @throws IOException
	 */
	//@Ignore // test is very heavy (it writes all coordinate files which takes half of the time or more), ignore if needed
	@Test
	public void test1auy() throws IOException {

		File outDir = new File(TMPDIR, "eppicTestLargeStructures");

		outDir.mkdir();

		assertTrue(outDir.isDirectory());


		String pdbId = "1auy";
		EppicParams params = Utils.generateEppicParams(pdbId, outDir);

		params.setGenerateOutputCoordFiles(true);

		Main m = new Main();

		FullAnalysis fullAnalysis = m.run(params);

		PdbInfoDB pdbInfo = fullAnalysis.getDataModelAdaptor().getPdbInfo();

		assertTrue(pdbInfo.isNcsOpsPresent());

		assertEquals(1, pdbInfo.getNumChainClusters());
		ChainClusterDB ccdb = pdbInfo.getChainClusters().get(0);
		assertEquals(3, ccdb.getNumMembers());

		assertEquals(10, pdbInfo.getInterfaceClusters().size());

		assertEquals(4, pdbInfo.getValidAssemblies().size());

		// the cluster members should be reduced to NCS equivalents: it should be a low number
		int count = 0;
		for (InterfaceClusterDB interfCluster : pdbInfo.getInterfaceClusters()) {
			assertTrue(interfCluster.size()<10);
			assertTrue(interfCluster.getAvgContactOverlapScore() > 0);
			for (InterfaceDB idb : interfCluster.getInterfaces()) {
				// can't assert this, the n chains are still in some interfaces
				//assertFalse(idb.getChain1().endsWith("n"));
				assertEquals(interfCluster.getClusterId(), idb.getClusterId());
				count++;
			}
		}

		assertTrue(count<20);

		File[] files = outDir.listFiles((d, name) -> (name.endsWith(".cif.gz") && name.contains(".interface.") ));

		assertNotNull(files);

		assertEquals(count, files.length);

		// test for issue #141
		AssemblyDB icoAssembly = pdbInfo.getValidAssemblies().get(3);
		// check that this really is the icosahedral assembly
		assertEquals("I", icoAssembly.getAssemblyContents().get(0).getSymmetry());
		assertEquals(180, icoAssembly.getAssemblyContents().get(0).getMmSize());
		boolean pdb1Annotation = false;
		for (AssemblyScoreDB as : icoAssembly.getAssemblyScores()) {
			if (as.getMethod().equals("pdb1") && as.getCallName().equals("bio")) {
				pdb1Annotation = true;
			}
		}

		assertTrue(pdb1Annotation);

		// delete all files and then the dir
		files = outDir.listFiles();
		for (File f : files) f.delete();

		outDir.delete();

	}

	/**
	 * As an extra test for NCS: some sanity checks that the grouping by NCS and clustering by contact
	 * overlap score are consistent with each other.
	 * @throws Exception
	 */
	@Test
	public void testInterfaceNcsGrouping() throws Exception {
		Structure s = TestLatticeGraph.getStructure("1auy");

		Map<String,String> chainOrigNames = new HashMap<>();
		Map<String, Matrix4d> chainNcsOps = new HashMap<>();
		CrystalBuilder.expandNcsOps(s,chainOrigNames,chainNcsOps);
		CrystalBuilder cb = new CrystalBuilder(s,chainOrigNames,chainNcsOps);

		StructureInterfaceList interfaces = cb.getUniqueInterfaces();
		int spherePoints = StructureInterfaceList.DEFAULT_ASA_SPHERE_POINTS / 10;
		interfaces.calcAsas(spherePoints,
				Runtime.getRuntime().availableProcessors(),
				StructureInterfaceList.DEFAULT_MIN_COFACTOR_SIZE);
		interfaces.removeInterfacesBelowArea();

		List<StructureInterfaceCluster> full = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		List<StructureInterfaceCluster> ncs = interfaces.getClustersNcs();

		int idx = 0;
		for (StructureInterfaceCluster c : ncs) {
			int refId = 0;
			int jdx = 0;
			for (StructureInterface i : c.getMembers()) {
				// it seems that ncs list does not filter for area (bug in biojava 5.0.0), this is a workaround
				if (i.getTotalArea()<StructureInterfaceList.DEFAULT_MINIMUM_INTERFACE_AREA) continue;
				StructureInterfaceCluster correspondingFull = findCorrespondingInterfCluster(i, full);
				assertNotNull(correspondingFull);
				if (jdx==0) refId = correspondingFull.getId();
				assertEquals("Interface "+i.getId()+" from NCS group with index "+idx+" should have same cluster id in full as first in group",
						refId, correspondingFull.getId());
				jdx++;
			}
			idx++;
		}

		// and the other way around
		idx = 0;
		for (StructureInterfaceCluster c : full) {
			int refId = 0;
			int jdx = 0;
			for (StructureInterface i : c.getMembers()) {
				StructureInterfaceCluster correspondingNcs = findCorrespondingInterfCluster(i, ncs);
				assertNotNull(correspondingNcs);
				if (jdx==0) refId = correspondingNcs.getId();
				assertEquals("Interface "+i.getId()+" from full group with index "+idx+" should have same cluster id in NCS as first in group",
						refId, correspondingNcs.getId());
				jdx++;
			}
			idx++;
		}


//		for (StructureInterfaceCluster c : full) {
//			System.out.println("### Cluster "+c.getId());
//			for (StructureInterface i : c.getMembers()) {
//				StructureInterface corresponding = findCorrespondingInterf(i, ncs);
//				System.out.println("id " + i.getId() + ": " + i + " --- "+corresponding);
//			}
//		}
	}

	private StructureInterfaceCluster findCorrespondingInterfCluster(StructureInterface interf, List<StructureInterfaceCluster> clusters) {
		for (StructureInterfaceCluster c : clusters) {
			for (StructureInterface i : c.getMembers()) {
				if (interf.getId() == i.getId()) return c;
			}
		}
		return null;
	}

//	private StructureInterface findCorrespondingInterf(StructureInterface interf, List<StructureInterfaceCluster> clusters) {
//		for (StructureInterfaceCluster c : clusters) {
//			for (StructureInterface i : c.getMembers()) {
//				if (interf.getId() == i.getId()) return i;
//			}
//		}
//		return null;
//	}
}
