package eppic.db.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DbPropertiesReader {

    private final Properties properties;

    public DbPropertiesReader(File configurationFile) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(configurationFile));
    }

    public String getMongoUri() {

        if (properties.getProperty("mongo.uri")!=null && !properties.getProperty("mongo.uri").isEmpty())
            return properties.getProperty("mongo.uri").trim();

        else
            return null;

    }
}
