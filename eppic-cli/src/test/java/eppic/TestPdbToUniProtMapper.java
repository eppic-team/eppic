package eppic;

import eppic.commons.sequence.UnirefEntry;
import eppic.commons.util.Interval;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.mmcif.ChemCompGroupFactory;
import org.biojava.nbio.structure.io.mmcif.DownloadChemCompProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class TestPdbToUniProtMapper {

    @BeforeClass
    public static void setUpBeforeClass() {

        // to be sure we download chemcomps properly
        ChemCompGroupFactory.setChemCompProvider(new DownloadChemCompProvider());

        AtomCache cache = new AtomCache();
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(true);
        cache.setUseMmCif(true); // with mmtf we get X in place of non-observed residues, due to https://github.com/biojava/biojava/issues/671
        cache.setFileParsingParams(params);
        StructureIO.setAtomCache(cache);
    }

    @Test
    public void test1smtMapping() throws IOException, StructureException, CompoundNotFoundException {

        // SIFTS record for 1smtA:
        // 1smt    A       P30340  1       122     None    None    1       122


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

        assertEquals(1, intervalPdb.beg);
        assertEquals(122, intervalPdb.end);

        assertEquals(1, intervalUniProt.beg);
        assertEquals(122, intervalUniProt.end);

        assertEquals("LYS", pdbToUniProtMapper.getPdbGroupFromUniProtIndex(3, "A").getPDBName());
    }

    @Test
    public void test4a71Mapping() throws IOException, StructureException, CompoundNotFoundException {

        // SIFTS record for 4a71A:
        // 4a71    A       P00431  3       296     None    294     68      361

        Structure s =  StructureIO.getStructure("4a71");
        PdbToUniProtMapper pdbToUniProtMapper = new PdbToUniProtMapper(s.getEntityInfos().get(0));
        UnirefEntry ref = new UnirefEntry();
        ref.setId("P00431");
        ref.setSequence(
                "MTTAVRLLPSLGRTAHKRSLYLFSAAAAAAAAATFAYSQSQKRSSSSPGGGSNHGWNNWG" +
                        "KAAALASTTPLVHVASVEKGRSYEDFQKVYNAIALKLREDDEYDNYIGYGPVLVRLAWHT" +
                        "SGTWDKHDNTGGSYGGTYRFKKEFNDPSNAGLQNGFKFLEPIHKEFPWISSGDLFSLGGV" +
                        "TAVQEMQGPKIPWRCGRVDTPEDTTPDNGRLPDADKDADYVRTFFQRLNMNDREVVALMG" +
                        "AHALGKTHLKNSGYEGPWGAANNVFTNEFYLNLLNEDWKLEKNDANNEQWDSKSGYMMLP" +
                        "TDYSLIQDPKYLSIVKEYANDQDKFFKDFSKAFEKLLENGITFPKDAPSPFIFKTLEEQG" +
                        "L");
        ref.setUniprotId("P00431");
        ref.setNcbiTaxId(559292);
        pdbToUniProtMapper.setUniProtReference(ref, new Interval(68, 361), new Interval(3, 296));

        Interval intervalPdb = pdbToUniProtMapper.getMatchingIntervalPdbCoords();
        Interval intervalUniProt = pdbToUniProtMapper.getMatchingIntervalUniProtCoords();

        assertEquals(3, intervalPdb.beg);
        assertEquals(296, intervalPdb.end);

        assertEquals(68, intervalUniProt.beg);
        assertEquals(361, intervalUniProt.end);

        assertEquals("PRO", pdbToUniProtMapper.getPdbGroupFromUniProtIndex(70, "A").getPDBName());

    }
}
