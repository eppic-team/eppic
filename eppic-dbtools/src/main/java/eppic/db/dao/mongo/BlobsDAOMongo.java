package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.BlobsDao;
import eppic.db.dao.DaoException;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.BlobDB;
import eppic.model.db.BlobIdentifierDB;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;


public class BlobsDAOMongo implements BlobsDao {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public BlobsDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        Table tableMetadata = MongoUtils.getTableMetadataFromJpa(BlobDB.class);
        this.collectionName = tableMetadata.name();
    }

    @Override
    public void insert(BlobIdentifierDB blobId, byte[] blob) throws DaoException {
        try {
            BlobDB blobDB = new BlobDB(blobId, blob);
            MongoUtils.writeObject(mongoDb, collectionName, blobDB);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public byte[] get(BlobIdentifierDB blobId) throws DaoException {
        // TODO how to deal with field names without hard coding
        // note we have to use this instead of List.of because List.of doesn't allow nulls
        List<Object> idList = new ArrayList<>();
        idList.add(blobId.getJobId());
        idList.add(blobId.getType().toString());
        idList.add(blobId.getId());

        BlobDB blob = MongoUtils.findOne(mongoDb, collectionName,
                List.of("blobId.jobId", "blobId.type", "blobId.id"),
                idList,
                BlobDB.class);

        if (blob == null) {
            // TODO review if this results in a 404 eventually
            throw new DaoException("Could not find blob for blob identifier: " + blobId);
        }

        return blob.getBlob();
    }

    @Override
    public long remove(String entryId) throws DaoException {
        return MongoUtils.remove(mongoDb, collectionName, "blobId.jobId", List.of(entryId));
    }
}
