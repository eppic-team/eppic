package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceResidueFeaturesDAO;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.InterfaceResidueFeaturesDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;

public class InterfaceResidueFeaturesDAOMongo implements InterfaceResidueFeaturesDAO {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceResidueFeaturesDAOMongo.class);

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public InterfaceResidueFeaturesDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(InterfaceResidueFeaturesDB.class);
        this.collectionName = tableMetadata.name();
    }

    @Override
    public void insertInterfResFeatures(List<InterfaceResidueFeaturesDB> list) throws DaoException {
        if (list.isEmpty()) {
            logger.debug("List of InterfaceResidueFeaturesDB is empty. Will not persist anything.");
            return;
        }
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

    @Override
    public long remove(String entryId) throws DaoException {
        return MongoUtils.remove(mongoDb, collectionName, "entryId", List.of(entryId));
    }
}
