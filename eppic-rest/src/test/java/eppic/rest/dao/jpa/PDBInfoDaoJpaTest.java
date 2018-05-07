package eppic.rest.dao.jpa;

import eppic.model.*;
import eppic.rest.dao.DaoException;
import eppic.rest.dao.PDBInfoDAO;
import org.junit.Test;
import static org.junit.Assert.*;

public class PDBInfoDaoJpaTest {

    /**
     * Test jpa dao, must pass config with -DeppicDbProperties
     */
    @Test
    public void testPDBInfoDaoJpa() throws DaoException {

        String pdbId ="1smt";

        PDBInfoDAO dao = new PDBInfoDAOJpa();
        PdbInfoDB pdbInfoDB = dao.getPDBInfo(pdbId);
        assertNotNull(pdbInfoDB);

        assertNotNull(pdbInfoDB.getAssemblies());
        assertNotNull(pdbInfoDB.getChainClusters());
        assertNotNull(pdbInfoDB.getInterfaceClusters());
        assertNotNull(pdbInfoDB.getJob());
        assertNotNull(pdbInfoDB.getRunParameters());

        //JobDB jobDB = pdbInfoDB.getJob();
        //assertEquals(pdbId, jobDB.getJobId());

        //RunParametersDB params = pdbInfoDB.getRunParameters();
        //assertEquals("GLOBAL", params.getSearchMode());

        for (InterfaceClusterDB icdb : pdbInfoDB.getInterfaceClusters()) {
            assertTrue(icdb.getClusterId()>0);
            assertEquals(pdbId, icdb.getPdbCode());
            for (InterfaceDB idb : icdb.getInterfaces()) {
                assertTrue(idb.getChain1().length()>0);
                assertTrue(idb.getChain2().length()>0);
                assertEquals(pdbId, idb.getPdbCode());
            }
        }

        for (AssemblyDB adb : pdbInfoDB.getAssemblies()) {
            assertTrue(adb.getId()>0);
            assertEquals(pdbId, adb.getPdbCode());
            assertTrue(adb.getAssemblyScores().size()>0);
        }

        for (ChainClusterDB ccdb : pdbInfoDB.getChainClusters()) {
            assertTrue(ccdb.getMemberChains().length()>0);
            assertTrue(ccdb.getHomologs().size()>0);
        }

    }
}
