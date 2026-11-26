package eppic;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eppic.db.mongoutils.ConfigurableMapper;
import eppic.model.db.AtomPairDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.InterfaceWarningDB;
import eppic.model.db.InterfaceWarningType;
import eppic.model.db.PdbInfoDB;

/**
 * Tests for the structured data in interface warnings
 */
public class TestInterfaceWarnings {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");

	/**
	 * Insulin: its chains are peptides, and the inter-chain disulfide bridges show up as
	 * close interactions in the geometry-only run.
	 */
	@Test
	public void test4ins() throws Exception {

		File outDir = new File(TMPDIR, "eppicTestInterfaceWarnings");
		outDir.mkdir();
		assertTrue(outDir.isDirectory());

		EppicParams params = Utils.generateEppicParams("4ins", outDir);
		// warnings are only added to the model when the model is the output
		params.setGenerateModelSerializedFile(true);

		Main m = new Main();
		m.run(params, false);

		PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();

		List<InterfaceWarningDB> warnings = new ArrayList<>();
		for (InterfaceClusterDB ic : pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf : ic.getInterfaces()) {
				for (InterfaceWarningDB w : interf.getInterfaceWarnings()) {
					assertSame(interf, w.getInterfaceItem());
					warnings.add(w);
				}
			}
		}

		assertFalse(warnings.isEmpty());

		boolean foundPeptide = false;
		boolean foundDisulfideCloseInteraction = false;

		for (InterfaceWarningDB w : warnings) {
			// every warning produced now must be typed and still have the text
			assertNotNull(w.getText());
			assertNotNull(w.getType());

			if (w.getType() == InterfaceWarningType.PEPTIDE) {
				foundPeptide = true;
				assertNotNull(w.getMolecId());
				assertTrue(w.getMolecId() == 1 || w.getMolecId() == 2);
			}

			if (w.getType() == InterfaceWarningType.CLOSE_INTERACTIONS || w.getType() == InterfaceWarningType.CLASHES) {
				assertNotNull(w.getCount());
				assertNotNull(w.getAtomPairs());
				assertFalse(w.getAtomPairs().isEmpty());
				assertTrue(w.getAtomPairs().size() <= w.getCount());
				for (AtomPairDB pair : w.getAtomPairs()) {
					// residues must be on the sides of the interface they belong to
					assertEquals(w.getInterfaceItem().getChain1(), pair.getFirstResidue().getChainId());
					assertEquals(w.getInterfaceItem().getChain2(), pair.getSecondResidue().getChainId());
					assertNotNull(pair.getFirstResidue().getPdbResidueNumber());
					assertNotNull(pair.getFirstResidue().getResidueNumber());
					assertTrue(pair.getDistance() > 0);
					if ("SG".equals(pair.getFirstAtom()) && "SG".equals(pair.getSecondAtom())) {
						foundDisulfideCloseInteraction = true;
						assertEquals("CYS", pair.getFirstResidue().getResidueType());
						assertEquals("CYS", pair.getSecondResidue().getResidueType());
					}
				}
			}
		}

		assertTrue(foundPeptide);
		assertTrue(foundDisulfideCloseInteraction);

		// and it serializes to json with the structured data
		String json = ConfigurableMapper.getMapper().writeValueAsString(warnings.get(0));
		System.out.println(json);
		assertTrue(json.contains("\"type\""));

		File[] files = outDir.listFiles();
		if (files != null) {
			for (File f : files) f.delete();
		}
		outDir.delete();
	}
}
