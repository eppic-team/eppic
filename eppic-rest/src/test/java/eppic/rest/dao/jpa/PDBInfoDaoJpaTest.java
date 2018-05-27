package eppic.rest.dao.jpa;

import ch.systemsx.sybit.crkwebui.shared.model.*;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.jpa.PDBInfoDAOJpa;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class PDBInfoDaoJpaTest {

    /**
     * Test jpa dao, must pass config with -DeppicDbProperties
     */
    @Ignore
    @Test
    public void testPDBInfoDaoJpa() throws DaoException {

        String pdbId ="1smt";

        PDBInfoDAO dao = new PDBInfoDAOJpa();
        PdbInfo pdbInfoDB = dao.getPDBInfo(pdbId);
        assertNotNull(pdbInfoDB);

        assertNotNull(pdbInfoDB.getAssemblies());
        assertNotNull(pdbInfoDB.getChainClusters());
        assertNotNull(pdbInfoDB.getInterfaceClusters());
        assertNotNull(pdbInfoDB.getRunParameters());

        //JobDB jobDB = pdbInfoDB.getJob();
        //assertEquals(pdbId, jobDB.getJobId());

        //RunParametersDB params = pdbInfoDB.getRunParameters();
        //assertEquals("GLOBAL", params.getSearchMode());

        for (InterfaceCluster icdb : pdbInfoDB.getInterfaceClusters()) {
            assertTrue(icdb.getClusterId()>0);
            assertEquals(pdbId, icdb.getPdbCode());
            for (Interface idb : icdb.getInterfaces()) {
                assertTrue(idb.getChain1().length()>0);
                assertTrue(idb.getChain2().length()>0);
            }
        }

        for (Assembly adb : pdbInfoDB.getAssemblies()) {
            assertTrue(adb.getId()>0);
            assertTrue(adb.getAssemblyScores().size()>0);
        }

        for (ChainCluster ccdb : pdbInfoDB.getChainClusters()) {
            assertTrue(ccdb.getMemberChains().length()>0);
            assertTrue(ccdb.getHomologs().size()>0);
        }

    }
}
