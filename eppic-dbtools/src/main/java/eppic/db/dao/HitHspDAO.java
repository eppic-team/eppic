package eppic.db.dao;

import eppic.model.db.HitHspDB;

import java.util.List;

public interface HitHspDAO {

    void insertHitHsps(List<HitHspDB> list) throws DaoException;

    void insertHitHsp(
            String queryId,
            String subjectId,
            double percentIdentity,
            int aliLength,
            int numMismatches,
            int numGapOpenings,
            int queryStart,
            int queryEnd,
            int subjectStart,
            int subjectEnd,
            double eValue,
            int bitScore) throws DaoException;

    List<HitHspDB> getHitHspsForQueryId(String queryId) throws DaoException;

    HitHspDB getHitHsp(String queryId, String subjectId, int queryStart, int queryEnd, int subjectStart, int subjectEnd) throws DaoException;
}
