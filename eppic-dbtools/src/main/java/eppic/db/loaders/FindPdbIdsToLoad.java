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
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class FindPdbIdsToLoad {

    private static final Logger logger = LoggerFactory.getLogger(FindPdbIdsToLoad.class);

    private static String dbName = null;
    private static File configFile = null;
    private static boolean full = false;
    private static String jsonGzUrl = null;

    public static void main(String[] args) throws IOException {

        String help =
                "Usage: FindPdbIdsToLoad\n" +
                        "  -D <string>  : the database name to use\n" +
                        "  -l <url>     : URL to json.gz file with PDB archive contents\n" +
                        " [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
                        "                 the config will be read from file " + DbPropertiesReader.DEFAULT_CONFIG_FILE_NAME + " in home dir\n" +
                        " [-F]          : whether FULL mode should be used: the db contents are ignored and all relevant \n" +
                        "                 PDB ids are used. Otherwise INCREMENTAL mode: the difference between contents \n" +
                        "                 of db and PDB file repo is used\n";


        Getopt g = new Getopt("UploadToDB", args, "D:l:g:Fh?");
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'D':
                    dbName = g.getOptarg();
                    break;
                case 'l':
                    jsonGzUrl = g.getOptarg();
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

        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
        String connUri = propsReader.getMongoUri();

        MongoDatabase mongoDb = MongoUtils.getMongoDatabase(dbName, connUri);

        // TODO if this is to be kept here, then it needs to be removed from UploadToDb
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

        // TODO get entries in db with timestamps
        Map<String, OffsetDateTime> map = getAllPdbIds();
        for (Map.Entry<String, OffsetDateTime> entry : map.entrySet()) {

            if (!isValidEntry(new URL("https://files.rcsb.org/download/" + entry.getKey() + ".cif.gz"))) {
                logger.info("Skipping entry: {}", entry.getKey());
                continue;
            }
            // TODO compare timestamps
        }

    }

    /**
     * Parse given cif.gz PDB entry and return true if entry is crystallograpic and contains at least 1 protein entity.
     * Otherwise false
     * @param cifgzFileUrl
     * @return
     * @throws IOException
     */
    private static boolean isValidEntry(URL cifgzFileUrl) throws IOException {
        CifFile cifFile = CifIO.readFromURL(cifgzFileUrl);
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
    private static Map<String, OffsetDateTime> getAllPdbIds() throws IOException {
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
}
