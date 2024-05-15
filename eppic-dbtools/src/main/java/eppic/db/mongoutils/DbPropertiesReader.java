package eppic.db.mongoutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbPropertiesReader {

    public static final String DEFAULT_CONFIG_FILE_NAME = "eppic-db.properties";
    public static final File DEFAULT_CONFIG_FILE = new File(System.getProperty("user.home"), DEFAULT_CONFIG_FILE_NAME);

    private final Properties properties;

    public DbPropertiesReader(File configurationFile) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(configurationFile));
    }

    public DbPropertiesReader(InputStream inputStream) throws IOException {
        properties = new Properties();
        properties.load(inputStream);
    }


    public String getMongoUri() {
        String val = properties.getProperty("eppic-rest.mongo-uri");
        if (val!=null && !val.isEmpty())
            return val.trim();
        return null;
    }

    public String getMongoUriUserJobs() {
        String val = properties.getProperty("eppic-rest.mongo-uri-userjobs");
        if (val!=null && !val.isEmpty())
            return val.trim();
        return null;
    }

    public String getDbName() {
        String val = properties.getProperty("eppic-rest.db-name");
        if (val!=null && !val.isEmpty()) {
            return val.trim();
        }
        return null;
    }

    public String getDbNameUserJobs() {
        String val = properties.getProperty("eppic-rest.db-name-userjobs");
        if (val!=null && !val.isEmpty()) {
            return val.trim();
        }
        return null;
    }
}
