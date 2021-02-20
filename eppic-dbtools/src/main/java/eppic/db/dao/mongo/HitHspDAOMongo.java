package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.mongoutils.MongoUtils;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.model.db.HitHspDB;
import eppic.model.dto.HitHsp;

import javax.persistence.Table;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HitHspDAOMongo implements HitHspDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public HitHspDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(HitHspDB.class);
        this.collectionName = tableMetadata.name();
    }

    @Override
    public void insertHitHsps(List<HitHspDB> list) throws DaoException {
        try {
            MongoUtils.writeObjects(mongoDb, collectionName, list);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void insertHitHsp(String queryId, String subjectId, double percentIdentity, int aliLength, int numMismatches, int numGapOpenings, int queryStart, int queryEnd, int subjectStart, int subjectEnd, double eValue, int bitScore) throws DaoException {
        HitHspDB hitHspDB = new HitHspDB();
        hitHspDB.setQueryId(queryId);
        hitHspDB.setSubjectId(subjectId);
        hitHspDB.setPercentIdentity(percentIdentity);
        hitHspDB.setAliLength(aliLength);
        hitHspDB.setNumMismatches(numMismatches);
        hitHspDB.setNumGapOpenings(numGapOpenings);
        hitHspDB.setQueryStart(queryStart);
        hitHspDB.setQueryEnd(queryEnd);
        hitHspDB.setSubjectStart(subjectStart);
        hitHspDB.setSubjectEnd(subjectEnd);
        hitHspDB.seteValue(eValue);
        hitHspDB.setBitScore(bitScore);

        try {
            MongoUtils.writeObject(mongoDb, collectionName, hitHspDB);
        } catch (Exception e) {
            throw new DaoException(e);
        }

    }

    @Override
    public List<HitHsp> getHitHspsForQueryId(String queryId) throws DaoException {
        // TODO how to keep the field name in a more prominent place. Or how to connect to class field name?
        List<HitHsp> list;
        try {
            list = MongoUtils.findAll(mongoDb, collectionName, Collections.singletonList("queryId"), Collections.singletonList(queryId), HitHsp.class);
        } catch (Exception e) {
            throw new DaoException(e);
        }

        return list;
    }

    @Override
    public HitHsp getHitHsp(String queryId, String subjectId, int queryStart, int queryEnd, int subjectStart, int subjectEnd) throws DaoException {
        // TODO how to keep the field names in a more prominent place. Or how to connect to class field names?
        HitHsp hit;
        try {
            hit = MongoUtils.findOne(mongoDb, collectionName,
                    Arrays.asList("queryId", "subjectId", "queryStart", "queryEnd", "subjectStart", "subjectEnd"),
                    Arrays.asList(queryId, subjectId, queryStart, queryEnd, subjectStart, subjectEnd),
                    HitHsp.class);
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return hit;
    }
}
