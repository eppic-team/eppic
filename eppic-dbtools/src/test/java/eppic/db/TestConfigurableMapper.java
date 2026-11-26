package eppic.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import eppic.db.mongoutils.ConfigurableMapper;
import eppic.model.db.InterfaceWarningDB;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestConfigurableMapper {

    @Test
    public void shouldSerializeNanAsNull() throws IOException {
        Map<String, Double> map = new HashMap<>();
        map.put("a number", 12.345);
        map.put("a nan", Double.NaN);
        ObjectMapper mapper = ConfigurableMapper.getMapper();
        String json = mapper.writeValueAsString(map);
        System.out.println(json);
        assertTrue(json.contains("null"));
        assertFalse(json.contains("NaN"));
        assertTrue(json.contains("12.345"));
    }

    @Test
    public void shouldIgnoreUnknownProperties() throws IOException {
        // a document written by a newer eppic-model must be readable by an older one
        String json = "{\"text\":\"some warning\",\"aFieldFromTheFuture\":{\"x\":1}}";
        InterfaceWarningDB w = ConfigurableMapper.getMapper().readValue(json, InterfaceWarningDB.class);
        assertEquals("some warning", w.getText());
    }
}
