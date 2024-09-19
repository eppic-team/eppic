package eppic.rest.jobs;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestEmailMessageData {

    @Test
    public void testFindOccurencesSubstring() {
        String str = "hola%s adios%s";
        assertEquals(2, EmailMessageData.countOccurrencesSubstring("%s", str));

        str = "hola%s";
        assertEquals(1, EmailMessageData.countOccurrencesSubstring("%s", str));

        str = "hola";
        assertEquals(0, EmailMessageData.countOccurrencesSubstring("%s", str));
    }
}
