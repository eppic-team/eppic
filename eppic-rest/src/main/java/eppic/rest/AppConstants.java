package eppic.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConstants {

    private static final Logger logger = LoggerFactory.getLogger(AppConstants.class);

    public static final String ABOUT_PROPERTIES_FILE_NAME = "about.properties";

    /**
     * The project version as declared in pom file (e.g. 1.2.1)
     */
    public static final String PROJECT_VERSION = getBuildProperties().getProperty("project.version", "NA");

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
     * The full prefix including leading slash and the major version number
     */
    public static final String RESOURCES_PREFIX_FULL = "/" + AppConstants.RESOURCES_PREFIX + "/v" + AppConstants.MAJOR_VERSION;


    public static Properties getBuildProperties() {
        InputStream propstream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ABOUT_PROPERTIES_FILE_NAME);
        Properties props = new Properties();

        try {
            props.load(propstream);
        } catch (IOException var3) {
            logger.warn("Could not get the build properties from {} file! Build information will not be available.", ABOUT_PROPERTIES_FILE_NAME);
        }

        return props;
    }

}