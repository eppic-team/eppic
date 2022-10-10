package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.mongoutils.MongoDbStore;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.PdbInfoDB;

import javax.persistence.Table;
import java.util.Collections;

public class PDBInfoDAOMongo implements PDBInfoDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public PDBInfoDAOMongo() {
        mongoDb = MongoDbStore.getMongoDb();
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class);
        this.collectionName = tableMetadata.name();
    }

    public PDBInfoDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class);
        this.collectionName = tableMetadata.name();
    }

    @Override
    public PdbInfoDB getPDBInfo(String entryId) throws DaoException {
        PdbInfoDB pdbInfoDB;
        try {
            pdbInfoDB = MongoUtils.findOne(mongoDb, collectionName,
                    Collections.singletonList("entryId"),
                    Collections.singletonList(entryId),
                    PdbInfoDB.class);
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return pdbInfoDB;
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