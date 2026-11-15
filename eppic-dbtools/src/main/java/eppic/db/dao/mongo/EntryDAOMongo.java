package eppic.db.dao.mongo;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.EntryDAO;
import eppic.db.loaders.EntryData;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.AssemblyDB;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes entries to the db. An entry is these documents:
 * <ul>
 * <li>one in the PdbInfo collection, the {@link PdbInfoDB} without its assemblies</li>
 * <li>one per assembly in the Assembly collection (see {@link AssemblyDB})</li>
 * <li>one per interface in the InterfaceResidueFeatures collection</li>
 * </ul>
 * The PdbInfo document is what makes an entry exist: it is what readers look for first. So it
 * is always the last one to be written and the first one to be removed, and thus an entry that
 * can be read is complete, whatever happens while writing.
 * <p>
 * On top of that, if the MongoDB deployment supports transactions (a replica set or a sharded
 * cluster) then {@link #write(PdbInfoDB, List)} and {@link #remove(String)} happen in one: readers
 * see either the old entry or the new one, and a failed write leaves the old one in place.
 * Without transactions a failed write leaves no entry, and while an entry is being replaced
 * there is a moment in which it does not exist.
 */
public class EntryDAOMongo implements EntryDAO {

    private static final Logger logger = LoggerFactory.getLogger(EntryDAOMongo.class);

    static final String ENTRY_ID_FIELD = "entryId";
    /**
     * The field of PdbInfoDB with the assemblies, which is not present in the PdbInfo documents
     */
    static final String ASSEMBLIES_FIELD = "assemblies";
    /**
     * The position of the assembly in the list of assemblies of its entry. It exists only in the
     * assembly documents, together with the entry id.
     */
    static final String ASSEMBLY_POSITION_FIELD = "position";

    private static final int DUPLICATE_KEY_ERROR_CODE = 11000;

    private final MongoClient mongoClient;
    private final MongoCollection<Document> pdbInfoCollection;
    private final MongoCollection<Document> assemblyCollection;
    private final MongoCollection<Document> interfResFeaturesCollection;
    private final MongoDatabase mongoDb;

    private boolean initialised = false;
    private boolean transactional = false;

    /**
     * @param mongoDb the db. If it was not obtained from
     * {@link MongoUtils#getMongoDatabase(String, String, String, String)} then transactions are not used
     */
    public EntryDAOMongo(MongoDatabase mongoDb) {
        this(MongoUtils.getMongoClient(mongoDb), mongoDb);
    }

    /**
     * @param mongoClient the client that the db was obtained from, null for not using transactions
     * @param mongoDb the db
     */
    public EntryDAOMongo(MongoClient mongoClient, MongoDatabase mongoDb) {
        this.mongoClient = mongoClient;
        this.mongoDb = mongoDb;
        pdbInfoCollection = mongoDb.getCollection(MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class).name());
        assemblyCollection = mongoDb.getCollection(MongoUtils.getTableMetadataFromJpa(AssemblyDB.class).name());
        interfResFeaturesCollection = mongoDb.getCollection(MongoUtils.getTableMetadataFromJpa(InterfaceResidueFeaturesDB.class).name());
    }

    @Override
    public void write(PdbInfoDB pdbInfo, List<InterfaceResidueFeaturesDB> interfResFeatures) throws DaoException {
        String entryId = pdbInfo.getEntryId();
        try {
            init();
            EntryDocuments docs = new EntryDocuments();
            docs.add(pdbInfo, interfResFeatures);

            if (transactional) {
                try (ClientSession session = mongoClient.startSession()) {
                    session.withTransaction(() -> {
                        remove(session, entryId);
                        insert(session, docs);
                        return null;
                    });
                }
            } else {
                remove(null, entryId);
                try {
                    insert(null, docs);
                } catch (Exception e) {
                    removeWithoutPdbInfo(List.of(entryId));
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void insertAll(List<EntryData> entries) throws DaoException {
        if (entries.isEmpty()) {
            return;
        }
        List<String> entryIds = entries.stream().map(e -> e.getPdbInfoDB().getEntryId()).toList();
        try {
            init();
            EntryDocuments docs = new EntryDocuments();
            for (EntryData entry : entries) {
                docs.add(entry.getPdbInfoDB(), entry.getInterfResFeaturesDB());
            }
            try {
                insert(null, docs);
            } catch (Exception e) {
                // A duplicate key means that one of the entries was in the db already: in that case
                // the documents in the db can be the ones of the entry that was there, so they must
                // stay. In any other case what is left behind are documents of entries that were
                // not inserted, which nothing would read but which would make a new attempt fail.
                if (!isDuplicateKeyError(e)) {
                    removeWithoutPdbInfo(entryIds);
                }
                throw e;
            }
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public boolean remove(String entryId) throws DaoException {
        try {
            init();
            if (transactional) {
                try (ClientSession session = mongoClient.startSession()) {
                    return session.withTransaction(() -> remove(session, entryId));
                }
            }
            return remove(null, entryId);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    private boolean remove(ClientSession session, String entryId) {
        Document query = new Document(ENTRY_ID_FIELD, entryId);
        // the PdbInfo document first: without it the entry does not exist anymore
        long removed = 0;
        for (MongoCollection<Document> collection : List.of(pdbInfoCollection, assemblyCollection, interfResFeaturesCollection)) {
            long count = session == null ?
                    collection.deleteMany(query).getDeletedCount() :
                    collection.deleteMany(session, query).getDeletedCount();
            if (collection == pdbInfoCollection) {
                removed = count;
            }
        }
        return removed > 0;
    }

    private void insert(ClientSession session, EntryDocuments docs) {
        // the PdbInfo documents last: they are what makes the entries exist
        insert(session, assemblyCollection, docs.assemblies);
        insert(session, interfResFeaturesCollection, docs.interfResFeatures);
        insert(session, pdbInfoCollection, docs.pdbInfos);
    }

    private static void insert(ClientSession session, MongoCollection<Document> collection, List<Document> docs) {
        if (docs.isEmpty()) {
            return;
        }
        if (session == null) {
            collection.insertMany(docs);
        } else {
            collection.insertMany(session, docs);
        }
    }

    /**
     * Removes the documents of those of the given entries that have no PdbInfo document, i.e.
     * what a failed insertion left behind. It doesn't throw: this is called when dealing with
     * a failure already, which is the one to report.
     */
    private void removeWithoutPdbInfo(List<String> entryIds) {
        try {
            List<String> withoutPdbInfo = new ArrayList<>(entryIds);
            pdbInfoCollection.find(new Document(ENTRY_ID_FIELD, new Document("$in", entryIds)))
                    .projection(new Document(ENTRY_ID_FIELD, 1))
                    .forEach(doc -> withoutPdbInfo.remove(doc.getString(ENTRY_ID_FIELD)));
            if (!withoutPdbInfo.isEmpty()) {
                Document query = new Document(ENTRY_ID_FIELD, new Document("$in", withoutPdbInfo));
                assemblyCollection.deleteMany(query);
                interfResFeaturesCollection.deleteMany(query);
            }
        } catch (Exception e) {
            logger.warn("Could not remove the documents left behind by the failed insertion of {}: {}", entryIds, e.getMessage());
        }
    }

    /**
     * Done before the first write: makes sure that the indices exist and finds out if
     * transactions can be used.
     */
    private synchronized void init() {
        if (initialised) {
            return;
        }
        // Reading the documents of an entry must not be a scan of a whole collection, so the
        // indices are not left to whoever sets up the db. This also creates the collections,
        // which in older MongoDB versions can't be done within a transaction.
        for (Class<?> modelClass : List.of(PdbInfoDB.class, AssemblyDB.class, InterfaceResidueFeaturesDB.class)) {
            try {
                if (!MongoUtils.hasExpectedIndices(mongoDb, modelClass)) {
                    logger.info("Creating indices in db {} for {}", mongoDb.getName(), modelClass.getSimpleName());
                    MongoUtils.createIndices(mongoDb, modelClass);
                }
            } catch (MongoException e) {
                logger.warn("Could not create indices in db {} for {}: {}", mongoDb.getName(), modelClass.getSimpleName(), e.getMessage());
            }
        }
        transactional = mongoClient != null && supportsTransactions();
        if (!transactional) {
            logger.info("Entries will be written to db {} without transactions{}", mongoDb.getName(),
                    mongoClient == null ? "" : ": it is not a replica set or a sharded cluster");
        }
        initialised = true;
    }

    private boolean supportsTransactions() {
        Document hello = mongoDb.runCommand(new Document("hello", 1));
        return hello.containsKey("setName") || "isdbgrid".equals(hello.get("msg"));
    }

    private static boolean isDuplicateKeyError(Exception e) {
        if (e instanceof MongoBulkWriteException bulkEx) {
            return bulkEx.getWriteErrors().stream().anyMatch(we -> we.getCode() == DUPLICATE_KEY_ERROR_CODE);
        }
        return e instanceof MongoException mongoEx && mongoEx.getCode() == DUPLICATE_KEY_ERROR_CODE;
    }

    /**
     * The documents to write for some entries, per collection
     */
    private static class EntryDocuments {
        final List<Document> pdbInfos = new ArrayList<>();
        final List<Document> assemblies = new ArrayList<>();
        final List<Document> interfResFeatures = new ArrayList<>();

        void add(PdbInfoDB pdbInfo, List<InterfaceResidueFeaturesDB> features) {
            Document pdbInfoDoc = MongoUtils.convertValueToDocument(pdbInfo);
            // the assemblies are taken out of the PdbInfo document to be documents themselves
            Object assembliesInDoc = pdbInfoDoc.remove(ASSEMBLIES_FIELD);
            if (assembliesInDoc != null) {
                int position = 0;
                for (Object assembly : (List<?>) assembliesInDoc) {
                    Document assemblyDoc = new Document(ENTRY_ID_FIELD, pdbInfo.getEntryId()).append(ASSEMBLY_POSITION_FIELD, position++);
                    for (Map.Entry<?, ?> field : ((Map<?, ?>) assembly).entrySet()) {
                        assemblyDoc.append((String) field.getKey(), field.getValue());
                    }
                    assemblies.add(assemblyDoc);
                }
            }
            pdbInfos.add(pdbInfoDoc);
            if (features != null) {
                for (InterfaceResidueFeaturesDB feature : features) {
                    interfResFeatures.add(MongoUtils.convertValueToDocument(feature));
                }
            }
        }
    }
}
