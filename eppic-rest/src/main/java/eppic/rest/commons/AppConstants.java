package eppic.rest.commons;

import eppic.db.jpautils.DbConfigGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class AppConstants {

    private static final Logger logger = LoggerFactory.getLogger(AppConstants.class);


    /**
     * The name of the parameter to be passed to JVM as system property with -D
     */
    public static final String DB_PROPERTIES_PARAM =  "eppicDbProperties";

    public static final String ABOUT_PROPERTIES_FILE_NAME = "about.properties";

    /**
     * The project version as declared in pom file (e.g. 1.2.1)
     */
    public static final String PROJECT_VERSION = getBuildProperties().getProperty("project.version", "0.0.0");

    /**
     * The major version as extracted from {@link #PROJECT_VERSION}, e.g. from project version 1.2.1, major is 1
     */
    public static final String MAJOR_VERSION = PROJECT_VERSION.substring(0, PROJECT_VERSION.indexOf('.'));

    /**
     * The git SHA hash value indicating the current git revision
     */
    public static final String PROJECT_SHA = getBuildProperties().getProperty("build.hash", "NA");

    /**
     * The prefix to all resources, same value as set in servlets in web.xml
     */
    public static final String RESOURCES_PREFIX = "eppic-rest/api";

    /**
     * The full prefix including leading slash and the major version number
     */
    public static final String RESOURCES_PREFIX_FULL = "/" + AppConstants.RESOURCES_PREFIX + "/v" + AppConstants.MAJOR_VERSION;

    /**
     * The settings to be passed to EntityManagerHandler to initialise the JPA connection
     */
    public static final Map<String,String> DB_SETTINGS = readDbProperties();


    public static Properties getBuildProperties() {
        InputStream propstream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ABOUT_PROPERTIES_FILE_NAME);
        Properties props = new Properties();

        try {
            props.load(propstream);
        } catch (Exception e) {
            logger.warn("Could not get the build properties from {} file! Build information will not be available.", ABOUT_PROPERTIES_FILE_NAME);
        }

        return props;
    }

    private static Map<String, String> readDbProperties() {
        // initialising db settings
        String value = System.getProperty(DB_PROPERTIES_PARAM);
        if (value==null || value.trim().isEmpty()) {
            logger.error("A location of a properties file with JPA and db setting must be passed to JVM with -D{}. Forcing JVM exit.",
                    DB_PROPERTIES_PARAM);
            System.exit(1);
        }
        File dbPropertiesFile =  new File(value);
        try {
            if (!dbPropertiesFile.exists()) {
                logger.error("The db properties file {} does not exist! Forcing JVM exit.",dbPropertiesFile);
                System.exit(1);
            } else {
                logger.info("Reading db properties file {}", dbPropertiesFile);
                return DbConfigGenerator.createDatabaseProperties(dbPropertiesFile);
            }
        } catch (IOException e) {
            logger.error("Could not read all needed properties from db config file {} specified with system property {}. Error: {}. Forcing JVM exit.",
                    dbPropertiesFile, DB_PROPERTIES_PARAM, e.getMessage());
            System.exit(1);
        }
        return null;
    }
}