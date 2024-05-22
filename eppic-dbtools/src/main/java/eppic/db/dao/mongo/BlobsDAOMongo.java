package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.BlobsDao;
import eppic.db.dao.DaoException;
import eppic.model.db.BlobIdentifierDB;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Projections.excludeId;

public class BlobsDAOMongo implements BlobsDao {

    private final MongoDatabase mongoDb;
    private final String collectionName;

    public BlobsDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        this.collectionName = BlobIdentifierDB.BLOBS_COLLECTION_NAME;
    }

    @Override
    public void insert(BlobIdentifierDB blobId, byte[] blob) throws DaoException {
        Document document = new Document();
        document.put(BlobIdentifierDB.BLOBS_ID_KEY, blobId);
        document.put(BlobIdentifierDB.BLOB_KEY, blob);
        mongoDb.getCollection(collectionName).insertOne(document);
    }

    @Override
    public byte[] get(BlobIdentifierDB blobId) throws DaoException {
        // TODO use reflection to get field names from model
        List<Bson> list = new ArrayList<>();
        list.add(new Document(BlobIdentifierDB.BLOBS_ID_KEY + ".jobId", blobId.getJobId()));
        list.add(new Document(BlobIdentifierDB.BLOBS_ID_KEY + ".type", blobId.getType()));
        list.add(new Document(BlobIdentifierDB.BLOBS_ID_KEY + ".id", blobId.getId()));

        Bson bsonQuery = new Document("$and", list);

        Document doc = mongoDb.getCollection(collectionName).find(bsonQuery)
                .projection(excludeId()).first();
        if (doc == null) {
            // TODO review if this results in a 404 eventually
            throw new DaoException("Could not find blob for blob identifier: " + blobId);
        }
        Binary bin = doc.get("blob", org.bson.types.Binary.class);
        return bin.getData();
    }
}
