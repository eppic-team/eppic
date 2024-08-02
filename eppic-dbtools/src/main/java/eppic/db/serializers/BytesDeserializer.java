package eppic.db.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Base64;

public class BytesDeserializer extends StdDeserializer<byte[]> {

    public BytesDeserializer() {
        super(byte[].class);
    }

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String base64 = node.asText();
        return Base64.getDecoder().decode(base64);
    }
}
