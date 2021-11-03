package eppic.db.mongoutils;

import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.IOException;

public class MongoDbStore {

    private static MongoDatabase mongoDb;

    public static void init(File configFile) throws IOException {
        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
        String connUri = propsReader.getMongoUri();

        mongoDb = MongoUtils.getMongoDatabase(propsReader.getDbName(), connUri);
    }

    public static MongoDatabase getMongoDb() {
        return mongoDb;
    }
}
