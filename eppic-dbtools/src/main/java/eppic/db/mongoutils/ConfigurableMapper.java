package eppic.db.mongoutils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eppic.db.serializers.NanAwareDoubleSerializer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Provides a default customization of {@link com.fasterxml.jackson.databind.ObjectMapper}
 * sharable across application.
 *
 */
public class ConfigurableMapper {

    private static ObjectMapper mapper;

    private static void configure() {

        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        // keep the original camel case in java types by not setting anything
        //mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        mapper.setDateFormat(df);

        // for text json files to have pretty json printed
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        // custom serializer for doubles so that NaN as written as null
        mapper.registerModule(new SimpleModule().addSerializer(Double.class, new NanAwareDoubleSerializer()));
    }

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            configure();
        }
        return mapper;
    }

}
