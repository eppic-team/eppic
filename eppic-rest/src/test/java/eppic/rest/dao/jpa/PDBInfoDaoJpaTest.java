package eppic.rest.dao.jpa;

import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;

import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;
import eppic.rest.commons.ServerProperties;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class PDBInfoDaoJpaTest {


    /**
     * Test Mongo dao, must pass config with spring mechanisms
     */
    @Ignore
    @Test
    public void testPDBInfoDaoJpa() throws DaoException {

        String pdbId ="1smt";

        // TODO this needs to be injected. For now the test is broken
        ServerProperties serverProperties = new ServerProperties();
        PDBInfoDAO dao = new PDBInfoDAOMongo(MongoUtils.getMongoDatabase(serverProperties.getDbName(), serverProperties.getMongoUri()));
        PdbInfoDB pdbInfoDB = dao.getPDBInfo(pdbId);
        assertNotNull(pdbInfoDB);

        assertNotNull(pdbInfoDB.getAssemblies());
        assertNotNull(pdbInfoDB.getChainClusters());
        assertNotNull(pdbInfoDB.getInterfaceClusters());
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
            }
        }

        for (AssemblyDB adb : pdbInfoDB.getAssemblies()) {
            assertTrue(adb.getId()>0);
            assertTrue(adb.getAssemblyScores().size()>0);
        }

        for (ChainClusterDB ccdb : pdbInfoDB.getChainClusters()) {
            assertTrue(ccdb.getMemberChains().length()>0);
            assertTrue(ccdb.getHomologs().size()>0);
        }

    }
}
