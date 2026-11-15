package eppic.db.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.mongoutils.ConfigurableMapper;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.AssemblyDB;
import eppic.model.db.PdbInfoDB;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Projections.excludeId;

/**
 * The entries in the db. An entry is the {@link PdbInfoDB} object with everything that hangs from
 * it, but it is not stored as a single document: its assemblies go to a collection of their own,
 * one document per assembly (see {@link AssemblyDB}). That is purely a matter of how the data are
 * laid out in the db: what is written and what is read here is always the complete PdbInfoDB.
 * <p>
 * The PdbInfo document is what makes an entry exist: the assembly documents are written before it
 * and removed after it, so a PdbInfo document that can be read always has its assemblies.
 */
public class PDBInfoDAOMongo implements PDBInfoDAO {

    private static final Logger logger = LoggerFactory.getLogger(PDBInfoDAOMongo.class);

    private static final String ENTRY_ID_FIELD = "entryId";
    /**
     * The field of PdbInfoDB with the assemblies, which is not present in the PdbInfo documents
     */
    private static final String ASSEMBLIES_FIELD = "assemblies";
    /**
     * The position of the assembly in the list of assemblies of its entry. It exists only in the
     * assembly documents, together with the entry id.
     */
    private static final String ASSEMBLY_POSITION_FIELD = "position";

    private static final int DUPLICATE_KEY_ERROR_CODE = 11000;

    private final MongoDatabase mongoDb;
    private final String collectionName;
    private final String assemblyCollectionName;

    private boolean assemblyIndicesChecked = false;

