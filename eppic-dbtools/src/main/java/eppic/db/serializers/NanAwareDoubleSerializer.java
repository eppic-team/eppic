package eppic.db.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * NaN aware serializer. Will serialize NaN as null.
 */
public class NanAwareDoubleSerializer extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (Double.isNaN(value)) {
            jgen.writeNull();
        } else {
            jgen.writeNumber(value);
        }
    }

}