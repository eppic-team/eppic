package ch.systemsx.sybit.crkwebui.server.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;

/**
 * Entity manager handler.
 * @author AS
 */
public class EntityManagerHandler {


	private final static EntityManagerFactory emf = 
			Persistence.createEntityManagerFactory("eppicjpa", CrkWebServiceImpl.dbSettings);

	public static EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
	
	
}
