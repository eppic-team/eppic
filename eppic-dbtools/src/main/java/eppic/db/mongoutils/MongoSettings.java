package eppic.db.mongoutils;

import com.mongodb.client.MongoDatabase;

public class MongoSettings {

    private final MongoDatabase mongoDatabase;
    private final String dbName;

    public MongoSettings(String dbName, String connUri) {
        this.dbName = dbName;
        mongoDatabase = MongoUtils.getMongoDatabase(dbName, connUri);
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public String getDbName() {
        return dbName;
    }
}
