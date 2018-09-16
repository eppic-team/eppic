package eppic.db.dao.jpa;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.model.db.HitHspDB;
import eppic.model.db.HitHspDB_;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class HitHspDAOJpa implements HitHspDAO {

    @Override
    public void insertHitHsp(HitHspDB hitHspDB) throws DaoException {

        EntityManager entityManager = null;

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
    public List<HitHspDB> getHitHspsForQueryId(String queryId) throws DaoException {
        EntityManager entityManager = null;

        try {
            entityManager = EntityManagerHandler.getEntityManager();

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<HitHspDB> criteriaQuery = criteriaBuilder.createQuery(HitHspDB.class);

            Root<HitHspDB> root = criteriaQuery.from(HitHspDB.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(HitHspDB_.queryId), queryId));

            TypedQuery<HitHspDB> query = entityManager.createQuery(criteriaQuery);

            return query.getResultList();
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
