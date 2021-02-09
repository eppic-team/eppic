package eppic.db.tools;

import com.mongodb.client.MongoDatabase;
import eppic.db.MongoUtils;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.db.dao.UniProtMetadataDAO;
import eppic.db.dao.mongo.HitHspDAOMongo;
import eppic.model.db.HitHspDB;
import eppic.model.dto.HitHsp;
import eppic.model.dto.UniProtMetadata;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Jose Duarte
 * @since 3.2.0
 */
public class UploadSearchSeqCacheToDb {

    private static final Logger logger = LoggerFactory.getLogger(UploadSearchSeqCacheToDb.class);

    private static int couldntInsert = 0;
    private static int alreadyPresent = 0;

    private static boolean full;

    public static void main(String[] args) throws IOException, InterruptedException {

        String help =
                "Usage: UploadSearchSeqCacheToDb\n" +
                        "  -D <string>  : the database name to use\n"+
                        "  -f <file>    : the blast tabular format file containing all hits\n" +
                        " [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
                        "                 the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n" +
                        " [-r <string>] : uniref type to be written to db, e.g. UniRef90. If not provided null is written\n" +
                        " [-v <string>] : version of UniProt to be written to db, e.g. 2018_08. If not provided, null is written\n" +
                        " [-F]          : whether FULL mode should be used. Otherwise INCREMENTAL mode. In FULL the collection \n" +
                        "                 is dropped and indexes reset. FULL is much faster because it uses Mongo batch insert\n";

        File blastTabFile = null;
        String dbName = null;
        File configFile = DBHandler.DEFAULT_CONFIG_FILE;
        String uniProtVersion = null;
        String uniRefType = null;
        full = false;

        Getopt g = new Getopt("UploadSearchSeqCacheToDb", args, "D:f:g:r:v:Fh?");
        int c;
        while ((c = g.getopt()) != -1) {
            switch(c){
                case 'D':
                    dbName = g.getOptarg();
                    break;
                case 'f':
                    blastTabFile = new File(g.getOptarg());
                    break;
                case 'g':
                    configFile = new File(g.getOptarg());
                    break;
                case 'r':
                    uniRefType = g.getOptarg();
                    break;
                case 'v':
                    uniProtVersion = g.getOptarg();
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

        if (blastTabFile == null) {
            System.err.println("A blast tabular format file must be provided with -f");
            System.exit(1);
        }

        if (uniRefType == null) {
            logger.warn("UniRef type not passed with -r option. UniRef type won't be available in db.");
        }

        if (uniProtVersion == null) {
            logger.warn("UniRef version not passed with -v option. UniRef version won't be available in db.");
        }

        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
        String connUri = propsReader.getMongoUri();

        MongoDatabase mongoDb = MongoUtils.getMongoDatabase(dbName, connUri);

        HitHspDAO hitHspDAO = new HitHspDAOMongo(mongoDb);

        if (full) {
            logger.info("FULL mode: dropping collection and recreating index");
            MongoUtils.dropCollection(mongoDb, HitHspDB.class);
            MongoUtils.createIndices(mongoDb, HitHspDB.class);
        } else {
            logger.info("INCREMENTAL mode: will respect existing data and check presence before inserting");
        }

        long start = System.currentTimeMillis();

        List<HitHspDB> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(blastTabFile));) {

            String line;
            int lineNum = 0;
            int cantParse = 0;
            int cantPersist = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.isEmpty()) continue;
                String[] tokens = line.split("\t");
                if (tokens.length != 12) {
                    logger.warn("Line {} of file {} does not have 12 fields. Ignoring line.", lineNum, blastTabFile);
                    continue;
                }
                // qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore

                String queryId = tokens[0];
                String subjectId = tokens[1];
                try {
                    double identity = Double.parseDouble(tokens[2]);
                    int length = Integer.parseInt(tokens[3]);
                    int mismatches = Integer.parseInt(tokens[4]);
                    int gapOpenings = Integer.parseInt(tokens[5]);
                    int qStart = Integer.parseInt(tokens[6]);
                    int qEnd = Integer.parseInt(tokens[7]);
                    int sStart = Integer.parseInt(tokens[8]);
                    int sEnd = Integer.parseInt(tokens[9]);
                    double eValue = Double.parseDouble(tokens[10]);
                    int bitScore = Integer.parseInt(tokens[11]);

                    HitHspDB hitHspDB = new HitHspDB();
                    hitHspDB.setQueryId(queryId);
                    hitHspDB.setSubjectId(subjectId);
                    hitHspDB.setPercentIdentity(identity);
                    hitHspDB.setAliLength(length);
                    hitHspDB.setNumMismatches(mismatches);
                    hitHspDB.setNumGapOpenings(gapOpenings);
                    hitHspDB.setQueryStart(qStart);
                    hitHspDB.setQueryEnd(qEnd);
                    hitHspDB.setSubjectStart(sStart);
                    hitHspDB.setSubjectEnd(sEnd);
                    hitHspDB.seteValue(eValue);
                    hitHspDB.setBitScore(bitScore);

                    list.add(hitHspDB);

                } catch (NumberFormatException e) {
                    logger.warn("Wrong number format for line {} of file {}. Query id is {}. Will ignore line. Error: {}", lineNum, blastTabFile, queryId, e.getMessage());
                    cantParse++;
                }

                if (lineNum % 1000 == 0) {
                    persistWrapper(hitHspDAO, list);
                    list = new ArrayList<>();
                }

                if (lineNum % 10000 == 0) {
                    logger.info("Done processing {} lines.", lineNum);
                }

            }

