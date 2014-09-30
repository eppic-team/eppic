package ch.systemsx.sybit.crkwebui.server.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Entity manager handler.
 * @author AS
 */
public class EntityManagerHandler 
{
   private final static EntityManagerFactory emf = Persistence.createEntityManagerFactory("eppicjpa");
	   
   public static EntityManager getEntityManager() 
   {
       return emf.createEntityManager();
   }
}
