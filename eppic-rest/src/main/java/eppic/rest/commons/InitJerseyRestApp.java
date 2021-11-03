package eppic.rest.commons;


import eppic.db.mongoutils.DbPropertiesReader;
import eppic.db.mongoutils.MongoDbStore;
import eppic.rest.filter.CORSResponseFilter;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.filtering.SelectableEntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;


@ApplicationPath("/")
public class InitJerseyRestApp extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(InitJerseyRestApp.class);

    public static final String RESOURCE_PACKAGE = "eppic.rest.endpoints";
    // note the filter package with the CORS filter needs to be in the resources to scan or CORS filter won't work - JD 2017-11-16
    public static final String PACKAGES_TO_SCAN = RESOURCE_PACKAGE + ";" + "eppic.rest.filter";

    public InitJerseyRestApp(@Context ServletConfig servletConfig, @Context ServletContext servletContext) {

        // ServletContext needed only for application context path in swagger,
        // see https://stackoverflow.com/questions/19450202/get-servletcontext-in-application/27785718

        logger.info("Initialising Jersey REST application");

        logger.info("Current git SHA is {}", AppConstants.PROJECT_SHA);
        logger.info("Application version is {}", AppConstants.PROJECT_VERSION);
        logger.info("API version is {}", AppConstants.MAJOR_VERSION);

        packages(true, PACKAGES_TO_SCAN);

        register(CORSResponseFilter.class);

        register(SelectableEntityFilteringFeature.class);
        property(SelectableEntityFilteringFeature.QUERY_PARAM_NAME, "select");

        // registering logging feature https://stackoverflow.com/questions/2332515/how-to-get-jersey-logs-at-server
        register(LoggingFeature.class);

        logger.info("Initialising Mongo connections");
        // TODO consider passing an arbitrary file name (with system prop?)
        File confFile = DbPropertiesReader.DEFAULT_CONFIG_FILE;
        try {
            MongoDbStore.init(confFile);
        } catch (IOException e) {
            logger.error("Failed to read db properties from config file {}", confFile);
            throw new RuntimeException(e);
        }

        logger.info("Completed Jersey REST application init");
    }


}

