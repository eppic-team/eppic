package eppic.db.jpautils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class DbConfigGenerator {


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

		String port = "3306"; // default mysql port
		String host = null;

		String user = null;
		String pwd = null;

		// default hbm2ddl mode is validate so that in normal production operations there's no risk of altering the schema
		String hbm2ddlMode = "validate";
		String showSql = "false";

		// port is the only optional property
		if (properties.getProperty("port")!=null && !properties.getProperty("port").isEmpty()) 
			port = properties.getProperty("port").trim();

		if (properties.getProperty("user")!=null && !properties.getProperty("user").isEmpty()) {
			user = properties.getProperty("user").trim();
		} else {
			throw new IOException("Missing property 'user' in config file "+configurationFile);
		}
		if (properties.getProperty("password")!=null && !properties.getProperty("password").isEmpty()) {
			pwd = properties.getProperty("password").trim();
		} else {
			throw new IOException("Missing property 'password' in config file "+configurationFile);
		}
		if (properties.getProperty("host")!=null && !properties.getProperty("host").isEmpty()) {
			host = properties.getProperty("host").trim();
		} else {
			throw new IOException("Missing property 'host' in config file "+configurationFile);
		}
		// one more (optional) property to be able to control externally the behaviour of hibernate on startup (create tables, update, validate etc)
		if (properties.getProperty("hibernate.hbm2ddl.auto")!=null && !properties.getProperty("hibernate.hbm2ddl.auto").isEmpty()) {
			hbm2ddlMode = properties.getProperty("hibernate.hbm2ddl.auto").trim();
		}
		if (properties.getProperty("hibernate.show_sql")!=null && !properties.getProperty("hibernate.show_sql").isEmpty()) {
			showSql = properties.getProperty("hibernate.show_sql").trim();
		}
		
		if (dbName == null) {
			// in this case the dbname must be present in file
			if (properties.getProperty("dbname")!=null && !properties.getProperty("dbname").isEmpty()) { 
				dbName = properties.getProperty("dbname");
			} else { 
				throw new IOException("Missing property 'dbname' in config file "+configurationFile);
			}
		}

		//System.out.println("Using database "+dbName+" in host "+host);

		
		Map<String, String> map = new HashMap<>();
		
		map.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
		//map.put("javax.persistence.nonJtaDataSource", "");

		String url = "jdbc:mysql://"+host+":"+port+"/"+dbName;
		map.put("javax.persistence.jdbc.url", url);
		map.put("javax.persistence.jdbc.user", user); 
		map.put("javax.persistence.jdbc.password", pwd);

		map.put("hibernate.hbm2ddl.auto", hbm2ddlMode);
		map.put("hibernate.show_sql", showSql);

		map.put("hibernate.c3p0.min_size", "5");
		map.put("hibernate.c3p0.max_size", "20");
		map.put("hibernate.c3p0.timeout", "1800");
		map.put("hibernate.c3p0.max_statements", "50");		

		return map;

	}
	
	public static Map<String, String> createDatabaseProperties(File configurationFile) throws IOException {
		return createDatabaseProperties(configurationFile, null);
	}
}
