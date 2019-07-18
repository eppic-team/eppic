package eppic.db;

import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.jpa.HitHspDAOJpa;
import eppic.db.dao.jpa.UniProtInfoDAOJpa;
import eppic.db.jpautils.DbConfigGenerator;
import eppic.model.dto.HitHsp;
import eppic.model.dto.UniProtInfo;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestJpa {

    private static Map<String, String> dbSettings;

    private void loadProperties() throws IOException {
        dbSettings = DbConfigGenerator.createDatabaseProperties(new File(System.getProperty("user.home"), "eppic-mongodb.properties"));
        EntityManagerHandler.initFactory(dbSettings);
    }

    @Ignore // can only be run with a mongodb and settings in a config file
    @Test
    public void testUniProtInfoDAO() throws IOException, DaoException {
        if (dbSettings == null) {
            loadProperties();
        }
        UniProtInfoDAO dao = new UniProtInfoDAOJpa();
        dao.insertUniProtInfo("P1234567", "AATGRYYTEWSDFGHKLLAADCVVIIE", "Homo", "Sapiens");
        dao.insertUniProtInfo("P1234568", "AATGRYYTEWSDFGHKLLAADCVVIIEACD", "Mus", "Musculus");
        dao.insertUniProtInfo("P1234569", "AATGRYYTEWSDFGHKLLAADCVVIIEACDEF", "Sus", "Scrofa");

        UniProtInfo uniProtInfo = dao.getUniProtInfo("P1234567");

        assertEquals("P1234567", uniProtInfo.getUniId());
        assertEquals("AATGRYYTEWSDFGHKLLAADCVVIIE", uniProtInfo.getSequence());

    }


    @Ignore // can only be run with a mongodb and settings in a config file
    @Test
    public void testHitHspDAO() throws IOException, DaoException {
        if (dbSettings == null) {
            loadProperties();
        }
        HitHspDAO dao = new HitHspDAOJpa();
        dao.insertHitHsp("P1234567", "O1234567", 0.98, 100, 2, 0, 1, 100, 10, 110, 1E-103, 300);

        List<HitHsp> hitHsps = dao.getHitHspsForQueryId("P1234567");

        assertEquals(1, hitHsps.size());
        assertEquals("P1234567", hitHsps.get(0).getQueryId());
        assertEquals("O1234567", hitHsps.get(0).getSubjectId());
        assertEquals(100, hitHsps.get(0).getAliLength());

        HitHsp hitHsp = dao.getHitHsp("P1234567", "O1234567", 1, 100, 10, 110);
        assertEquals("P1234567", hitHsp.getQueryId());
        assertEquals("O1234567", hitHsp.getSubjectId());
        assertEquals(100, hitHsp.getAliLength());


    }


}
