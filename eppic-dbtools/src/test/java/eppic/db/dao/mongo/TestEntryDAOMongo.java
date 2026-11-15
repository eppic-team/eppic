package eppic.db.dao.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.EntryDAO;
import eppic.db.dao.InterfaceResidueFeaturesDAO;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.loaders.EntryData;
import eppic.db.loaders.UploadToDb;
import eppic.db.mongoutils.ConfigurableMapper;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.AssemblyDB;
import eppic.model.db.AssemblyScoreDB;
import eppic.model.db.GraphEdgeDB;
import eppic.model.db.GraphNodeDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.NoResultException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests of how entries are written to the db and read from it, against a real MongoDB. They run only if the
 * environment variable {@value #MONGO_URI_ENV_VAR} is set to the URI of a MongoDB that can be
 * written to, e.g. mongodb://localhost:27017. They work in a db of their own, which they drop.
 * <p>
 * If {@value #SERIALIZED_DIR_ENV_VAR} is set too, to a directory with serialized files as written
 * by the eppic cli with -w (it is searched recursively), then those entries are tested as well.
 */
public class TestEntryDAOMongo {

    static final String MONGO_URI_ENV_VAR = "EPPIC_TEST_MONGO_URI";
    static final String SERIALIZED_DIR_ENV_VAR = "EPPIC_TEST_SERIALIZED_DIR";

    private MongoClient mongoClient;
    private MongoDatabase mongoDb;
    private EntryDAO entryDao;
    private PDBInfoDAO dao;
    private InterfaceResidueFeaturesDAO featuresDao;

    @Before
    public void setUp() {
        String uri = System.getenv(MONGO_URI_ENV_VAR);
        assumeTrue("Needs a MongoDB, with its URI in env variable " + MONGO_URI_ENV_VAR, uri != null && !uri.isBlank());
        mongoClient = MongoClients.create(uri);
        mongoDb = mongoClient.getDatabase("eppic_test_" + System.nanoTime());
        entryDao = new EntryDAOMongo(mongoClient, mongoDb);
        dao = new PDBInfoDAOMongo(mongoDb);
        featuresDao = new InterfaceResidueFeaturesDAOMongo(mongoDb);
    }

    @After
    public void tearDown() {
        if (mongoClient != null) {
            mongoDb.drop();
            mongoClient.close();
        }
    }

    @Test
    public void testAssembliesAreSeparateDocuments() throws DaoException {
        PdbInfoDB pdbInfo = createEntry("1abc", 4);
        entryDao.write(pdbInfo, createFeatures("1abc", 3));

        Document pdbInfoDoc = mongoDb.getCollection("PdbInfo").find(new Document("entryId", "1abc")).first();
        assertNotNull(pdbInfoDoc);
        assertFalse(pdbInfoDoc.containsKey("assemblies"));
        assertEquals(4, mongoDb.getCollection("Assembly").countDocuments(new Document("entryId", "1abc")));
        assertEquals(3, mongoDb.getCollection("InterfaceResidueFeatures").countDocuments(new Document("entryId", "1abc")));
        assertTrue(MongoUtils.hasExpectedIndices(mongoDb, PdbInfoDB.class));
        assertTrue(MongoUtils.hasExpectedIndices(mongoDb, AssemblyDB.class));
        assertTrue(MongoUtils.hasExpectedIndices(mongoDb, InterfaceResidueFeaturesDB.class));
        assertEquals(2, featuresDao.getInterfResFeatures("1abc", 2).getInterfaceId());

        assertSameEntry(pdbInfo, dao.getPDBInfo("1abc"));

        PdbInfoDB noAssemblies = dao.getPDBInfo("1abc", false);
        assertTrue(noAssemblies.getAssemblies().isEmpty());
        assertEquals(3, noAssemblies.getInterfaceClusters().size());
    }

    @Test
    public void testEntriesDontMix() throws DaoException {
        PdbInfoDB first = createEntry("1abc", 4);
        PdbInfoDB second = createEntry("2abc", 2);
        PdbInfoDB noAssemblies = createEntry("3abc", 0);
        entryDao.insertAll(List.of(
                new EntryData(first, createFeatures("1abc", 3)),
                new EntryData(second, createFeatures("2abc", 1)),
                new EntryData(noAssemblies, createFeatures("3abc", 0))));

        assertSameEntry(first, dao.getPDBInfo("1abc"));
        assertSameEntry(second, dao.getPDBInfo("2abc"));
        assertTrue(dao.getPDBInfo("3abc").getAssemblies().isEmpty());

        assertTrue(entryDao.remove("1abc"));
        assertFalse(entryDao.remove("1abc"));
        assertEquals(0, mongoDb.getCollection("Assembly").countDocuments(new Document("entryId", "1abc")));
        assertEquals(0, mongoDb.getCollection("InterfaceResidueFeatures").countDocuments(new Document("entryId", "1abc")));
        assertEquals(1, mongoDb.getCollection("InterfaceResidueFeatures").countDocuments());
        try {
            dao.getPDBInfo("1abc");
            fail("The entry should be gone");
        } catch (NoResultException e) {
            // expected
        }
        assertSameEntry(second, dao.getPDBInfo("2abc"));

        // all entries, which come without assemblies
        List<PdbInfoDB> all = dao.getAll();
        assertEquals(2, all.size());
        for (PdbInfoDB pdbInfo : all) {
            assertTrue(pdbInfo.getAssemblies().isEmpty());
        }
    }

    /**
     * Entries written before the assemblies had a collection of their own have them in the
     * PdbInfo document and must still be read.
     */
    @Test
    public void testEntryWithEmbeddedAssemblies() throws DaoException {
        PdbInfoDB pdbInfo = createEntry("1abc", 4);
        MongoUtils.writeObject(mongoDb, "PdbInfo", pdbInfo);
        assertEquals(0, mongoDb.getCollection("Assembly").countDocuments());

        assertSameEntry(pdbInfo, dao.getPDBInfo("1abc"));
        assertTrue(dao.getPDBInfo("1abc", false).getAssemblies().isEmpty());

        // and are replaced by entries with the assemblies in their collection
        PdbInfoDB replacement = createEntry("1abc", 2);
        entryDao.write(replacement, createFeatures("1abc", 1));
        assertFalse(mongoDb.getCollection("PdbInfo").find().first().containsKey("assemblies"));
        assertSameEntry(replacement, dao.getPDBInfo("1abc"));

        assertTrue(entryDao.remove("1abc"));
        assertEquals(0, mongoDb.getCollection("PdbInfo").countDocuments());
    }

    @Test
    public void testWriteReplacesEntry() throws DaoException {
        entryDao.write(createEntry("1abc", 4), createFeatures("1abc", 3));
        entryDao.write(createEntry("2abc", 2), createFeatures("2abc", 2));
        PdbInfoDB replacement = createEntry("1abc", 2);
        replacement.setTitle("The replacement");
        entryDao.write(replacement, createFeatures("1abc", 1));

        assertEquals(2, mongoDb.getCollection("PdbInfo").countDocuments());
        assertEquals(2, mongoDb.getCollection("Assembly").countDocuments(new Document("entryId", "1abc")));
        assertEquals(1, mongoDb.getCollection("InterfaceResidueFeatures").countDocuments(new Document("entryId", "1abc")));
        assertEquals("The replacement", dao.getPDBInfo("1abc").getTitle());
        assertSameEntry(replacement, dao.getPDBInfo("1abc"));
        assertEquals(2, dao.getPDBInfo("2abc").getAssemblies().size());
    }

    /**
     * With transactions a write that fails leaves the entry that was in the db
     */
    @Test
    public void testFailedWriteInTransaction() throws DaoException {
        Document hello = mongoDb.runCommand(new Document("hello", 1));
        assumeTrue("Needs a MongoDB with transactions", hello.containsKey("setName") || "isdbgrid".equals(hello.get("msg")));

        PdbInfoDB pdbInfo = createEntry("1abc", 4);
        entryDao.write(pdbInfo, createFeatures("1abc", 3));
        try {
            entryDao.write(createEntry("1abc", 2), createFeaturesWithDuplicate("1abc"));
            fail("Writing 2 InterfaceResidueFeaturesDB for the same interface should fail");
        } catch (DaoException e) {
            // expected
        }
        assertSameEntry(pdbInfo, dao.getPDBInfo("1abc"));
        assertEquals(4, mongoDb.getCollection("Assembly").countDocuments());
        assertEquals(3, mongoDb.getCollection("InterfaceResidueFeatures").countDocuments());
    }

    /**
     * Without transactions a write that fails leaves nothing of the entry
     */
    @Test
    public void testFailedWriteWithoutTransaction() throws DaoException {
        EntryDAO noTransactionsDao = new EntryDAOMongo(null, mongoDb);
        noTransactionsDao.write(createEntry("1abc", 4), createFeatures("1abc", 3));
        noTransactionsDao.write(createEntry("2abc", 4), createFeatures("2abc", 3));
        try {
            noTransactionsDao.write(createEntry("1abc", 2), createFeaturesWithDuplicate("1abc"));
            fail("Writing 2 InterfaceResidueFeaturesDB for the same interface should fail");
        } catch (DaoException e) {
            // expected
        }
        try {
            dao.getPDBInfo("1abc");
            fail("The entry should be gone");
        } catch (NoResultException e) {
            // expected
        }
        assertEquals(4, mongoDb.getCollection("Assembly").countDocuments());
        assertEquals(3, mongoDb.getCollection("InterfaceResidueFeatures").countDocuments());
        assertEquals(4, dao.getPDBInfo("2abc").getAssemblies().size());

        // and the entry can then be written
        noTransactionsDao.write(createEntry("1abc", 2), createFeatures("1abc", 2));
        assertEquals(2, dao.getPDBInfo("1abc").getAssemblies().size());
    }

    @Test
    public void testInsertingExistingEntryFails() throws DaoException {
        PdbInfoDB pdbInfo = createEntry("1abc", 4);
        entryDao.insertAll(List.of(new EntryData(pdbInfo, createFeatures("1abc", 3))));
        try {
            entryDao.insertAll(List.of(new EntryData(createEntry("1abc", 2), createFeatures("1abc", 1))));
            fail("Inserting an entry that exists should fail");
        } catch (DaoException e) {
            // expected
        }
        // and what was there is untouched
        assertSameEntry(pdbInfo, dao.getPDBInfo("1abc"));
        assertEquals(3, mongoDb.getCollection("InterfaceResidueFeatures").countDocuments());
    }

    @Test
    public void testSerializedFiles() throws DaoException {
        String dir = System.getenv(SERIALIZED_DIR_ENV_VAR);
        assumeTrue("Needs serialized files, in the dir in env variable " + SERIALIZED_DIR_ENV_VAR, dir != null && !dir.isBlank());
        List<File> files = new ArrayList<>();
        findSerializedFiles(new File(dir), files);
        assertFalse("No serialized files in " + dir, files.isEmpty());
        for (File file : files) {
            EntryData entryData = UploadToDb.readSerializedFile(file);
            assertNotNull(entryData);
            PdbInfoDB pdbInfo = entryData.getPdbInfoDB();
            entryDao.write(pdbInfo, entryData.getInterfResFeaturesDB());
            assertEquals(entryData.getInterfResFeaturesDB().size(),
                    mongoDb.getCollection("InterfaceResidueFeatures").countDocuments(new Document("entryId", pdbInfo.getEntryId())));
            assertEquals(pdbInfo.getAssemblies().size(),
                    mongoDb.getCollection("Assembly").countDocuments(new Document("entryId", pdbInfo.getEntryId())));
            // What must come back is what a single document with everything would give. That is
            // not exactly what went in: e.g. a NaN is written as null and read as 0
            PdbInfoDB expected = ConfigurableMapper.getMapper().convertValue(
                    MongoUtils.convertValueToDocument(pdbInfo), PdbInfoDB.class);
            assertSameEntry(expected, dao.getPDBInfo(pdbInfo.getEntryId()));
        }
    }

    private static void findSerializedFiles(File dir, List<File> files) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                findSerializedFiles(child, files);
            } else if (child.getName().endsWith(UploadToDb.SERIALIZED_FILE_SUFFIX)) {
                files.add(child);
            }
        }
    }

    /**
     * Asserts that the 2 entries are the same as far as anything that reads them can tell, i.e.
     * that they serialize to the same json, and that the references in the entry that was read
     * are in place.
     */
    static void assertSameEntry(PdbInfoDB expected, PdbInfoDB actual) {
        assertEquals(toComparableJson(expected), toComparableJson(actual));
        assertEquals(expected.getAssemblies().size(), actual.getAssemblies().size());
        for (int i = 0; i < expected.getAssemblies().size(); i++) {
            assertEquals(expected.getAssemblies().get(i).getId(), actual.getAssemblies().get(i).getId());
            assertSame(actual, actual.getAssemblies().get(i).getPdbInfo());
        }
    }

    private static JsonNode toComparableJson(PdbInfoDB pdbInfo) {
        JsonNode json = ConfigurableMapper.getMapper().valueToTree(pdbInfo);
        // the interface clusters of an assembly are a hash set: they serialize in any order
        for (JsonNode assembly : json.path("assemblies")) {
            if (assembly.get("interfaceClusters") instanceof ArrayNode clusters) {
                List<JsonNode> sorted = new ArrayList<>();
                clusters.forEach(sorted::add);
                sorted.sort((c1, c2) -> Integer.compare(c1.path("clusterId").asInt(), c2.path("clusterId").asInt()));
                clusters.removeAll();
                clusters.addAll(sorted);
            }
        }
        return json;
    }

    /**
     * An entry with the given number of assemblies, with the ids that the eppic cli gives them:
     * not sorted and, for the topologically invalid ones, repeated.
     */
    private static PdbInfoDB createEntry(String entryId, int numAssemblies) {
        int[] ids = {1, 2, 0, -1, -1};
        PdbInfoDB pdbInfo = new PdbInfoDB();
        pdbInfo.setEntryId(entryId);
        pdbInfo.setTitle("Entry " + entryId);

        List<InterfaceClusterDB> interfaceClusters = new ArrayList<>();
        for (int clusterId = 1; clusterId <= 3; clusterId++) {
            InterfaceClusterDB interfaceCluster = new InterfaceClusterDB();
            interfaceCluster.setClusterId(clusterId);
            interfaceCluster.setPdbCode(entryId);
            interfaceCluster.setPdbInfo(pdbInfo);
            interfaceClusters.add(interfaceCluster);
        }
        pdbInfo.setInterfaceClusters(interfaceClusters);

        for (int i = 0; i < numAssemblies; i++) {
            AssemblyDB assembly = new AssemblyDB();
            assembly.setId(ids[i]);
            assembly.setUnitCellAssembly(ids[i] == 0);
            assembly.setTopologicallyValid(ids[i] >= 0);
            assembly.setInterfaceClusterIds(entryId + "-" + i);
            assembly.setPdbInfo(pdbInfo);

            Set<InterfaceClusterDB> engaged = new HashSet<>(interfaceClusters.subList(0, 1 + i % 3));
            assembly.setInterfaceClusters(engaged);

            AssemblyScoreDB score = new AssemblyScoreDB();
            score.setMethod("eppic");
            score.setCallName("bio");
            score.setScore(0.5 + i);
            score.setAssembly(assembly);
            assembly.addAssemblyScore(score);

            List<GraphNodeDB> nodes = new ArrayList<>();
            List<GraphEdgeDB> edges = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                GraphNodeDB node = new GraphNodeDB();
                node.setLabel("A_" + j);
                node.setPos3dX(j + 0.25);
                node.setAssembly(assembly);
                nodes.add(node);
                GraphEdgeDB edge = new GraphEdgeDB();
                edge.setLabel("e" + j);
                edge.setInterfaceId(j + 1);
                edge.setXtalTransA(-j);
                edge.setAssembly(assembly);
                edges.add(edge);
            }
            assembly.setGraphNodes(nodes);
            assembly.setGraphEdges(edges);

            pdbInfo.addAssembly(assembly);
        }
        return pdbInfo;
    }

    private static List<InterfaceResidueFeaturesDB> createFeatures(String entryId, int numInterfaces) {
        List<InterfaceResidueFeaturesDB> features = new ArrayList<>();
        for (int interfaceId = 1; interfaceId <= numInterfaces; interfaceId++) {
            InterfaceResidueFeaturesDB feature = new InterfaceResidueFeaturesDB();
            feature.setEntryId(entryId);
            feature.setInterfaceId(interfaceId);
            features.add(feature);
        }
        return features;
    }

    /**
     * Features that can't be written, because of the unique index on entry id and interface id
     */
    private static List<InterfaceResidueFeaturesDB> createFeaturesWithDuplicate(String entryId) {
        List<InterfaceResidueFeaturesDB> features = createFeatures(entryId, 2);
        features.get(1).setInterfaceId(1);
        return features;
    }
}
