package eppic.rest.dao.jpa;

import eppic.rest.commons.AppConstants;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class EntityManagerHandler {


    private final static EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("eppicjpa", AppConstants.DB_SETTINGS);

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }


}