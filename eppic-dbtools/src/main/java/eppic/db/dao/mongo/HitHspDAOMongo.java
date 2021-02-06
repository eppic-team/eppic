package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.MongoUtils;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.model.db.HitHspDB;
import eppic.model.dto.HitHsp;

import java.util.List;

public class HitHspDAOMongo implements HitHspDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public HitHspDAOMongo(MongoDatabase mongoDb, String collectionName) {
        this.mongoDb = mongoDb;
        this.collectionName = collectionName;
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

        MongoUtils.writeObject(mongoDb, collectionName, hitHspDB);

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
