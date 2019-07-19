package eppic.db.dao.jpa;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtMetadataDAO;

import eppic.model.db.UniProtMetadataDB;
import eppic.model.dto.UniProtMetadata;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;



public class UniProtMetadataDAOJpa implements UniProtMetadataDAO {

    @Override
    public void insertUniProtMetadata(String uniRefType, String version) throws DaoException {
        EntityManager entityManager = null;

        UniProtMetadataDB uniProtMetadataDB = new UniProtMetadataDB();
        uniProtMetadataDB.setUniRefType(uniRefType);
        uniProtMetadataDB.setVersion(version);

        try {
            entityManager = EntityManagerHandler.getEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist(uniProtMetadataDB);
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
    public UniProtMetadata getUniProtMetadata() throws DaoException {
        EntityManager entityManager = null;

        try {
            entityManager = EntityManagerHandler.getEntityManager();

            TypedQuery<UniProtMetadataDB> query = entityManager.createQuery( "SELECT o FROM UniProtMetadataDB o", UniProtMetadataDB.class);

            UniProtMetadataDB uniProtMetadataDB = query.getSingleResult();

            return UniProtMetadata.create(uniProtMetadataDB);
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
