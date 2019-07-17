package eppic.rest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eppic.db.dao.JobDAO;
import eppic.db.dao.jpa.JobDAOJpa;
import eppic.rest.commons.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UtilService {

    private static final Logger logger = LoggerFactory.getLogger(UtilService.class);

    public static ObjectNode getInfo() {
        InetAddress ip;
        String hostname = "unknown";
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();

        } catch (UnknownHostException e) {
            logger.warn("Exception occurred while getting hostname. Will not be able to report host name. Error: {}" , e.getMessage());
        }

        ObjectNode jsonObj = new ObjectMapper().createObjectNode();

        jsonObj.put("hostname", hostname);
        jsonObj.put("buildHash", AppConstants.PROJECT_SHA);
        jsonObj.put("applicationVersion", AppConstants.PROJECT_VERSION);
        jsonObj.put("apiVersion", AppConstants.MAJOR_VERSION);

        return jsonObj;
    }

    public static boolean isDbHealthy() {
        JobDAO jobDAO = new JobDAOJpa();
        boolean isHealthy = !jobDAO.isJobsEmpty();
        if (!isHealthy) {
            logger.error("DB is either unreachable or empty");
        }
        return isHealthy;
    }

    public static boolean isTempDiskHealthy() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));

        long space = tmpDir.getUsableSpace();
        if (space < 1000000) { // below 1 MB
            logger.error("Java is reporting very low disk usable space in {} dir: only {} bytes available", tmpDir, space);
            return false;
        } else {
            logger.debug("Usable space is {}", space);
        }

        return true;
    }
}
