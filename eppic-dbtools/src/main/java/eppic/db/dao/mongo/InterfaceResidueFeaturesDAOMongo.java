package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceResidueFeaturesDAO;
import eppic.db.mongoutils.MongoDbStore;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.InterfaceResidueFeaturesDB;

import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;

public class InterfaceResidueFeaturesDAOMongo implements InterfaceResidueFeaturesDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public InterfaceResidueFeaturesDAOMongo() {
        this.mongoDb = MongoDbStore.getMongoDb();
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(InterfaceResidueFeaturesDB.class);
        this.collectionName = tableMetadata.name();
    }

    public InterfaceResidueFeaturesDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(InterfaceResidueFeaturesDB.class);
        this.collectionName = tableMetadata.name();
    }

    @Override
    public void insertInterfResFeatures(List<InterfaceResidueFeaturesDB> list) throws DaoException {
        try {
            MongoUtils.writeObjects(mongoDb, collectionName, list);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public InterfaceResidueFeaturesDB getInterfResFeatures(String entryId, int interfId) throws DaoException {
        // TODO how to keep the field names in a more prominent place. Or how to connect to class field names?
        InterfaceResidueFeaturesDB feat;
        try {
            feat = MongoUtils.findOne(mongoDb, collectionName,
                    Arrays.asList("entryId", "interfaceId"),
                    Arrays.asList(entryId, interfId),
                    InterfaceResidueFeaturesDB.class);
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return feat;
    }
}
