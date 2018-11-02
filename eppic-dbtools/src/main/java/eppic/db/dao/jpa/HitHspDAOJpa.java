package eppic.db.dao.jpa;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.model.db.HitHspDB;
import eppic.model.db.HitHspDB_;
import eppic.model.dto.HitHsp;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<HitHspDB> criteriaQuery = criteriaBuilder.createQuery(HitHspDB.class);

            Root<HitHspDB> root = criteriaQuery.from(HitHspDB.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(HitHspDB_.queryId), queryId));

            TypedQuery<HitHspDB> query = entityManager.createQuery(criteriaQuery);

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

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<HitHspDB> criteriaQuery = criteriaBuilder.createQuery(HitHspDB.class);

            Root<HitHspDB> root = criteriaQuery.from(HitHspDB.class);
            criteriaQuery.where(
                    criteriaBuilder.equal(root.get(HitHspDB_.queryId), queryId),
                    criteriaBuilder.equal(root.get(HitHspDB_.subjectId), subjectId),
                    criteriaBuilder.equal(root.get(HitHspDB_.queryStart), queryStart),
                    criteriaBuilder.equal(root.get(HitHspDB_.queryEnd), queryEnd),
                    criteriaBuilder.equal(root.get(HitHspDB_.subjectStart), subjectStart),
                    criteriaBuilder.equal(root.get(HitHspDB_.subjectEnd), subjectEnd));

            TypedQuery<HitHspDB> query = entityManager.createQuery(criteriaQuery);

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
