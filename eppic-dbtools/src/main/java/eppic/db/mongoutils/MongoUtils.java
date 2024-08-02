package eppic.db.mongoutils;

import com.mongodb.client.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Index;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Projections.excludeId;

public class MongoUtils {

    private static final Logger logger = LoggerFactory.getLogger(MongoUtils.class);

    /**
     * Writes a single {@link Object} to a {@param collectionName} collection in the given {@param mongoDatabase}.
     *
     *  @param mongoDatabase destination {@link MongoDatabase}
     *  @param collectionName destination {@link com.mongodb.client.MongoCollection} name
     *  @param object {@link Object}
     */
    public static <T> void writeObject(MongoDatabase mongoDatabase, String collectionName, T object) {

        Document document = convertValueToDocument(object);

        try {
            mongoDatabase.getCollection(collectionName).insertOne(document);
        } catch (MongoWriteException e) {
            logger.error("{}: {}. Error category: {}", e.getError().getMessage(), document.toJson(), e.getError().getCategory().name());
            throw e;
        }
    }

    public static <T> void writeObjects(MongoDatabase mongoDatabase, String collectionName, List<T> objects) {
        List<Document> documents = objects.stream().map(MongoUtils::convertValueToDocument).collect(Collectors.toList());

        try {
            mongoDatabase.getCollection(collectionName).insertMany(documents);
        } catch (MongoWriteException e) {
            logger.error("{}. Error category: {}", e.getError().getMessage(), e.getError().getCategory().name());
            throw e;
        }
    }

    /**
     * Uses an object mapper {@link ConfigurableMapper#getMapper()} to
     * convert an {@link Object} to a {@link Document}.
     *
     * @param o input {@link Object}.
     * @return an instance of {@link Document}.
     */
    public static Document convertValueToDocument(Object o) {

        if ( !(o instanceof Document) )
            return ConfigurableMapper.getMapper().convertValue(o, Document.class);
        else
            return (Document) o;
    }

    /**
     * Reads configuration properties and create a connection to Mongo database using specified connection URI.
     *
     * @param dbName the Mongo database name
     * @param connUriStr the Mongo connection URI
     * @return {@link com.mongodb.client.MongoDatabase} database
     */
    public static MongoDatabase getMongoDatabase(String dbName, String connUriStr) {

        MongoClient mongoClient;
        try {
            mongoClient = MongoClients.create(connUriStr);
        } catch (Exception e) {
            logger.error("Cannot connect to mongodb using connection parameters: {}", connUriStr);
            throw e;
        }

        if (dbName == null)
            throw new IllegalArgumentException("dbName must not be null");

        return mongoClient.getDatabase(dbName);
    }

    public static void dropCollection(MongoDatabase mongoDb, Class<?> modelClass) {
        mongoDb.getCollection(getTableMetadataFromJpa(modelClass).name()).drop();
    }

    /**
     * Create Mongo indices by reading jpa Table metadata from given model class.
     * @param mongoDb the mongo db
     * @param modelClass the model class with jpa Table  annotation
     */
    public static void createIndices(MongoDatabase mongoDb, Class<?> modelClass) {
        Table tableMetadata = getTableMetadataFromJpa(modelClass);
        for (Index index : tableMetadata.indexes()) {
            final String[] fieldList = index.columnList().split(",");
            mongoDb.getCollection(tableMetadata.name())
                    .createIndex(ascending(fieldList), new IndexOptions()
                            .unique(index.unique()).name(index.name()));
        }
    }

    public static boolean hasExpectedIndices(MongoDatabase mongoDb, Class<?> modelClass) {
        Table tableMetadata = getTableMetadataFromJpa(modelClass);
        Set<String> existingIndices = new HashSet<>();
        for (Document doc : mongoDb.getCollection(tableMetadata.name()).listIndexes()) {
            existingIndices.add(doc.getString("name"));
        }
        for (Index index : tableMetadata.indexes()) {
            if (!existingIndices.contains(index.name())) {
                return false;
            }
        }
        return true;
    }

    public static Table getTableMetadataFromJpa(Class<?> modelClass) {
        return modelClass.getAnnotation(Table.class);
    }

    public static <T> T findOne(MongoDatabase mongoDb, String collectionName, List<String> idFields, List<Object> id, Class<T> modelClass) {
        Bson q = getIdentifierQuery(idFields, id);
        return ConfigurableMapper.getMapper().convertValue(mongoDb.getCollection(collectionName).find(q)
                .projection(excludeId()).first(), modelClass);
    }

    public static <T> T findFirst(MongoDatabase mongoDb, String collectionName, Class<T> modelClass) {
        return ConfigurableMapper.getMapper().convertValue(mongoDb.getCollection(collectionName).find()
                .projection(excludeId()).first(), modelClass);
    }

//    public List<Document> findAll(MongoDatabase mongoDb, String collectionName, List<String> id) {
//        Bson q = getIdentifierQuery(id);
//        return mongoDb.getCollection(collectionName).find(q)
//                .projection(excludeId()).into(new ArrayList<>());
//    }

    public static <T> List<T> findAll(MongoDatabase mongoDb, String collectionName, List<String> idFields, List<Object> id, Class<T> modelClass) {
        Bson q = getIdentifierQuery(idFields, id);
        return mongoDb.getCollection(collectionName).find(q)
                .projection(excludeId())
                .map(doc -> ConfigurableMapper.getMapper().convertValue(doc, modelClass))
                .into(new ArrayList<>());
    }

    public static <T> List<T> findAll(MongoDatabase mongoDb, String collectionName, Class<T> modelClass) {
        return mongoDb.getCollection(collectionName).find()
                .projection(excludeId())
                .map(doc -> ConfigurableMapper.getMapper().convertValue(doc, modelClass))
                .into(new ArrayList<>());
    }

    public static Bson getIdentifierQuery(List<String> idFields, List<Object> id) {

        if (id.size() != idFields.size())
            throw new IllegalArgumentException("Size of id must coincide with size of idFields");

        List<Bson> list = new ArrayList<>();
        for (int i =0; i < id.size(); i++) {
            list.add(new Document(idFields.get(i), id.get(i)));
        }
        return new Document("$and", list);
    }

    public static boolean isCollectionEmpty(MongoDatabase mongoDb, Class<?> modelClass) {
        long count = mongoDb.getCollection(getTableMetadataFromJpa(modelClass).name()).countDocuments();
        return count == 0;
    }

    public static long remove(MongoDatabase mongoDb, String collectionName, String field, List<String> ids) {
        List<Document> allIds = ids.stream().map(id -> new Document(field, id)).toList();
        Bson q = new Document("$or", allIds);
        DeleteResult deleteResult = mongoDb.getCollection(collectionName).deleteMany(q);
        return deleteResult.getDeletedCount();
    }

    public static long countDocuments(MongoDatabase mongoDb, String collectionName) {
        return mongoDb.getCollection(collectionName).countDocuments();
    }

    // TODO finish up. This only gets stats for whole db
    // https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples/command/
    public static long getSize(MongoDatabase mongoDb, String collectionName) {
        Bson command = new BsonDocument("dbStats", new BsonInt64(1));
        Document doc = mongoDb.runCommand(command);

        return 0; // TODO implement
    }

}
