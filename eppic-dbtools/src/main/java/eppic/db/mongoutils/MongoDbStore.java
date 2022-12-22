package eppic.db.mongoutils;

import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MongoDbStore {

    private static MongoDatabase mongoDb;

    private static MongoDatabase mongoDbUserJobs;

    public static void init(File configFile) throws IOException {
        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);

        mongoDb = MongoUtils.getMongoDatabase(propsReader.getDbName(), propsReader.getMongoUri());

        mongoDbUserJobs = MongoUtils.getMongoDatabase(propsReader.getDbNameUserJobs(), propsReader.getMongoUriUserJobs());
    }

    public static void init(InputStream configInputStream) throws IOException {
        DbPropertiesReader propsReader = new DbPropertiesReader(configInputStream);

        mongoDb = MongoUtils.getMongoDatabase(propsReader.getDbName(), propsReader.getMongoUri());

        mongoDbUserJobs = MongoUtils.getMongoDatabase(propsReader.getDbNameUserJobs(), propsReader.getMongoUriUserJobs());
    }

    public static MongoDatabase getMongoDb() {
        return mongoDb;
    }

    public static MongoDatabase getMongoDbUserJobs() {
        return mongoDbUserJobs;
    }
}
