package eppic.db;

import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.jpa.UniProtInfoDAOJpa;
import eppic.db.jpautils.DbConfigGenerator;
import org.junit.Ignore;
import org.junit.Test;

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
        EntityManager em = EntityManagerHandler.getEntityManager();
        UniProtInfoDAO dao = new UniProtInfoDAOJpa();
        dao.insertUniProtInfo("P1234567", "AATGRYYTEWSDFGHKLLAADCVVIIE", "Homo", "Sapiens");

    }

    @Ignore
    @Test
    public void testReading() throws IOException{
        if (dbSettings == null) {
            loadProperties();
        }
        EntityManager em = EntityManagerHandler.getEntityManager();

    }
}
