package eppic.rest.dao.jpa;

import eppic.rest.commons.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class EntityManagerHandler {

    private static final Logger logger = LoggerFactory.getLogger(EntityManagerHandler.class);

    private final static EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("eppicjpa", AppConstants.DB_SETTINGS);

    public static EntityManager getEntityManager() {
        logger.debug("Creating entity manager from factory {}", emf.toString());
        return emf.createEntityManager();
    }


}