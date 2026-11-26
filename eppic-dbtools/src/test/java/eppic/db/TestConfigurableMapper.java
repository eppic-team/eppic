package eppic.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import eppic.db.mongoutils.ConfigurableMapper;
import eppic.model.db.AtomPairDB;
import eppic.model.db.InterfaceWarningDB;
import eppic.model.db.InterfaceWarningType;
import eppic.model.db.ResidueRefDB;
import eppic.model.db.UniProtRefWarningDB;
import eppic.model.db.UniProtRefWarningType;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    @Test
    public void shouldReadWarningsWithoutStructuredData() throws IOException {
        // data produced before structured warnings existed
        ObjectMapper mapper = ConfigurableMapper.getMapper();
        InterfaceWarningDB w = mapper.readValue("{\"text\":\"old warning\"}", InterfaceWarningDB.class);
        assertEquals("old warning", w.getText());
        assertNull(w.getType());
        assertNull(w.getCount());
        assertNull(w.getAtomPairs());
        UniProtRefWarningDB u = mapper.readValue("{\"text\":\"old warning\"}", UniProtRefWarningDB.class);
        assertNull(u.getType());
    }

    @Test
    public void shouldReadUnknownWarningTypesAsUnknown() throws IOException {
        ObjectMapper mapper = ConfigurableMapper.getMapper();
        InterfaceWarningDB w = mapper.readValue("{\"text\":\"t\",\"type\":\"A_TYPE_FROM_THE_FUTURE\"}", InterfaceWarningDB.class);
        assertEquals(InterfaceWarningType.UNKNOWN, w.getType());
        UniProtRefWarningDB u = mapper.readValue("{\"text\":\"t\",\"type\":\"A_TYPE_FROM_THE_FUTURE\"}", UniProtRefWarningDB.class);
        assertEquals(UniProtRefWarningType.UNKNOWN, u.getType());
    }

    @Test
    public void shouldRoundTripStructuredWarnings() throws IOException {
        ObjectMapper mapper = ConfigurableMapper.getMapper();
        InterfaceWarningDB w = new InterfaceWarningDB(InterfaceWarningType.CLASHES, "Clashes found between: ...");
        w.setCount(3);
        AtomPairDB pair = new AtomPairDB();
        pair.setFirstResidue(new ResidueRefDB("A", 10, "10A", "LYS"));
        pair.setSecondResidue(new ResidueRefDB("B", 20, "20", "GLU"));
        pair.setFirstAtom("NZ");
        pair.setSecondAtom("OE1");
        pair.setDistance(1.2);
        w.setAtomPairs(List.of(pair));

        String json = mapper.writeValueAsString(w);
        assertTrue(json.contains("\"CLASHES\""));
        // no molecId and residues: NON_EMPTY inclusion should leave them out
        assertFalse(json.contains("molecId"));
        assertFalse(json.contains("residues"));

        InterfaceWarningDB read = mapper.readValue(json, InterfaceWarningDB.class);
        assertEquals(InterfaceWarningType.CLASHES, read.getType());
        assertEquals(Integer.valueOf(3), read.getCount());
        assertEquals(1, read.getAtomPairs().size());
        AtomPairDB readPair = read.getAtomPairs().get(0);
        assertEquals("10A", readPair.getFirstResidue().getPdbResidueNumber());
        assertEquals(Integer.valueOf(20), readPair.getSecondResidue().getResidueNumber());
        assertEquals("OE1", readPair.getSecondAtom());
        assertEquals(1.2, readPair.getDistance(), 0.0001);
    }
}
