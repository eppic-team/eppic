package eppic.commons.util;

import eppic.commons.blast.BlastException;
import eppic.commons.blast.MmseqsRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.*;

public class TestMmseqs {

    private static final String[] MMSEQS_LOCATIONS = {"/usr/local/bin/mmseqs", "/usr/bin/mmseqs", "/opt/mmseqs2/bin/mmseqs"};

    private static File mmseqsBin;

    @Before
    public void before() {
        for (String loc : MMSEQS_LOCATIONS) {
            File f = new File(loc);
            if (f.exists()) mmseqsBin = f;
        }
        if (mmseqsBin==null) {
            System.err.println("Could not find mmseqs executable in one of the normal locations");
        }
    }

    @Ignore
    @Test
    public void testClustering() throws Exception {
        String[] seqs = {
                "MTKPVLQDGETVVCQGTHAA",
                "MTKPVLQDGETVVCQGTHAA",
                "MTKPVLQDGETVVCQGTHAA",
                "MTKPVLQDGESVVCQGTHAA" // 1 mismatch out of 20 (5%)

        };

        // cluster at 100%, should give 1
        List<List<String>> clusters = clusterIt(seqs, 100, 0.9);
        assertEquals(2, clusters.size());

        // cluster at 94%, should give 1
        clusters = clusterIt(seqs, 94, 0.9);
        assertEquals(1, clusters.size());

        // cluster at 96%, should give 2
        clusters = clusterIt(seqs, 96, 0.9);
        assertEquals(2, clusters.size());
    }

    private List<List<String>> clusterIt(String[] seqs, int id, double cov) throws IOException, BlastException, InterruptedException {
        File inFile = File.createTempFile("test_mmseqs", ".fasta");
        writeFastaFile(inFile, seqs);

        assertNotNull(mmseqsBin);

        assertTrue(mmseqsBin.exists());

        File outFilePrefix = File.createTempFile("test_mmseqs", "");
        outFilePrefix.delete();

        List<List<String>> clusters = MmseqsRunner.runMmseqsEasyCluster(mmseqsBin, inFile, outFilePrefix, id, cov, 1);

        inFile.delete();

        return clusters;
    }

    private void writeFastaFile(File f, String[] seqs) throws IOException {
        PrintWriter pw = new PrintWriter(f);
        for (int i = 0;i<seqs.length; i++) {
            pw.println(">"+i);
            pw.println(seqs[i]);
        }
        pw.close();
    }
}
