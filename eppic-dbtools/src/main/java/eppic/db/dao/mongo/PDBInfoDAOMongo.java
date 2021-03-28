package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.PdbInfoDB;

import javax.persistence.Table;

public class PDBInfoDAOMongo implements PDBInfoDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    // TODO Remove. Placeholder to get things compiling
    public PDBInfoDAOMongo() {
        mongoDb = null;
        collectionName = null;
    }

    public PDBInfoDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class);
        this.collectionName = tableMetadata.name();
    }

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
        try {
            MongoUtils.writeObject(mongoDb, collectionName, pdbInfo);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }
}
