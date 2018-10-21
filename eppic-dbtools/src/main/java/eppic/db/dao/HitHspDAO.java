package eppic.db.dao;

import eppic.model.dto.HitHsp;

import java.util.List;

public interface HitHspDAO {

    void insertHitHsp(
            String db,
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

    List<HitHsp> getHitHspsForQueryId(String queryId) throws DaoException;

    HitHsp getHitHsp(String queryId, String subjectId, int queryStart, int queryEnd, int subjectStart, int subjectEnd) throws DaoException;
}
