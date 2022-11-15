package eppic.db.mongoutils;

import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.IOException;

public class MongoDbStore {

    private static MongoDatabase mongoDb;

    private static MongoDatabase mongoDbUserJobs;

    public static void init(File configFile) throws IOException {
        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);

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
