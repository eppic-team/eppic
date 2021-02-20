package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.mongoutils.MongoUtils;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtMetadataDAO;
import eppic.model.db.UniProtMetadataDB;
import eppic.model.dto.UniProtMetadata;

import javax.persistence.Table;

public class UniProtMetadataDAOMongo implements UniProtMetadataDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public UniProtMetadataDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(UniProtMetadataDB.class);
        this.collectionName = tableMetadata.name();
    }

    @Override
    public void insertUniProtMetadata(String uniRefType, String version) throws DaoException {
        UniProtMetadataDB uniProtMetadataDB = new UniProtMetadataDB();
        uniProtMetadataDB.setUniRefType(uniRefType);
        uniProtMetadataDB.setVersion(version);

        try {
            MongoUtils.writeObject(mongoDb, collectionName, uniProtMetadataDB);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public UniProtMetadata getUniProtMetadata() throws DaoException {
        UniProtMetadata val;
        try {
            val = MongoUtils.findFirst(mongoDb, collectionName, UniProtMetadata.class);
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return val;
    }
}
