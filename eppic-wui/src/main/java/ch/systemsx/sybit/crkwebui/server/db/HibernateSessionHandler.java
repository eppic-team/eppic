package ch.systemsx.sybit.crkwebui.server.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateSessionHandler 
{
	
	private static final Logger logger = LoggerFactory.getLogger(HibernateSessionHandler.class);
	
	
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() 
    {
        try 
        {
            return new Configuration().configure().buildSessionFactory();
        }
        catch (Throwable ex) 
        {
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() 
    {
        return sessionFactory;
    }
}
    