package eppic;

import eppic.commons.sequence.UnirefEntry;
import eppic.commons.util.Interval;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class TestPdbToUniProtMapper {

    @Test
    public void test1smtMapping() throws IOException, StructureException, CompoundNotFoundException {

        // SIFTS record for 1smtA:
        // 1smt    A       P30340  1       122     None    None    1       122

        AtomCache cache = new AtomCache();
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(true);
        cache.setFileParsingParams(params);

        Structure s =  StructureIO.getStructure("1smt");
        PdbToUniProtMapper pdbToUniProtMapper = new PdbToUniProtMapper(s.getEntityInfos().get(0));
        UnirefEntry ref = new UnirefEntry();
        ref.setId("P30340");
        ref.setSequence(
                "MTKPVLQDGETVVCQGTHAAIASELQAIAPEVAQSLAEFFAVLADPNRLRLLSLLARSEL" +
                "CVGDLAQAIGVSESAVSHQLRSLRNLRLVSYRKQGRHVYYQLQDHHIVALYQNALDHLQE" +
                "CR");
        ref.setUniprotId("P30340");
        ref.setNcbiTaxId(1140);
        pdbToUniProtMapper.setUniProtReference(ref);

        Interval intervalPdb = pdbToUniProtMapper.getMatchingIntervalPdbCoords();
        Interval intervalUniProt = pdbToUniProtMapper.getMatchingIntervalUniProtCoords();

        // we should get the observed subset of chain A (note both PDB and UniProt reference sequences are identical in this case)
        assertEquals(24, intervalPdb.beg);
        assertEquals(24, intervalUniProt.beg);
        assertEquals(121, intervalPdb.end);
        assertEquals(121, intervalUniProt.end);
    }
}
