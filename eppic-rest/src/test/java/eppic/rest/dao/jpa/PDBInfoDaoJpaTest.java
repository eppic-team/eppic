package eppic.rest.dao.jpa;

import eppic.model.PdbInfoDB;
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

        PDBInfoDAO dao = new PDBInfoDAOJpa();
        PdbInfoDB pdbInfoDB = dao.getPDBInfo("1smt");
        assertNotNull(pdbInfoDB);

        assertNotNull(pdbInfoDB.getAssemblies());
        assertNotNull(pdbInfoDB.getChainClusters());
        assertNotNull(pdbInfoDB.getInterfaceClusters());


    }
}
