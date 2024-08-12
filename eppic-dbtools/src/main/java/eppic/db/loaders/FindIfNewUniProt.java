package eppic.db.loaders;

import com.mongodb.client.MongoDatabase;
import eppic.db.mongoutils.ConfigurableMapper;
import eppic.db.mongoutils.DbPropertiesReader;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.UniProtMetadataDB;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import static com.mongodb.client.model.Projections.excludeId;

public class FindIfNewUniProt {

    private static final Logger logger = LoggerFactory.getLogger(FindIfNewUniProt.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        File configFile = null;
        File outFile = null;
        String dbName = null;
        String upUrl = null;

        String help =
                "Usage: FindIfNewUniProt\n" +
                        "  -u <url>     : URL to a UniProt resource (e.g. FASTA file) from which the release date will\n" +
                        "                 be obtained by looking at http headers\n" +
                        "  -o <file>    : output text file to write the new UniProt release version\n" +
                        "  -g <file>    : a configuration file containing the database access parameters\n" +
                        " [-D <string>] : the database name to use. If not provided it is read from config file in -g\n";


        Getopt g = new Getopt("FindPdbIdsToLoad", args, "D:u:o:g:h?");
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'D':
                    dbName = g.getOptarg();
                    break;
                case 'u':
                    upUrl = g.getOptarg();
                    break;
                case 'o':
                    outFile = new File(g.getOptarg());
                    break;
                case 'g':
                    configFile = new File(g.getOptarg());
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

        if (upUrl == null) {
            System.err.println("A UniProt url must be provided with -u");
            System.exit(1);
        }
        if (outFile == null) {
            System.err.println("An output file path to write out the UniProt release version must be provided with -o");
            System.exit(1);
        }
        if (configFile == null) {
            System.err.println("A config file path must be provided with -g");
            System.exit(1);
        }

        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
        String connUri = propsReader.getMongoUri();
        if (dbName == null) {
            logger.info("No db name provided with -D. Reading it from config file {}", configFile);
            dbName = propsReader.getDbName();
        }

        MongoDatabase mongoDb = MongoUtils.getMongoDatabase(dbName, connUri);

        String collName = MongoUtils.getTableMetadataFromJpa(UniProtMetadataDB.class).name();
        UniProtMetadataDB upMetadata = ConfigurableMapper.getMapper().convertValue(mongoDb.getCollection(collName).find()
                .projection(excludeId()).first(), UniProtMetadataDB.class);
        String dbVersion = null;
        if (upMetadata != null) {
            dbVersion = upMetadata.getVersion();
        }
        logger.info("Found UniProt version in database: {}", dbVersion);

        String onlineUniProtVer = findUniProtVerFromUrl(upUrl);
        if (onlineUniProtVer == null) {
            logger.error("Could not find UniProt version from url {}.", upUrl);
            throw new IllegalStateException("Could not find UniProt version from url " + upUrl);
        }
        logger.info("Found online UniProt version is: {}", onlineUniProtVer);

        logger.info("Writing output file {}", outFile);
        boolean isNewUniProt = isNewUniProtVersion(onlineUniProtVer, dbVersion);
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outFile.toPath()))) {
            if (isNewUniProt) {
                pw.println(onlineUniProtVer);
            }
        }
    }

    private static String findUniProtVerFromUrl(String url) throws IOException {
        URL cifGzUrl = new URL(url);
        HttpURLConnection httpCon = (HttpURLConnection) cifGzUrl.openConnection();
        return httpCon.getHeaderField("X-UniProt-Release");
    }

    private static boolean isNewUniProtVersion(String onlineVer, String dbVer) {
        if (dbVer == null) {
            logger.info("Considering this is a new UniProt version because db version was null");
            return true;
        }

        Integer[] dbYearMonth = getYearAndMonth(dbVer);
        Integer[] onlineYearMonth = getYearAndMonth(onlineVer);

        boolean isNewUniProt = false;
        if (onlineYearMonth[0] > dbYearMonth[0]) {
            isNewUniProt = true;
        } else if (onlineYearMonth[0].equals(dbYearMonth[0])) {
            if (onlineYearMonth[1] > dbYearMonth[1]) {
                isNewUniProt = true;
            }
        } else {
            throw new IllegalArgumentException("UniProt version year from db ("+dbVer+") is newer than the one found online ("+onlineVer+")! Something is wrong");
        }
        return isNewUniProt;
    }

    private static Integer[] getYearAndMonth(String upVer) {
        String[] tokens = upVer.split("_");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Invalid UniProt version: " + upVer);
        }
        return new Integer[]{Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1])};
    }
}
