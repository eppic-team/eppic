package eppic.db.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BytesSerializer extends StdSerializer<byte[]> {

    public BytesSerializer() {
        super(byte[].class);
    }

    @Override
    public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(new String(Base64.getEncoder().encode(value), StandardCharsets.UTF_8));
    }
}
