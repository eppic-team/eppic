package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.MongoUtils;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.model.db.HitHspDB;
import eppic.model.dto.HitHsp;

import javax.persistence.Table;
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
        //TODO implement!
        return null;
    }

    @Override
    public HitHsp getHitHsp(String queryId, String subjectId, int queryStart, int queryEnd, int subjectStart, int subjectEnd) throws DaoException {
        //TODO implement!
        return null;
    }
}
