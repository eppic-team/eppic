package eppic.commons.sequence;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class TestSiftsConnection {

    @Test
    public void testSifts() throws IOException, NoMatchFoundException {
        SiftsConnection siftsConnection = new SiftsConnection();

        Reader reader = new StringReader(
                "3sn6    A       P04896  1       380     None    394     1       394\n" +
                "3sn6    B       P54311  13      351     2       340     2       340\n" +
                "3sn6    G       P63212  1       68      None    None    1       68\n" +
                "3sn6    R       P00720  16      175     1002    None    2       161\n" +
                "3sn6    R       P07550  178     511     None    None    29      362\n" +
                "3sn6    R       P07550  512     514     None    None    363     365");
        siftsConnection.parsePdb2Uniprot(reader);

        List<SiftsFeature> features = siftsConnection.getMappings("3sn6", "A");

        assertEquals(1, features.size());

        assertEquals(1, features.get(0).getCifIntervalSet().size());

        features = siftsConnection.getMappings("3sn6", "R");

        assertEquals(3, features.size());

        assertEquals(1, features.get(0).getCifIntervalSet().size());
    }
}
