package eppic.db.dao.jpa;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.model.db.HitHspDB;
import eppic.model.dto.HitHsp;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

public class HitHspDAOJpa implements HitHspDAO {

    @Override
    public void insertHitHsp(String queryId,
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
                             int bitScore) throws DaoException {

        EntityManager entityManager = null;

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
            entityManager = EntityManagerHandler.getEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist(hitHspDB);
            entityManager.getTransaction().commit();

        } catch(Throwable e) {

            if (entityManager != null)
                entityManager.getTransaction().rollback();

            throw new DaoException(e);

        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }

    @Override
    public List<HitHsp> getHitHspsForQueryId(String queryId) throws DaoException {
        EntityManager entityManager = null;

        List<HitHsp> hitHsps = new ArrayList<>();

        try {
            entityManager = EntityManagerHandler.getEntityManager();

            TypedQuery<HitHspDB> query = entityManager.createQuery( "SELECT o FROM HitHspDB o WHERE o.queryId = :queryId", HitHspDB.class);
            query.setParameter("queryId", queryId);

            for (HitHspDB hitHspDB : query.getResultList()) {
                hitHsps.add(HitHsp.create(hitHspDB));
            }

            return hitHsps;
        }
        catch(Throwable e) {
            throw new DaoException(e);
        }
        finally {
            if (entityManager!=null)
                entityManager.close();
        }
    }

    @Override
    public HitHsp getHitHsp(String queryId, String subjectId, int queryStart, int queryEnd, int subjectStart, int subjectEnd) throws DaoException {
        EntityManager entityManager = null;

        HitHsp hitHsp = null;

        try {
            entityManager = EntityManagerHandler.getEntityManager();

            TypedQuery<HitHspDB> query = entityManager.createQuery( "SELECT o FROM HitHspDB o " +
                    "WHERE o.queryId = :queryId " +
                    "AND o.subjectId = :subjectId " +
                    "AND o.queryStart = :queryStart " +
                    "AND o.queryEnd = :queryEnd " +
                    "AND o.subjectStart = :subjectStart " +
                    "AND o.subjectEnd = :subjectEnd"
                    , HitHspDB.class);

            query.setParameter("queryId", queryId);
            query.setParameter("subjectId", subjectId);
            query.setParameter("queryStart", queryStart);
            query.setParameter("queryEnd", queryEnd);
            query.setParameter("subjectStart", subjectStart);
            query.setParameter("subjectEnd", subjectEnd);

            for (HitHspDB hitHspDB : query.getResultList()) {
                if (hitHsp != null) {
                    throw new DaoException("More than 1 result for queryId "+queryId+", subjectId "+subjectId+
                            ", qStart "+queryStart+", qEnd "+queryEnd+", sStart "+subjectStart+", sEnd " + subjectEnd);
                }
                hitHsp = HitHsp.create(hitHspDB);
            }

            return hitHsp;
        }
        catch(Throwable e) {
            throw new DaoException(e);
        }
        finally {
            if (entityManager!=null)
                entityManager.close();
        }
    }

}
