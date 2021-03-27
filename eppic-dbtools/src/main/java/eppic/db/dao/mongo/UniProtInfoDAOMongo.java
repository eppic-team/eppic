package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.mongoutils.MongoUtils;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.model.db.UniProtInfoDB;

import javax.persistence.Table;
import java.util.Collections;
import java.util.List;

public class UniProtInfoDAOMongo implements UniProtInfoDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public UniProtInfoDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(UniProtInfoDB.class);
        this.collectionName = tableMetadata.name();
    }

    @Override
    public void insertUniProtInfo(String uniId, String sequence, String firstTaxon, String lastTaxon) throws DaoException {
        UniProtInfoDB uniProtInfoDB = new UniProtInfoDB();
        uniProtInfoDB.setUniId(uniId);
        uniProtInfoDB.setFirstTaxon(firstTaxon);
        uniProtInfoDB.setLastTaxon(lastTaxon);
        uniProtInfoDB.setSequence(sequence);

        try {
            MongoUtils.writeObject(mongoDb, collectionName, uniProtInfoDB);
        } catch (Exception e) {
            throw new DaoException(e);
        }

    }

    @Override
    public void insertUniProtInfos(List<UniProtInfoDB> uniProtInfoDBList) throws DaoException {
        try {
            MongoUtils.writeObjects(mongoDb, collectionName, uniProtInfoDBList);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public UniProtInfoDB getUniProtInfo(String uniId) throws DaoException {
        // TODO how to keep the field name in a more prominent place. Or how to connect to class field name?
        UniProtInfoDB val;
        try {
            val = MongoUtils.findOne(mongoDb, collectionName, Collections.singletonList("uniId"), Collections.singletonList(uniId), UniProtInfoDB.class);
        } catch (Exception e) {
            throw new DaoException(e);
        }

        return val;
    }
}
