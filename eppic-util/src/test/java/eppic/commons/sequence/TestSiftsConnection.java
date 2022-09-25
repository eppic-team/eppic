package eppic.commons.sequence;

import eppic.commons.util.Interval;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TestSiftsConnection {

    @Test
    public void testSifts() throws IOException, NoMatchFoundException {
        SiftsConnection siftsConnection = new SiftsConnection();

        Reader reader = new StringReader(
                "# 2022/02/20 - 14:48 | PDB: 07.22 | UniProt: 2022.01\n" +
                "PDB,CHAIN,SP_PRIMARY,RES_BEG,RES_END,PDB_BEG,PDB_END,SP_BEG,SP_END\n" +
                "3sn6,A,P04896,1,380,None,394,1,394\n" +
                "3sn6,B,P54311,13,351,2,340,2,340\n" +
                "3sn6,G,P63212,1,68,None,None,1,68\n" +
                "3sn6,R,P00720,16,175,1002,None,2,161\n" +
                "3sn6,R,P07550,178,511,None,None,29,362\n" +
                "3sn6,R,P07550,512,514,None,None,363,365\n" +
                "3snk,A,Q989D4,2,135,None,None,1,134\n");

        siftsConnection.parsePdb2Uniprot(reader);

        SiftsFeature feature = siftsConnection.getMappings("3sn6", "A");

        assertEquals(1, feature.getCifIntervalSet().size());
        assertEquals(1, feature.getUniprotIntervalSet().size());

        assertEquals(394, feature.getUniProtLengthCoverage("P04896"));
        assertEquals(380, feature.getPdbLengthCoverage());

        feature = siftsConnection.getMappings("3sn6", "R");

        assertEquals(3, feature.getCifIntervalSet().size());
        assertEquals(3, feature.getUniprotIntervalSet().size());

        assertEquals((175-16)+ 1 + (511-178)+1 + (514-512)+1, feature.getPdbLengthCoverage());
        assertEquals((161-2)+ 1, feature.getUniProtLengthCoverage("P00720"));
        assertEquals((362-29)+1 + (365-363)+1, feature.getUniProtLengthCoverage("P07550"));

        assertTrue(feature.isChimeric());

        assertEquals("P07550", feature.getLargestTotalCoverageUniProtId());

        assertEquals(1, feature.getLargestSegmentCoverageIndex());

        assertFalse(feature.hasNegativeLength());
        assertFalse(feature.hasNegatives());

        assertEquals(5, siftsConnection.getAllMappings().size());

        Map<String, List<Interval>> uniMappings = siftsConnection.getUniqueMappings();

        assertEquals(6, uniMappings.size());

        assertEquals(1, uniMappings.get("P54311").size());

        assertEquals(2, uniMappings.get("P07550").size());

        assertEquals(new Interval(29, 362), uniMappings.get("P07550").get(0));
        assertEquals(new Interval(363, 365), uniMappings.get("P07550").get(1));


    }
}
