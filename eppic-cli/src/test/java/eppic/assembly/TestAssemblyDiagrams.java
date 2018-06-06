package eppic.assembly;

import eppic.EppicParams;
import eppic.Main;
import eppic.Utils;
import eppic.model.db.PdbInfoDB;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TestAssemblyDiagrams {

    private static final String TMPDIR = System.getProperty("java.io.tmpdir");


    /**
     * Testing that assembly diagram json files are properly generated
     */
    @Test
    public void testAssemblyDiagramGeneration() {
        File outDir = new File(TMPDIR, "eppicTestAssemblyDiagrams");

        outDir.mkdir();

        assertTrue(outDir.isDirectory());


        String pdbId = "5cti";
        EppicParams params = Utils.generateEppicParams(pdbId, outDir);
        params.setGenerateDiagrams(true);

        Main m = new Main();

        m.run(params);

        PdbInfoDB pdbInfo = m.getDataModelAdaptor().getPdbInfo();

        File[] files = outDir.listFiles((d, name) -> (name.endsWith(".json") && name.contains(".diagram.") ));
        assertNotNull(files);
        assertEquals(pdbInfo.getAssemblies().size(), files.length);

        files = outDir.listFiles((d, name) -> (name.endsWith(".json") && name.contains(".latticeGraph.") ));
        assertNotNull(files);
        // there's always 1 additional file for the whole unit cell (named with a "*")
        assertEquals(pdbInfo.getAssemblies().size() + 1, files.length);


        // delete all files and then the dir
        files = outDir.listFiles();
        for (File f : files) f.delete();
        outDir.delete();
    }
}
