package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.jpautils.DbConfigGenerator;


/**
 * Entity manager handler.
 * @author AS
 */
public class EntityManagerHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(EntityManagerHandler.class);
	
	/**
	 * The property that needs to be passed to JVM with a -D to specify the 
	 * directory where all config files are located (the "config profile").
	 */
	public static final String CONFIG_DIR_PROPERTY	  = "eppicWuiConfigDir";
	
	// TODO this code is copy-pasted from CrkWebuiImpl, review how to unify
	// the off-war location for server config files
	private static final String CONFIG_FILES_LOCATION = System.getProperty(CONFIG_DIR_PROPERTY);
	
	static {
		if (CONFIG_FILES_LOCATION==null || CONFIG_FILES_LOCATION.isEmpty()) {
			logger.error("Property {} wasn't specified correctly. The property must be passed to the JVM with a -D parameter", CONFIG_DIR_PROPERTY);
			throw new RuntimeException("Can't continue without a valid config file dir");
		} else if (! new File(CONFIG_FILES_LOCATION).isDirectory()) {
			logger.error("The value '{}' specified for the config files directory with system property {} is not a directory!", CONFIG_FILES_LOCATION , CONFIG_DIR_PROPERTY);
			throw new RuntimeException("Can't continue without a valid config file dir");
		} else {
			logger.info("Reading configuration files from dir {}, passed through property {}", CONFIG_FILES_LOCATION, CONFIG_DIR_PROPERTY);
		}
	}

	// the file with the eppicjpa settings for database access through hibernate
	public static final String DB_PROPERTIES_FILE = CONFIG_FILES_LOCATION + "/eppic-db.properties";

	/**
	 * The settings to be passed to EntityManagerHandler to initialise the JPA connection
	 */
	public static Map<String,String> dbSettings;

	static {
		// initialising db settings
		try {
			File dbPropertiesFile =  new File(DB_PROPERTIES_FILE);
			if (!dbPropertiesFile.exists()) {
				logger.error("The db properties file {} does not exist!",dbPropertiesFile);
			} else {
				logger.info("Reading db properties file {}", dbPropertiesFile);
				dbSettings = DbConfigGenerator.createDatabaseProperties(dbPropertiesFile);
			}
		} catch (IOException e) {
			logger.error("Could not read all needed properties from db config file {}. Error: {}", 
					DB_PROPERTIES_FILE, e.getMessage());

		}
	}

	private final static EntityManagerFactory emf = 
			Persistence.createEntityManagerFactory("eppicjpa", dbSettings);

	public static EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
	
	
}
