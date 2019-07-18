package eppic.db;

import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.jpa.UniProtInfoDAOJpa;
import eppic.db.jpautils.DbConfigGenerator;
import eppic.model.dto.UniProtInfo;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TestJpa {

    private static Map<String, String> dbSettings;

    private void loadProperties() throws IOException {
        dbSettings = DbConfigGenerator.createDatabaseProperties(new File("/home/jose/eppic-mongodb.properties"));
        EntityManagerHandler.initFactory(dbSettings);
    }

    @Ignore // can only be run with a mongodb and settings in a config file
    @Test
    public void testPersisting() throws IOException, DaoException {
        if (dbSettings == null) {
            loadProperties();
        }
        UniProtInfoDAO dao = new UniProtInfoDAOJpa();
        dao.insertUniProtInfo("P1234567", "AATGRYYTEWSDFGHKLLAADCVVIIE", "Homo", "Sapiens");
        dao.insertUniProtInfo("P1234568", "AATGRYYTEWSDFGHKLLAADCVVIIEACD", "Mus", "Musculus");
        dao.insertUniProtInfo("P1234569", "AATGRYYTEWSDFGHKLLAADCVVIIEACDEF", "Sus", "Scrofa");

    }

    @Ignore
    @Test
    public void testReading() throws IOException, DaoException {
        if (dbSettings == null) {
            loadProperties();
        }
        EntityManager em = EntityManagerHandler.getEntityManager();
        UniProtInfoDAO dao = new UniProtInfoDAOJpa();
        UniProtInfo uniProtInfo = dao.getUniProtInfo("P1234567");

        assertEquals("P1234567", uniProtInfo.getUniId());
        assertEquals("AATGRYYTEWSDFGHKLLAADCVVIIE", uniProtInfo.getSequence());
    }
}
