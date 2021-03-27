package eppic.db.dao.mongo;

import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.model.db.PdbInfoDB;

public class PDBInfoDAOMongo implements PDBInfoDAO {
    @Override
    public PdbInfoDB getPDBInfo(String jobId) throws DaoException {
        return null;
    }

    @Override
    public PdbInfoDB getPDBInfo(String jobId, boolean withChainClustersAndResidues) throws DaoException {
        return null;
    }

    @Override
    public void insertPDBInfo(PdbInfoDB pdbInfo) throws DaoException {

    }
}
