package eppic.db.mongoutils;

import com.mongodb.client.MongoDatabase;

public class MongoSettings {

    private final MongoDatabase mongoDatabase;
    private final String dbName;

    public MongoSettings(String dbName, String connUri) {
        this.dbName = dbName;
        mongoDatabase = MongoUtils.getMongoDatabase(dbName, connUri, MongoUtils.MONGO_USER_ENV_VAR, MongoUtils.MONGO_PASSWORD_ENV_VAR);
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public String getDbName() {
        return dbName;
    }
}
