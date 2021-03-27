package eppic.db.dao.mongo;

import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceClusterDAO;
import eppic.model.db.InterfaceClusterDB;

import java.util.List;
import java.util.Set;

public class InterfaceClusterDAOMongo implements InterfaceClusterDAO {
    @Override
    public List<InterfaceClusterDB> getInterfaceClusters(int pdbInfoUid, boolean withScores, boolean withInterfaces) throws DaoException {
        return null;
    }

    @Override
    public List<InterfaceClusterDB> getInterfaceClusters(int pdbInfoUid, Set<Integer> interfaceClusterIds, boolean withScores, boolean withInterfaces) throws DaoException {
        return null;
    }
}
