package eppic.db.jpautils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class DbConfigGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DbConfigGenerator.class);


	/**
	 * Generate DB config parameters by reading a properties file (configurationFile).
	 * The returned map can be used with JPA:
	 * <pre>
	 * EntityManagerFactory emf = 
	 * 		Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, map);
	 * </pre>
	 * 
	 * @param configurationFile
	 * @param dbName the database name, if null then it will be read from configurationFile's 'dbname' property
	 * @return
	 * @throws IOException if some non-optional property is missing in configurationFile
	 */
	public static Map<String, String> createDatabaseProperties(File configurationFile, String dbName) throws IOException {

		Properties properties = new Properties();
		properties.load(new FileInputStream(configurationFile));

		String host = null;

		String user = null;
		String pwd = null;

		// user and pwd are optional properties
		if (properties.getProperty("user")!=null && !properties.getProperty("user").isEmpty()) {
			user = properties.getProperty("user").trim();
		}
		if (properties.getProperty("password")!=null && !properties.getProperty("password").isEmpty()) {
			pwd = properties.getProperty("password").trim();
		}

		if (properties.getProperty("host")!=null && !properties.getProperty("host").isEmpty()) {
			host = properties.getProperty("host").trim();
		} else {
			throw new IOException("Missing property 'host' in config file "+configurationFile);
		}

		
		if (dbName == null) {
			// in this case the dbname must be present in file
			if (properties.getProperty("dbname")!=null && !properties.getProperty("dbname").isEmpty()) { 
				dbName = properties.getProperty("dbname");
			} else { 
				throw new IOException("Missing property 'dbname' in config file "+configurationFile);
			}
		}

		logger.debug("Properties read from file: user: '{}', pwd: *****, database: '{}', host: '{}'", user, dbName, host);

		
		Map<String, String> map = new HashMap<>();

		// MONGODB OGM parameters, see http://www.kode12.com/kode12/hibernate-ogm/hibernate-ogm-basics-mongodb/
		//			<property name="hibernate.ogm.datastore.database" value="kode12" />
		//			<property name="hibernate.ogm.datastore.host" value="localhost" />
		//			<property name="hibernate.ogm.datastore.create_database" value="true" />
		//			<property name="hibernate.ogm.datastore.username" value="root" />
		//			<property name="hibernate.ogm.datastore.password" value="root" />


		map.put("hibernate.ogm.datastore.database", dbName);
		map.put("hibernate.ogm.datastore.host", host);

		if (user!=null)
			map.put("hibernate.ogm.datastore.username", user);
		if (pwd!=null)
			map.put("hibernate.ogm.datastore.password", pwd);


		return map;

	}
	
	public static Map<String, String> createDatabaseProperties(File configurationFile) throws IOException {
		return createDatabaseProperties(configurationFile, null);
	}
}