    public PDBInfoDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        this.collectionName = MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class).name();
        this.assemblyCollectionName = MongoUtils.getTableMetadataFromJpa(AssemblyDB.class).name();
    }

    @Override
    public PdbInfoDB getPDBInfo(String entryId) throws DaoException {
        PdbInfoDB pdbInfoDB;
        try {
            pdbInfoDB = MongoUtils.findOne(mongoDb, collectionName,
                    Collections.singletonList(ENTRY_ID_FIELD),
                    Collections.singletonList(entryId),
                    PdbInfoDB.class);
            if (pdbInfoDB != null) {
                readAssemblies(pdbInfoDB);
            }
        } catch (Exception e) {
            throw new DaoException(e);
        }

        if (pdbInfoDB == null) {
            throw new NoResultException("Could not find PDBInfo data for id " + entryId);
        }
        return pdbInfoDB;
    }

    @Override
    public void insertPDBInfo(PdbInfoDB pdbInfo) throws DaoException {
        insertPDBInfos(List.of(pdbInfo));
    }

    @Override
    public void insertPDBInfos(List<PdbInfoDB> pdbInfos) throws DaoException {
        if (pdbInfos.isEmpty()) {
            return;
        }
        List<Document> pdbInfoDocs = new ArrayList<>();
        List<Document> assemblyDocs = new ArrayList<>();
        try {
            for (PdbInfoDB pdbInfo : pdbInfos) {
                Document pdbInfoDoc = MongoUtils.convertValueToDocument(pdbInfo);
                assemblyDocs.addAll(splitAssemblies(pdbInfo.getEntryId(), pdbInfoDoc));
                pdbInfoDocs.add(pdbInfoDoc);
            }
            ensureAssemblyIndices();
        } catch (Exception e) {
            throw new DaoException(e);
        }

        try {
            // the assemblies first: the PdbInfo document is what makes the entry exist
            if (!assemblyDocs.isEmpty()) {
                mongoDb.getCollection(assemblyCollectionName).insertMany(assemblyDocs);
            }
            mongoDb.getCollection(collectionName).insertMany(pdbInfoDocs);
        } catch (Exception e) {
            // A duplicate key means that one of the entries was in the db already: in that case the
            // assemblies in the db can be the ones of the entry that was there, so they must stay.
            // In any other case what is left behind are the assemblies of entries that were not
            // written, which nothing would read but which would make a new attempt fail.
            if (!isDuplicateKeyError(e)) {
                List<String> entryIds = pdbInfos.stream().map(PdbInfoDB::getEntryId).toList();
                try {
                    removeNotInserted(entryIds);
                } catch (Exception e2) {
                    logger.warn("Could not remove the assembly documents left behind by the failed insertion of {}: {}", entryIds, e2.getMessage());
                }
            }
            throw new DaoException(e);
        }
    }

    /**
     * Gets all the entries, without their assemblies: this is for going over the whole
     * collection, where reading the assemblies of every entry would be a lot of data.
     * @return list of PdbInfoDB objects, with no assemblies
     */
    @Override
    public List<PdbInfoDB> getAll() throws DaoException {
       List<PdbInfoDB> list;
       try {
           list = mongoDb.getCollection(collectionName).find()
                   .projection(new Document("_id", 0).append(ASSEMBLIES_FIELD, 0))
                   .map(doc -> ConfigurableMapper.getMapper().convertValue(doc, PdbInfoDB.class))
                   .into(new ArrayList<>());
       } catch (Exception e) {
           throw new DaoException(e);
       }
       return list;
    }

    @Override
    public long remove(String entryId) throws DaoException {
        try {
            // the PdbInfo document first: without it the entry does not exist anymore
            long removed = MongoUtils.remove(mongoDb, collectionName, ENTRY_ID_FIELD, List.of(entryId));
            MongoUtils.remove(mongoDb, assemblyCollectionName, ENTRY_ID_FIELD, List.of(entryId));
            return removed;
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    /**
     * Takes the assemblies out of the given PdbInfo document and returns them as the documents
     * to write to the assemblies collection.
     */
    private static List<Document> splitAssemblies(String entryId, Document pdbInfoDoc) {
        List<Document> assemblyDocs = new ArrayList<>();
        Object assemblies = pdbInfoDoc.remove(ASSEMBLIES_FIELD);
        if (assemblies == null) {
            return assemblyDocs;
        }
        int position = 0;
        for (Object assembly : (List<?>) assemblies) {
            Document assemblyDoc = new Document(ENTRY_ID_FIELD, entryId).append(ASSEMBLY_POSITION_FIELD, position++);
            for (Map.Entry<?, ?> field : ((Map<?, ?>) assembly).entrySet()) {
                assemblyDoc.append((String) field.getKey(), field.getValue());
            }
            assemblyDocs.add(assemblyDoc);
        }
        return assemblyDocs;
    }

    /**
     * Reads the assemblies of the given entry from their collection and puts them in it.
     * An entry that was written before the assemblies had a collection of their own has them in
     * its PdbInfo document, so it comes with them already and there is nothing to read.
     */
    private void readAssemblies(PdbInfoDB pdbInfo) {
        if (pdbInfo.getAssemblies() != null && !pdbInfo.getAssemblies().isEmpty()) {
            return;
        }
        List<AssemblyDB> assemblies = new ArrayList<>();
        MongoCollection<Document> assemblyCollection = mongoDb.getCollection(assemblyCollectionName);
        for (Document assemblyDoc : assemblyCollection.find(new Document(ENTRY_ID_FIELD, pdbInfo.getEntryId()))
                .sort(Sorts.ascending(ASSEMBLY_POSITION_FIELD))
                .projection(excludeId())) {
            assemblyDoc.remove(ENTRY_ID_FIELD);
            assemblyDoc.remove(ASSEMBLY_POSITION_FIELD);
            AssemblyDB assembly = ConfigurableMapper.getMapper().convertValue(assemblyDoc, AssemblyDB.class);
            // the same back reference that deserializing the assemblies within their PdbInfoDB sets
            assembly.setPdbInfo(pdbInfo);
            assemblies.add(assembly);
        }
        pdbInfo.setAssemblies(assemblies);
    }

    /**
     * Removes the assembly documents of those of the given entries that have no PdbInfo document
     */
    private void removeNotInserted(List<String> entryIds) {
        List<String> notInserted = new ArrayList<>(entryIds);
        mongoDb.getCollection(collectionName)
                .find(new Document(ENTRY_ID_FIELD, new Document("$in", entryIds)))
                .projection(new Document(ENTRY_ID_FIELD, 1))
                .forEach(doc -> notInserted.remove(doc.getString(ENTRY_ID_FIELD)));
        if (!notInserted.isEmpty()) {
            MongoUtils.remove(mongoDb, assemblyCollectionName, ENTRY_ID_FIELD, notInserted);
        }
    }

    /**
     * The index is what guarantees that reading the assemblies of an entry is not a scan of the
     * whole collection, so it is not left to whoever sets up the db: it is created here if needed,
     * before the first write.
     */
    private synchronized void ensureAssemblyIndices() {
        if (assemblyIndicesChecked) {
            return;
        }
        if (!MongoUtils.hasExpectedIndices(mongoDb, AssemblyDB.class)) {
            logger.info("Creating indices for collection {}", assemblyCollectionName);
            MongoUtils.createIndices(mongoDb, AssemblyDB.class);
        }
        assemblyIndicesChecked = true;
    }

    private static boolean isDuplicateKeyError(Exception e) {
        if (e instanceof com.mongodb.MongoBulkWriteException bulkEx) {
            return bulkEx.getWriteErrors().stream().anyMatch(we -> we.getCode() == DUPLICATE_KEY_ERROR_CODE);
        }
        return e instanceof MongoException mongoEx && mongoEx.getCode() == DUPLICATE_KEY_ERROR_CODE;
    }
}