            // persist the final chunk
            if (!list.isEmpty()) {
                persistWrapper(hitHspDAO, list);
            }

            logger.info("File {} fully processed", blastTabFile);

            long end = System.currentTimeMillis();

            logger.info("Processed a total of {} lines in file {}. Time taken: {} s", lineNum, blastTabFile, (end-start)/1000.0);
            if (!full) {
                logger.info("We were in INCREMENTAL mode. {} entries were not persisted again because they were already present in db", alreadyPresent);
            }
            if (cantParse!=0) {
                logger.info("Could not parse {} lines", cantParse);
            }
            if (couldntInsert!=0) {
                logger.info("Could not persist {} lines", cantPersist);
            }

            persistUniprotMetadata(lineNum, uniRefType, uniProtVersion);
        }

    }

    private static void persistWrapper(HitHspDAO dao, List<HitHspDB> list) {
        if (full) {
            persistList(dao, list);
        } else {
            list.forEach(h -> persistOne(dao, h));
        }

    }

    private static void persistOne(HitHspDAO hitHspDAO, HitHspDB hit) {

        try {

            HitHsp hitInDb = hitHspDAO.getHitHsp(hit.getQueryId(), hit.getSubjectId(), hit.getQueryStart(), hit.getQueryEnd(), hit.getSubjectStart(), hit.getSubjectEnd());
            if (hitInDb != null) {
                logger.debug("Hit already present for queryId {}, subjectId {}, qStart {}, qEnd {}, sStart {}, sEnd {}",
                        hit.getQueryId(), hit.getSubjectId(), hit.getQueryStart(), hit.getQueryEnd(), hit.getSubjectStart(), hit.getSubjectEnd());
                alreadyPresent++;
            } else {

                //logger.debug("Persisting {}--{}", queryId, subjectId);
                hitHspDAO.insertHitHsp(hit.getQueryId(), hit.getSubjectId(), hit.getPercentIdentity(), hit.getAliLength(), hit.getNumMismatches(), hit.getNumGapOpenings(),
                        hit.getQueryStart(), hit.getQueryEnd(), hit.getSubjectStart(), hit.getSubjectEnd(), hit.geteValue(), hit.getBitScore());

            }

        } catch (DaoException e) {
            logger.error("Could not persist HitHsp for query {}, subject {}. Error: {}", hit.getQueryId(), hit.getSubjectId(), e.getMessage());
            couldntInsert++;
        }
    }

    private static void persistList(HitHspDAO hitHspDAO, List<HitHspDB> list) {

        try {
            //logger.debug("Persisting {}--{}", queryId, subjectId);
            hitHspDAO.insertHitHsps(list);

        } catch (DaoException e) {
            logger.error("Could not persist HitHsps with first query {}, first subject {}. Error: {}", list.get(0).getQueryId(), list.get(0).getSubjectId(), e.getMessage());
            couldntInsert++;
        }
    }

    private static void persistUniprotMetadata(int lastLineCount, String uniRefType, String uniProtVersion) {
        // TODO rewrite in Mongo
        // a rough way of guessing that the insertion of records was successful
//        if (lastLineCount - couldntInsert > 1000) {
//            UniProtMetadataDAO dao = new UniProtMetadataDAOJpa();
//            UniProtMetadata uniProtMetadata = null;
//            try {
//                uniProtMetadata = dao.getUniProtMetadata();
//            } catch (DaoException e) {
//                logger.info("UniProtMetadata could not be find in database. We will persist a new one.");
//            }
//
//            if (uniProtMetadata == null) {
//                try {
//                    dao.insertUniProtMetadata(uniRefType, uniProtVersion);
//                } catch (DaoException e) {
//                    logger.warn("Could not persist the UniProt metadata (UniRef type and version). " +
//                            "Things could fail downstream. Error: {}", e.getMessage());
//                }
//            } else {
//                logger.info("UniProtMetadata present in database with uniRefType={}, version={}. Will not persist a new one.",
//                        uniProtMetadata.getUniRefType(), uniProtMetadata.getVersion());
//            }
//        }
    }

}
