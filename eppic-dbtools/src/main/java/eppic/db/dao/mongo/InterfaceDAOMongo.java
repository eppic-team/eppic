package eppic.db.dao.mongo;

import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceDAO;
import eppic.model.db.InterfaceDB;

import java.util.List;
import java.util.Set;

public class InterfaceDAOMongo implements InterfaceDAO {
    @Override
    public List<InterfaceDB> getInterfacesForCluster(int interfaceClusterUid, boolean withScores, boolean withResidues) throws DaoException {
        return null;
    }

    @Override
    public List<InterfaceDB> getInterfacesForCluster(int interfaceClusterUid, Set<Integer> interfaceIds, boolean withScores, boolean withResidues) throws DaoException {
        return null;
    }

    @Override
    public InterfaceDB getInterface(int pdbInfoUid, int interfaceId, boolean withScores, boolean withResidues) throws DaoException {
        return null;
    }

    @Override
    public List<InterfaceDB> getInterfacesByPdbUid(int pdbInfoUid, boolean withScores, boolean withResidues) throws DaoException {
        return null;
    }
}
