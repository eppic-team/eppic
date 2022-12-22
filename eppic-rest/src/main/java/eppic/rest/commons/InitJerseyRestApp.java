package eppic.rest.commons;


import eppic.db.mongoutils.DbPropertiesReader;
import eppic.db.mongoutils.MongoDbStore;
import eppic.rest.filter.CORSResponseFilter;
import eppic.rest.filter.CustomMapperProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.filtering.SelectableEntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;


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

        // our custom json serializer provider, to make sure serialization (for REST API output) does use @json annotations
        register(CustomMapperProvider.class);
        // these 2 are needed for serialization to work and be configurable via CustomMapperProvider
        register(JacksonFeature.class);
        register(JacksonJsonProvider.class);

        // registering logging feature https://stackoverflow.com/questions/2332515/how-to-get-jersey-logs-at-server
        register(LoggingFeature.class);

        // for file upload via Multipart
        register(MultiPartFeature.class);

        logger.info("Initialising Mongo connections");

        URL confFile;
        try {
            confFile = DbPropertiesReader.DEFAULT_CONFIG_FILE.toURI().toURL();
            String fromSysProp = System.getProperty(AppConstants.CONFIG_FILE_URL_SYS_PROPERTY);
            if (fromSysProp != null && !fromSysProp.equals("")) {
                confFile = new URL(fromSysProp);
            } else {
                logger.info("Using default config file location {}, because no other location was passed with -D{}", confFile, AppConstants.CONFIG_FILE_URL_SYS_PROPERTY);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed url for config file {}", e.getMessage());
            throw new RuntimeException(e);
        }
        try {
            MongoDbStore.init(confFile.openStream());
        } catch (IOException e) {
            logger.error("Failed to read db properties from config file {}", confFile);
            throw new RuntimeException(e);
        }

        Properties props = new Properties();
        try {
            props.load(confFile.openStream());
            props.forEach((k, v) -> property((String) k, v));
            // see https://stackoverflow.com/questions/27431979/passing-parameter-to-a-jersey-ressource

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("Completed Jersey REST application init");
    }


}

