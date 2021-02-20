package eppic;

import eppic.db.mongoutils.MongoSettings;

/**
 * A singleton to hold the MongoSettings throughout app runtime
 */
public class MongoSettingsStore {

    private static MongoSettings mongoSettings;

    public static void init(String dbName, String connUri) {
        mongoSettings = new MongoSettings(dbName, connUri);
    }

    public static MongoSettings getMongoSettings() {
        return mongoSettings;
    }
}
