package eppic.rest.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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
    public static final String RESOURCES_PREFIX = "api";

    /**
     * The full prefix including leading slash and the major version number (but not including servlet context root)
     */
    public static final String RESOURCES_PREFIX_FULL = "/" + AppConstants.RESOURCES_PREFIX + "/v" + AppConstants.MAJOR_VERSION;

    public static final String REST_API_DOCS_DIR = "api-docs";
    public static final String REST_API_DOCS_FILE = "eppic-restful-api-docs.json";

    /**
     * The file where the progress log of the eppic CLI program is written to (using -L option)
     */
    public static final String PROGRESS_LOG_FILE_NAME 	= "eppic_wui_progress.log";

    /**
     * The file that the eppic CLI writes upon successful completion, used to signal that the queuing job finished successfully
     * Must coincide with EppicParams.FINISHED_FILE_NAME (from eppic-clie module, which is not a dependency for this module)
     */
    public static final String FINISHED_FILE_NAME 		= "finished";

    /**
     * The file to signal a killed job
     */
    public static final String KILLED_FILE_NAME 		= "killed";


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
}