package eppic;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.EntityInfo;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.chem.ChemCompGroupFactory;
import org.biojava.nbio.structure.chem.DownloadChemCompProvider;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.StructureFiletype;
import org.junit.BeforeClass;
import org.junit.Test;

import eppic.model.db.UniProtRefWarningDB;
import eppic.model.db.UniProtRefWarningType;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class TestChainEvolContext {

    @BeforeClass
    public static void setUpBeforeClass() {
        ChemCompGroupFactory.setChemCompProvider(new DownloadChemCompProvider());

        AtomCache cache = new AtomCache();
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(true);
        cache.setFiletype(StructureFiletype.CIF);
        cache.setFileParsingParams(params);
        StructureIO.setAtomCache(cache);
    }

    private static EppicParams getUserJobParams() {
        EppicParams params = new EppicParams();
        // input from file, i.e. a user job: SIFTS is not used
        params.setInFile(new File("user_job.cif"));
        params.setHomologsSearchMode(HomologsSearchMode.LOCAL);
        return params;
    }

    /**
     * Peptide chains (insulin) are not analysed and get a typed warning
     */
    @Test
    public void testPeptideWarningIsTyped() throws IOException, StructureException {
        Structure s = StructureIO.getStructure("4ins");
        EppicParams params = getUserJobParams();

        ChainEvolContextList cecl = new ChainEvolContextList(s, params);
        int count = 0;
        for (ChainEvolContext cec : cecl.getAllChainEvolContext()) {
            cec.retrieveQueryData(params);
            assertFalse(cec.hasQueryMatch());
            assertEquals(1, cec.getQueryWarnings().size());
            UniProtRefWarningDB warning = cec.getQueryWarnings().get(0);
            assertEquals(UniProtRefWarningType.PEPTIDE, warning.getType());
            assertTrue(warning.getText().startsWith("Chain is a peptide"));
            count++;
        }
        assertEquals(2, count);
    }

    /**
     * For input from file the chain's sequence must be taken as the query as is, with no UniProt reference
     */
    @Test
    public void testQueryFromSequenceForFileInput() throws IOException, StructureException {
        Structure s = StructureIO.getStructure("1smt");
        EppicParams params = getUserJobParams();

        ChainEvolContextList cecl = new ChainEvolContextList(s, params);
        EntityInfo entity = s.getEntityInfos().get(0);
        ChainEvolContext cec = cecl.getAllChainEvolContext().iterator().next();

        cec.retrieveQueryData(params);

        assertTrue(cec.hasQueryMatch());
        assertFalse(cec.hasUniProtRef());
        assertTrue(cec.getQueryWarnings().isEmpty());

        String seqres = entity.getRepresentative().getSeqResSequence();
        assertEquals(seqres, cec.getQuery().getSequence());
        assertEquals(ChainEvolContext.QUERY_ID_PREFIX + cec.getRepresentativeChainCode(), cec.getQuery().getUniId());
        assertFalse(cec.getQuery().hasTaxons());

        // the mapping is the sequence to itself: no mismatches and the "reference" positions are the SEQRES positions
        PdbToUniProtMapper mapper = cec.getPdbToUniProtMapper();
        assertEquals(1, mapper.getMatchingIntervalUniProtCoords().beg);
        assertEquals(seqres.length(), mapper.getMatchingIntervalUniProtCoords().end);
        assertEquals(seqres.length(), mapper.getHomologsSearchInterval(HomologsSearchMode.GLOBAL).getLength());

        for (Chain c : entity.getChains()) {
            for (Group g : c.getAtomGroups()) {
                if (!g.isAminoAcid()) continue;
                assertTrue(mapper.isPdbGroupMatchingUniProt(g));
                int seqresIndex = entity.getAlignedResIndex(g, c);
                assertEquals(seqresIndex, mapper.getUniProtIndexForPdbGroup(g, false));
            }
        }
    }

    /**
     * With blasting disabled, homologs can only come from the cache, which is keyed by UniProt reference:
     * no evolutionary analysis without a UniProt reference
     */
    @Test
    public void testNoQueryForFileInputWithNoBlast() throws IOException, StructureException {
        Structure s = StructureIO.getStructure("1smt");
        EppicParams params = getUserJobParams();
        params.setNoBlast(true);

        ChainEvolContextList cecl = new ChainEvolContextList(s, params);
        ChainEvolContext cec = cecl.getAllChainEvolContext().iterator().next();

        cec.retrieveQueryData(params);

        assertFalse(cec.hasQueryMatch());
        assertFalse(cec.hasUniProtRef());
        assertNull(cec.getQuery());
    }
}
