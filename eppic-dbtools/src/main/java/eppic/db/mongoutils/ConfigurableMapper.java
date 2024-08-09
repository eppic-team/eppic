package eppic.db.mongoutils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import eppic.db.serializers.BytesDeserializer;
import eppic.db.serializers.BytesSerializer;
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

        // date serialization: it's important to get the timezones right! Using the ISO-8601 recipe from https://www.baeldung.com/jackson-serialize-dates
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DateFormat df = new StdDateFormat().withColonInTimeZone(true);
        mapper.setDateFormat(df);

        // for text json files to have pretty json printed
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        // custom serializer for doubles so that NaN as written as null
        mapper.registerModule(new SimpleModule().addSerializer(Double.class, new NanAwareDoubleSerializer()));

        // without this, serialization into json to write out to MongoDB fails with :
        // java.lang.IllegalArgumentException: Cannot resolve PropertyFilter with id 'eppic.model.db.PdbInfoDB'; no FilterProvider configured
        // see https://stackoverflow.com/questions/9382094/jsonfilter-throws-jsonmappingexception-can-not-resolve-beanpropertyfilter
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));

        // needed to serialize/deserialize blobs that are in json fields (only way is using base64 encoding)
        // see https://stackoverflow.com/questions/40993617/how-to-deserialize-serialize-byte-array-using-jackson-and-wrapper-object
        mapper.registerModule(new SimpleModule().addSerializer(byte[].class, new BytesSerializer()));
        mapper.registerModule(new SimpleModule().addDeserializer(byte[].class, new BytesDeserializer()));
    }

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            configure();
        }
        return mapper;
    }

}
