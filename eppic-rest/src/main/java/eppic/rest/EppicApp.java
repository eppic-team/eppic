package eppic.rest;

import eppic.rest.commons.BuildProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan("eppic.rest.commons")
public class EppicApp {

    private static final Logger logger = LoggerFactory.getLogger(EppicApp.class);

    private final BuildProperties buildProperties;

    @Autowired
    public EppicApp(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
        logVersions();
    }

    public static void main(String... args) {

        SpringApplication.run(EppicApp.class, args);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        // TODO set from major version in properties
                        .version(null)
                        .title("EPPIC REST API")
                        .description("Provides programmatic access to assemblies/interfaces information stored in the EPPIC database.")
                        .contact(new Contact().email("info@rcsb.org"))
                        .license(new License()
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                                .name("Apache 2.0")))
                // this makes sure that the Swagger UI doesn't try to communicate via HTTP and gets rejected due to mixed content
                .addServersItem(new Server()
                        .url("/")
                        .description("Default URL"));
    }

    private void logVersions() {
        logger.info("Project version is {}", buildProperties.getProjectVersion());
        logger.info("Project major version is {}", buildProperties.getProjectMajorVersion());
        logger.info("Project build git hash is {}", buildProperties.getHash());
    }
}
