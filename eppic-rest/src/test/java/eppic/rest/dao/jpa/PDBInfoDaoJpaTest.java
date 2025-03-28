package eppic.rest.dao.jpa;

import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;

import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.loaders.EntryData;
import eppic.db.loaders.UploadToDb;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;
import eppic.rest.commons.EppicRestProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations = "file:/home/jose/eppic/configs/eppic.rest-prodA.properties")
public class PDBInfoDaoJpaTest {

    @Autowired
    private EppicRestProperties eppicRestProperties;

    /**
     * Test Mongo dao, must pass config with spring mechanisms
     */
    //@Ignore
    @Test
    public void testPDBInfoDaoMongoRead() throws DaoException {

        String pdbId = "1smt";

        PDBInfoDAO dao = new PDBInfoDAOMongo(MongoUtils.getMongoDatabase(eppicRestProperties.getDbName(), eppicRestProperties.getMongoUri()));
        PdbInfoDB pdbInfoDB = dao.getPDBInfo(pdbId, true);
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
                assertFalse(idb.getChain1().isEmpty());
                assertFalse(idb.getChain2().isEmpty());
            }
        }

        for (AssemblyDB adb : pdbInfoDB.getAssemblies()) {
            assertFalse(adb.getAssemblyScores().isEmpty());
        }

        for (ChainClusterDB ccdb : pdbInfoDB.getChainClusters()) {
            assertFalse(ccdb.getMemberChains().isEmpty());
            //assertFalse(ccdb.getHomologs().isEmpty());
        }

    }

    //@Ignore
    @Test
    public void testPdbInfoDAOMongoWrite() throws DaoException {
        String pdbId = "1smt";
        EntryData entryData = UploadToDb.readSerializedFile(new File("/home/jose/eppic/jobs/1smt/", pdbId + UploadToDb.SERIALIZED_FILE_SUFFIX));

        PDBInfoDAO dao = new PDBInfoDAOMongo(MongoUtils.getMongoDatabase(eppicRestProperties.getDbName(), eppicRestProperties.getMongoUri()));
        dao.insertPDBInfo(entryData.getPdbInfoDB());
    }
}
