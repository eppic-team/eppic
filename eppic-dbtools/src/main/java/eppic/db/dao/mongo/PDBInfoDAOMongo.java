package eppic.db.dao.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.mongoutils.ConfigurableMapper;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.AssemblyDB;
import eppic.model.db.PdbInfoDB;
import org.bson.Document;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Projections.excludeId;
import static eppic.db.dao.mongo.EntryDAOMongo.ASSEMBLIES_FIELD;
import static eppic.db.dao.mongo.EntryDAOMongo.ASSEMBLY_POSITION_FIELD;
import static eppic.db.dao.mongo.EntryDAOMongo.ENTRY_ID_FIELD;

/**
 * Reads the {@link PdbInfoDB} of the entries in the db. A PdbInfoDB is not stored as a single
 * document: its assemblies are in a collection of their own, one document per assembly (see
 * {@link EntryDAOMongo}, which is what writes them). That is purely a matter of how the data are
 * laid out in the db: what is read here is the complete PdbInfoDB.
 */
public class PDBInfoDAOMongo implements PDBInfoDAO {

    private final MongoDatabase mongoDb;
    private final String collectionName;
    private final String assemblyCollectionName;

    public PDBInfoDAOMongo(MongoDatabase mongoDb) {
        this.mongoDb = mongoDb;
        this.collectionName = MongoUtils.getTableMetadataFromJpa(PdbInfoDB.class).name();
        this.assemblyCollectionName = MongoUtils.getTableMetadataFromJpa(AssemblyDB.class).name();
    }

    @Override
    public PdbInfoDB getPDBInfo(String entryId) throws DaoException {
        return getPDBInfo(entryId, true);
    }

    @Override
    public PdbInfoDB getPDBInfo(String entryId, boolean withAssemblies) throws DaoException {
        PdbInfoDB pdbInfoDB;
        try {
            // the assemblies are excluded for the entries that have them in their PdbInfo document
            Document projection = new Document("_id", 0);
            if (!withAssemblies) {
                projection.append(ASSEMBLIES_FIELD, 0);
            }
            pdbInfoDB = ConfigurableMapper.getMapper().convertValue(
                    mongoDb.getCollection(collectionName).find(new Document(ENTRY_ID_FIELD, entryId)).projection(projection).first(),
                    PdbInfoDB.class);
            if (pdbInfoDB != null && withAssemblies) {
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
        for (Document assemblyDoc : mongoDb.getCollection(assemblyCollectionName)
                .find(new Document(ENTRY_ID_FIELD, pdbInfo.getEntryId()))
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
}
