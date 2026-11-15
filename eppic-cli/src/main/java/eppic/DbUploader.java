package eppic;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.EntryDAO;
import eppic.db.dao.mongo.EntryDAOMongo;
import eppic.db.mongoutils.DbPropertiesReader;
import eppic.model.db.PdbInfoDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eppic.model.db.InterfaceResidueFeaturesDB;

/**
 * Writes the data model of a finished entry straight to the db, as an alternative to
 * serializing it to a zip file for a separate loading tool to pick up later.
 *
 * <p>The point of it is that the db record becomes the one commit point of an entry: it is
 * either in the db or it is not, with no intermediate state where results sit on a volume
 * and nothing knows about them. The equivalent file based path is
 * {@link eppic.db.loaders.UploadToDb}, which writes entries in the same way: through
 * {@link EntryDAO}, so that recomputing an entry replaces it.
 */
public class DbUploader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbUploader.class);

    private final EntryDAO entryDao;

    /**
     * @param dbConfigFile the mongo config file, i.e. what was passed in -G
     */
    public DbUploader(File dbConfigFile) throws EppicException {
        // -M does not imply -s, and the connection is otherwise only opened on the evol
        // scoring path (ChainEvolContextList.openConnections), so it can still be unset here
        if (MongoSettingsStore.getMongoSettings() == null) {
            try {
                DbPropertiesReader reader = new DbPropertiesReader(dbConfigFile);
                MongoSettingsStore.init(reader.getDbName(), reader.getMongoUri());
            } catch (IOException e) {
                throw new EppicException(e, "Could not init db config from file " + dbConfigFile
                        + ": " + e.getMessage(), true);
            }
        }
        // the settings hold a single MongoDatabase for the whole jvm, so all entries of a
        // multi-input run share one client and its connection pool
        MongoDatabase mongoDb = MongoSettingsStore.getMongoSettings().getMongoDatabase();
        entryDao = new EntryDAOMongo(mongoDb);
    }

    /**
     * Writes the given entry, replacing any data already in the db for it. An entry can already
     * be in the db: a task that is retried recomputes entries it already did, and a full
     * recompute goes over entries that previous runs loaded.
     */
    public void upload(PdbInfoDB pdbInfo, List<InterfaceResidueFeaturesDB> interfResFeatures)
            throws EppicException {

        String entryId = pdbInfo.getEntryId();
        long start = System.currentTimeMillis();
        try {
            entryDao.write(pdbInfo, interfResFeatures);
        } catch (DaoException e) {
            throw new EppicException(e, "Could not upload data for " + entryId + " to db: "
                    + e.getMessage(), true);
        }
        LOGGER.info("Done inserting {} to db. Time: {}ms", entryId, System.currentTimeMillis() - start);
    }
}
