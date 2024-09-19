package eppic.rest.commons;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "build")
@ConfigurationPropertiesScan
public class BuildProperties {

    private String projectMajorVersion;
    private String projectVersion;
    private String hash;
    private String timestamp;


    public String getProjectMajorVersion() {
        return projectMajorVersion;
    }

    public void setProjectMajorVersion(String projectMajorVersion) {
        this.projectMajorVersion = projectMajorVersion;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
