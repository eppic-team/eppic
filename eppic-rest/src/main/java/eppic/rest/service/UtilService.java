package eppic.rest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eppic.db.dao.JobDAO;
import eppic.db.dao.mongo.JobDAOMongo;
import eppic.rest.commons.AppConstants;
import eppic.rest.commons.BuildProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class UtilService {

    private static final Logger logger = LoggerFactory.getLogger(UtilService.class);

    private static final long DISK_SPACE_LEFT_FOR_WARNING = 10000 * 1000; // 10 MB

    private final BuildProperties buildProperties;

    @Autowired
    public UtilService(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public ObjectNode getInfo() {
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
        jsonObj.put("buildHash", buildProperties.getHash());
        jsonObj.put("applicationVersion", buildProperties.getProjectVersion());
        jsonObj.put("apiVersion", buildProperties.getProjectMajorVersion());

        return jsonObj;
    }

    public boolean isDbHealthy() {
        JobDAO jobDAO = new JobDAOMongo();
        boolean isHealthy = !jobDAO.isJobsEmpty();
        if (!isHealthy) {
            logger.error("DB is either unreachable or empty");
        }
        return isHealthy;
    }

    public boolean isTempDiskHealthy() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));

        long space = tmpDir.getUsableSpace();
        if (space < DISK_SPACE_LEFT_FOR_WARNING) {
            logger.error("Java is reporting very low disk usable space in {} dir: only {} bytes available", tmpDir, space);
            return false;
        } else {
            logger.debug("Usable space is {}", space);
        }

        return true;
    }
}
