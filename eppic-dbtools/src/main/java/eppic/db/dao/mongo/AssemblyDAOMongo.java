package eppic.db.dao.mongo;

import eppic.db.dao.AssemblyDAO;
import eppic.db.dao.DaoException;
import eppic.model.db.AssemblyDB;

import java.util.List;

public class AssemblyDAOMongo implements AssemblyDAO {
    @Override
    public List<AssemblyDB> getAssemblies(int pdbInfoUid, boolean withScores, boolean withGraph) throws DaoException {
        return null;
    }

    @Override
    public AssemblyDB getAssemblyByPdbAssemblyId(int pdbInfoUid, int pdbAssemblyId, boolean withGraph) throws DaoException {
        return null;
    }

    @Override
    public AssemblyDB getAssembly(int pdbInfoUid, int assemblyId, boolean withGraph) throws DaoException {
        return null;
    }
}
