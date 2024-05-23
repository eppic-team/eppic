package eppic.rest.commons;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * A configuration to make enum conversion work case insensitive. Needed for png image endpoint for enum {@link eppic.model.db.FileTypeEnum}
 * See <a href="https://www.baeldung.com/spring-boot-enum-mapping#1-using-applicationconversionservice">...</a>
 */
@Configuration
public class EnumMappingConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        ApplicationConversionService.configure(registry);
    }
}
