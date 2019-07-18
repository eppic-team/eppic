package eppic.db.dao.jpa;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.model.db.UniProtInfoDB;
import eppic.model.dto.UniProtInfo;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class UniProtInfoDAOJpa implements UniProtInfoDAO {

    @Override
    public void insertUniProtInfo(String uniId, String sequence, String firstTaxon, String lastTaxon) throws DaoException {

        EntityManager entityManager = null;

        UniProtInfoDB uniProtInfoDB = new UniProtInfoDB();
        uniProtInfoDB.setUniId(uniId);
        uniProtInfoDB.setSequence(sequence);
        uniProtInfoDB.setFirstTaxon(firstTaxon);
        uniProtInfoDB.setLastTaxon(lastTaxon);

        try {
            entityManager = EntityManagerHandler.getEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist(uniProtInfoDB);
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
    public UniProtInfo getUniProtInfo(String uniId) throws DaoException {
        EntityManager entityManager = null;

        UniProtInfo uniProtInfo = null;

        try {
            entityManager = EntityManagerHandler.getEntityManager();

            TypedQuery<UniProtInfoDB> query = entityManager.createQuery( "SELECT o FROM UniProtInfoDB o WHERE o.uniId = :uniId", UniProtInfoDB.class);
            query.setParameter("uniId", uniId);

            for (UniProtInfoDB uniProtInfoDB : query.getResultList()) {
                if (uniProtInfo!=null) {
                    throw new DaoException("More than 1 UniProtInfo record found");
                }
                uniProtInfo = UniProtInfo.create(uniProtInfoDB);
            }

            return uniProtInfo;
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
