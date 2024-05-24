package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.PdbInfoDB;

import javax.persistence.Table;
import java.util.Collections;
import java.util.List;

public class PDBInfoDAOMongo implements PDBInfoDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

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

    @Override
    public void insertPDBInfos(List<PdbInfoDB> pdbInfos) throws DaoException {
        try {
            MongoUtils.writeObjects(mongoDb, collectionName, pdbInfos);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public List<PdbInfoDB> getAll() throws DaoException {
       List<PdbInfoDB> list;
       try {
           list = MongoUtils.findAll(mongoDb, collectionName, PdbInfoDB.class);
       } catch (Exception e) {
           throw new DaoException(e);
       }
       return list;
    }

    @Override
    public long remove(String entryId) throws DaoException {
        return MongoUtils.remove(mongoDb, collectionName, "entryId", List.of(entryId));
    }
}
