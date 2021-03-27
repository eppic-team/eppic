package eppic.db.dao.mongo;

import eppic.db.dao.ChainClusterDAO;
import eppic.db.dao.DaoException;
import eppic.model.db.ChainClusterDB;

import java.util.List;

public class ChainClusterDAOMongo implements ChainClusterDAO {
    @Override
    public List<ChainClusterDB> getChainClusters(int pdbInfoUid) throws DaoException {
        return null;
    }
}
