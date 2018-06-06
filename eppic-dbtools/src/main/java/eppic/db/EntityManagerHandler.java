package eppic.db;


import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entity manager handler.
 * @author AS
 */
public class EntityManagerHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(EntityManagerHandler.class);


	private static EntityManagerFactory emf;

	public static void initFactory(Map<String,String> dbSettings) {
		emf = Persistence.createEntityManagerFactory("eppicjpa", dbSettings);
	}

	public static EntityManager getEntityManager() {
		if (emf == null) {
			String msg = "EntityManagerHandler must be initialised by calling initFactory()";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		return emf.createEntityManager();
	}
	
	
}
