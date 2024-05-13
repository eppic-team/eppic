package eppic.db.loaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import eppic.db.mongoutils.DbPropertiesReader;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;
import gnu.getopt.Getopt;
import org.rcsb.cif.CifIO;
import org.rcsb.cif.model.CifFile;
import org.rcsb.cif.model.StrColumn;
import org.rcsb.cif.schema.StandardSchemata;
import org.rcsb.cif.schema.mm.EntityPoly;
import org.rcsb.cif.schema.mm.Exptl;
import org.rcsb.cif.schema.mm.MmCifBlock;
import org.rcsb.cif.schema.mm.MmCifFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.include;

public class FindPdbIdsToLoad {

    private static final Logger logger = LoggerFactory.getLogger(FindPdbIdsToLoad.class);

    private static final String DEFAULT_CIF_BASE_URL = "https://models.rcsb.org/";

    private static String dbName = null;
    private static File configFile = null;
    private static boolean full = false;
    private static String jsonGzUrl = null;
    private static String baseCifUrl = null;
    private static boolean isBcifRepo = false;

    public static void main(String[] args) throws IOException {

        String help =
                "Usage: FindPdbIdsToLoad\n" +
                        "  -l <url>     : URL to json.gz file with PDB archive contents\n" +
                        "  -D <string>  : the database name to use. If not provided it is read from config file in -g\n" +
                        " [-b <url>]    : base URL for grabbing CIF.gz or BCIF.gz PDB archive files. If %s placeholder\n" +
                        "                 present, it is replaced by the 2 middle letters from the PDB id\n" +
                        " [-B]          : base URL refers to a BCIF.gz repo. If not provided defaults to a CIF.gz repo\n" +
                        " [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
                        "                 the config will be read from file " + DbPropertiesReader.DEFAULT_CONFIG_FILE_NAME + " in home dir\n" +
                        " [-F]          : whether FULL mode should be used: the db contents are ignored and all relevant \n" +
                        "                 PDB ids are used. Otherwise INCREMENTAL mode: the difference between contents \n" +
                        "                 of db and PDB file repo is used\n";


        Getopt g = new Getopt("FindPdbIdsToLoad", args, "D:l:b:Bg:Fh?");
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'D':
                    dbName = g.getOptarg();
                    break;
                case 'l':
                    jsonGzUrl = g.getOptarg();
                    break;
                case 'b':
                    baseCifUrl = g.getOptarg();
                    break;
                case 'B':
                    isBcifRepo = true;
                    break;
                case 'g':
                    configFile = new File(g.getOptarg());
                    break;
                case 'F':
                    full = true;
                    break;
                case 'h':
                    System.out.println(help);
                    System.exit(0);
                    break;
                case '?':
                    System.err.println(help);
                    System.exit(1);
                    break; // getopt() already printed an error
            }
        }

        if (dbName == null) {
            System.err.println("A database name must be provided with -D");
            System.exit(1);
        }
        if (jsonGzUrl == null) {
            System.err.println("A json gz url must be provided with -l");
            System.exit(1);
        }
        if (baseCifUrl == null) {
            logger.warn("Base CIF URL not provided. Using default: {}", DEFAULT_CIF_BASE_URL);
            baseCifUrl = DEFAULT_CIF_BASE_URL;
        } else {
            if (!baseCifUrl.endsWith("/")) baseCifUrl = baseCifUrl + "/";
        }
        if (isBcifRepo) {
            logger.info("Considering base URL {} a BCIF.gz repo", baseCifUrl);
        } else {
            logger.info("Considering base URL {} a CIF.gz repo", baseCifUrl);
        }

        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
        String connUri = propsReader.getMongoUri();
        if (dbName == null) {
            logger.info("No db name provided with -D. Reading it from config file {}", configFile);
            dbName = propsReader.getDbName();
        }

        MongoDatabase mongoDb = MongoUtils.getMongoDatabase(dbName, connUri);

        // TODO we need to drop also the sequence data (HitHsp etc): how to do it? probably with another CLI param to control for it
        if (full) {
            // remove content and create collections and index
            logger.info("FULL mode was selected. Will drop all data and recreate collections and indexes");
            MongoUtils.dropCollection(mongoDb, PdbInfoDB.class);
            MongoUtils.dropCollection(mongoDb, InterfaceResidueFeaturesDB.class);
            MongoUtils.createIndices(mongoDb, PdbInfoDB.class);
            MongoUtils.createIndices(mongoDb, InterfaceResidueFeaturesDB.class);
        } else {
            // we might be doing incremental but start from an empty db: we must add indexes in this case too
            if (MongoUtils.isCollectionEmpty(mongoDb, PdbInfoDB.class)) {
                logger.info("Empty PdbInfoDB collection. Creating indices for it, even though we are in INCREMENTAL mode");
                MongoUtils.createIndices(mongoDb, PdbInfoDB.class);
            }
            if (MongoUtils.isCollectionEmpty(mongoDb, InterfaceResidueFeaturesDB.class)) {
                logger.info("Empty InterfaceResidueFeaturesDB collection. Creating indices for it, even though we are in INCREMENTAL mode");
                MongoUtils.createIndices(mongoDb, InterfaceResidueFeaturesDB.class);
            }
        }

        Map<String, OffsetDateTime> dbEntries = getDbEntries(mongoDb);
        Map<String, OffsetDateTime> currentEntries = getAllCurrentPdbIds();
        logger.info("Entries in db: {}", dbEntries.size());
        logger.info("Current entries from holdings file ({}): {}", jsonGzUrl, currentEntries.size() );

        Map<String, UpdateType> candidates = findToUpdateCandidates(dbEntries, currentEntries);
        long numOutdated = candidates.values().stream().filter(v -> v == UpdateType.UPDATED).count();
        long numObsoleted = candidates.values().stream().filter(v -> v == UpdateType.OBSOLETED).count();
        long numMissing = candidates.values().stream().filter(v -> v == UpdateType.MISSING).count();
        logger.info("Found total of {} candidates. From those: {} are outdated, {} are obsoleted, {} are missing", candidates.size(), numOutdated, numObsoleted,  numMissing);

        candidates.entrySet().removeIf(e ->
                e.getValue() == UpdateType.MISSING && !isValidEntry(e.getKey()));
        numMissing = candidates.values().stream().filter(v -> v == UpdateType.MISSING).count();
        logger.info("From the missing set, only {} need adding. The rest are invalid (non crystallograpic or not containing protein)", numMissing);
        // TODO write it out to file
    }

    /**
     * For the given pdbId parse its cif.gz/bcif.gz file using base URL baseCifUrl and return true if entry is crystallograpic and contains at least 1 protein entity.
     * Otherwise false
     * @param pdbId
     * @return
     * @throws UncheckedIOException if an IOException happens when reading url
     */
    private static boolean isValidEntry(String pdbId) {
        if (baseCifUrl.contains("%s")) {
            String hash = pdbId.substring(1, 3);
            baseCifUrl = String.format(baseCifUrl, hash);
        }
        String cifgzFileUrlStr;
        if (isBcifRepo) {
            cifgzFileUrlStr = baseCifUrl + pdbId + ".bcif.gz";
        } else {
            cifgzFileUrlStr = baseCifUrl + pdbId + ".cif.gz";
        }
        URL url;
        try {
            url = new URL(cifgzFileUrlStr);
        } catch (MalformedURLException e) {
            logger.error("Malformed URL: {}. Please make sure input cif base URL is correct", cifgzFileUrlStr);
            throw new UncheckedIOException(e);
        }
        CifFile cifFile;
        try {
            cifFile = CifIO.readFromURL(url);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        MmCifFile mmCifFile = cifFile.as(StandardSchemata.MMCIF);

        // get first block of CIF
        MmCifBlock data = mmCifFile.getFirstBlock();
        Exptl exptl = data.getExptl();
        StrColumn methods = exptl.getMethod();
        boolean isCrystallographic = methods.values().anyMatch(m -> m.equals("X-RAY DIFFRACTION") || m.equals("NEUTRON DIFFRACTION") || m.equals("ELECTRON CRYSTALLOGRAPHY"));
        if (!isCrystallographic) {
            return false;
        }
        EntityPoly entityPoly = data.getEntityPoly();
        StrColumn types = entityPoly.getType();
        return types.values().anyMatch(t -> t.equals("polypeptide(L)"));
    }

    /**
     * Parse eh PDB archive holdings release structures json.gz file and return a map of lower case
     * PDB ids to dates
     * @return
     * @throws IOException
     */
    private static Map<String, OffsetDateTime> getAllCurrentPdbIds() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        URL url;
        try {
            url = new URL(jsonGzUrl);
        } catch (MalformedURLException e) {
            logger.error("Invalid URL provided: {}", jsonGzUrl);
            throw new RuntimeException(e);
        }
        Map<String, OffsetDateTime> pdbIds = new HashMap<>();
        InputStream is = new GZIPInputStream(url.openStream());
        JsonNode tree = mapper.readTree(is);
        Iterator<String> it = tree.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            OffsetDateTime t = OffsetDateTime.parse(tree.get(key).asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            pdbIds.put(key.toLowerCase(), t);
        }
        return pdbIds;
    }

    private static Map<String, OffsetDateTime> getDbEntries(MongoDatabase db) {
        return db.getCollection("PdbInfo")
                .find()
                .projection(fields(excludeId(), include("entryId"), include("releaseDate")))
                .into(new ArrayList<>())
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getString("entryId"),
                        d -> OffsetDateTime.parse(d.getString("releaseDate"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    }

    private static Map<String, UpdateType> findToUpdateCandidates(Map<String, OffsetDateTime> dbEntries, Map<String, OffsetDateTime> currentEntries) {
        Map<String, UpdateType> candidates = new HashMap<>();
        // loop through all current entries and find if they are in db and up-to-date
        for (Map.Entry<String, OffsetDateTime> currentEntry : currentEntries.entrySet()) {
            if (!dbEntries.containsKey(currentEntry.getKey())) {
                candidates.put(currentEntry.getKey(), UpdateType.MISSING);
            } else {
                if (currentEntry.getValue().isAfter(dbEntries.get(currentEntry.getKey()))) {
                    candidates.put(currentEntry.getKey(), UpdateType.UPDATED);
                }
            }
        }
        // loop through all db entries and find if any are not in current set (then it is obsolete)
        for (Map.Entry<String, OffsetDateTime> dbEntry : dbEntries.entrySet()) {
            if (!currentEntries.containsKey(dbEntry.getKey())) {
                candidates.put(dbEntry.getKey(), UpdateType.OBSOLETED);
            }
        }
        return candidates;
    }

    private enum UpdateType {
        MISSING, UPDATED, OBSOLETED
    }
}
