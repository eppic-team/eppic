package eppic.rest.commons;


import eppic.rest.filter.CORSResponseFilter;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class CustomApplication extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(CustomApplication.class);

    public static final String RESOURCE_PACKAGE = "eppic.rest";
    // note the filter package with the CORS filter needs to be in the resources to scan or CORS filter won't work - JD 2017-11-16
    public static final String PACKAGES_TO_SCAN = RESOURCE_PACKAGE + ";" + "eppic.rest.filter";

    public CustomApplication() throws Exception {

        logger.info("Starting CustomApplication");

        logger.info("Current git SHA is {}", AppConstants.PROJECT_SHA);
        logger.info("Application version is {}", AppConstants.PROJECT_VERSION);
        logger.info("API version is {}", AppConstants.MAJOR_VERSION);

        packages(true, PACKAGES_TO_SCAN);

        register(CORSResponseFilter.class);

        //Hooking up Swagger-Core
        logger.info("Registering swagger core");
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);

        //Configure and Initialize Swagger
        logger.info("Initialising swagger bean config");
        createSwaggerBeanConfig();

        logger.info("Completed CustomApplication");
    }

    /**
     * Setting up swagger, see https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-Jersey-2.X-Project-Setup-1.5
     * @return
     */
    private static BeanConfig createSwaggerBeanConfig(){
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(AppConstants.PROJECT_VERSION);
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setBasePath(AppConstants.RESOURCES_PREFIX_FULL);
        beanConfig.setResourcePackage(RESOURCE_PACKAGE);
        beanConfig.setDescription( "List of services currently provided" );
        beanConfig.setTitle( "EPPIC REST API" );
        beanConfig.setScan(true);
        return beanConfig;
    }
}

