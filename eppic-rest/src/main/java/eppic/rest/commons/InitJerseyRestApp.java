package eppic.rest.commons;


import eppic.db.EntityManagerHandler;
import eppic.rest.filter.CORSResponseFilter;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.filtering.SelectableEntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        //Configure and Initialize Swagger
        logger.info("Initialising swagger/openapi config");
        initOpenApiDocs(servletConfig, servletContext);
        logger.info("Done initialising swagger/openapi config");

        logger.info("Initialising JPA/hibernate");
        EntityManagerHandler.initFactory(AppConstants.DB_SETTINGS);

        logger.info("Completed Jersey REST application init");
    }

    /**
     * Generate and store REST API documentation in Open API 3.0
     * <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md">specification</a> format.
     *
     * @param servletConfig object used by a servlet container to pass information to a servlet during initialization.
     * @param servletContext the servlet context
     */
    private static void initOpenApiDocs(@Context ServletConfig servletConfig, ServletContext servletContext) {

        OpenAPI oas = new OpenAPI();

        Info info = new Info();
        info.setTitle("EPPIC REST API");
        info.setVersion(AppConstants.PROJECT_VERSION);
        info.setDescription("Provides programmatic access to assemblies/interfaces information " +
                "stored in the EPPIC database.");
        Contact contact = new Contact();
        contact.setName("RCSB PDB");
        contact.setEmail("eppic@systemsx.ch");
        contact.setUrl("www.eppic-web.org");
        info.setContact(contact);

        oas.setInfo(info);

        // configure base path
        String basePath = servletContext.getContextPath() + AppConstants.RESOURCES_PREFIX_FULL;
        Server server = new Server();
        server.setUrl(basePath);
        oas.setServers(Collections.singletonList(server));

        // scan through REST services annotations to build OpenAPI context
        SwaggerConfiguration config = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourcePackages(Stream.of(RESOURCE_PACKAGE).collect(Collectors.toSet()));

        try {
            new JaxrsOpenApiContextBuilder<>()
                    .servletConfig(servletConfig)
                    .openApiConfiguration(config)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            logger.error("Failed to build Open API Context:", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}

