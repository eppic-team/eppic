package eppic.rest.service;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestUtilService {

    @Test
    public void testIsDiskHealthy() {
        boolean tmpHealthy = UtilService.isTempDiskHealthy();

        assertTrue(tmpHealthy);
    }
}
