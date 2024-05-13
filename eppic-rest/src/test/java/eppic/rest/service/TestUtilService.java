package eppic.rest.service;

import eppic.rest.commons.BuildProperties;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestUtilService {

    @Test
    public void testIsDiskHealthy() {
        UtilService utilService = new UtilService(new BuildProperties());
        boolean tmpHealthy = utilService.isTempDiskHealthy();

        assertTrue(tmpHealthy);
    }
}
