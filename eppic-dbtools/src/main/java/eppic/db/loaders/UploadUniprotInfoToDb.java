package eppic.db.loaders;

import com.mongodb.client.MongoDatabase;
import eppic.commons.sequence.NoMatchFoundException;
import eppic.commons.sequence.UniProtConnection;
import eppic.commons.sequence.UnirefEntry;
import eppic.db.mongoutils.MongoUtils;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.mongo.UniProtInfoDAOMongo;
import eppic.db.mongoutils.DbPropertiesReader;
import eppic.model.db.UniProtInfoDB;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Parses all unique UniRef ids out of a .m8 file (blast/mmseqs2 output) and
 * reads info for each entry from a UniRef FASTA file (sequence and last taxon),
 * persisting the info to db via JPA.
 * Avoids using the UniProt REST API and instead uses the info in FASTA file (for speed). It will
 * only use the REST API  for those ids that could not be found in FASTA file.
 *
 * @author Jose Duarte
 * @since 3.2.0
 */
public class UploadUniprotInfoToDb {

    private static final Logger logger = LoggerFactory.getLogger(UploadUniprotInfoToDb.class);

    private static final int BATCH_SIZE = 1000;

    private static final Pattern UNIREF_SUBJECT_PATTERN = Pattern.compile("^UniRef\\d+_([0-9A-Z\\-]+)$");
    private static final Pattern TAXONOMY_PATTERN = Pattern.compile(".*Tax=(.*)TaxID=.*");

    private static int countDone = 0;
    private static int alreadyPresent = 0;
    private static int couldntInsert = 0;
    private static int couldntFindInRestApi = 0;
    private static int didInsert = 0;

    private static Set<String> notInFastaUniIds = new HashSet<>();

    private static boolean full;


    public static void main(String[] args) throws IOException, InterruptedException {

        String help =
                        "Usage: UploadUniprotInfoToDb\n" +
                        "  -f <file>    : the blast tabular format file containing all hits\n" +
                        "  -a <file>    : UniRef FASTA file (plain text or gzipped, if gzipped must have .gz suffix)\n" +
                        " [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
                        "                 the config will be read from file "+ DbPropertiesReader.DEFAULT_CONFIG_FILE_NAME+" in home dir\n" +
                        " [-F]          : whether FULL mode should be used. Otherwise INCREMENTAL mode. In FULL the collection \n" +
                        "                 is dropped and indexes reset. FULL is much faster because it uses Mongo batch insert\n" +
                        "Reads all unique UniRef ids present in the .m8 file (output of blast/mmseqs), \n" +
                        "then retrieves the sequence and taxonomy data (only last taxon) from the UniRef FASTA file and\n" +
                        "finally persists the data to db\n";


        File blastTabFile = null;
        File unirefFastaFile = null;
        File configFile = DbPropertiesReader.DEFAULT_CONFIG_FILE;
        full = false;

        Getopt g = new Getopt("UploadSearchSeqCacheToDb", args, "f:a:g:Fh?");
        int c;
        while ((c = g.getopt()) != -1) {
            switch(c){
                case 'f':
                    blastTabFile = new File(g.getOptarg());
                    break;
                case 'a':
                    unirefFastaFile = new File(g.getOptarg());
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

        if (blastTabFile == null) {
            System.err.println("A blast tabular format file must be provided with -f");
            System.exit(1);
        }

        logger.info("Parsing file {}", blastTabFile);

        Set<String> uniqueIds = getUniqueUniRefIds(blastTabFile);

        logger.info("A total of {} unique UniProt/Parc ids were found in file {}", uniqueIds.size(), blastTabFile);

        DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
        String connUri = propsReader.getMongoUri();

        MongoDatabase mongoDb = MongoUtils.getMongoDatabase(propsReader.getDbName(), connUri, MongoUtils.MONGO_USER_ENV_VAR, MongoUtils.MONGO_PASSWORD_ENV_VAR);

        if (full) {
            logger.info("FULL mode: dropping collection and recreating index");
            MongoUtils.dropCollection(mongoDb, UniProtInfoDB.class);
            MongoUtils.createIndices(mongoDb, UniProtInfoDB.class);
        } else {
            logger.info("INCREMENTAL mode: will respect existing data and check presence before inserting");
        }

        retrieveInfoAndPersist(mongoDb, unirefFastaFile, uniqueIds);

        logger.info("A total of {} ids were not present in FASTA file. Proceeding to grab them from UniProt REST API and persist them", notInFastaUniIds.size());

        retrieveFromRestApiAndPersist(mongoDb, notInFastaUniIds);

        if (countDone + couldntFindInRestApi < uniqueIds.size()) {
            logger.warn("Count of processed entries ({}) after retrieving info from FASTA file and JAPI is less than the number of entries needed ({}). " +
                    "There's something wrong!", countDone + couldntFindInRestApi, uniqueIds.size());
        }
        logger.info("Done processing {} UniProt/Parc entries to database. Actually inserted {} in db", uniqueIds.size(), didInsert);
        if (couldntInsert>0) {
            logger.info("{} entries could not be persisted to db", couldntInsert);
        }
        if (alreadyPresent>0) {
            logger.info("{} entries were already present in db and were not reloaded", alreadyPresent);
        }
        if (couldntFindInRestApi>0) {
            logger.info("{} entries (out of {}) could not be retrieved via JAPI", couldntFindInRestApi, notInFastaUniIds.size());
        }

    }

    /**
     * Parses the FASTA file extracting id, taxonomy and sequence for ids present in input uniqueIds,
     * subsequently persisting the info to db.
     * @param fastaFile the UniRef FASTA file, either plain text or gzipped (must have .gz extension)
     * @param uniqueIds the ids for which we want to extract info from the FASTA file
     * @throws IOException if problems when parsing FASTA file
     */
    private static void retrieveInfoAndPersist(MongoDatabase mongoDb, File fastaFile, Set<String> uniqueIds) throws IOException {

        logger.info("Proceeding to parse info from FASTA file {} and persist it.", fastaFile);

        UniProtInfoDAO dao = new UniProtInfoDAOMongo(mongoDb);

        Set<String> uniIdsPresentInFasta = new HashSet<>();

        InputStreamReader isr;
        if (fastaFile.getName().endsWith(".gz")) {
            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(fastaFile)));
        } else {
            isr = new InputStreamReader(new FileInputStream(fastaFile));
        }
        try (BufferedReader br = new BufferedReader(isr)) {
            String line;
            boolean readSequence = false;
            UniProtInfoDB currentUniEntry = null;
            StringBuilder currentSequence = null;
            int lineNum = 0;
            List<UniProtInfoDB> list = new ArrayList<>();
            while ((line = br.readLine())!=null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                if (line.startsWith(">")) {
                    if (currentUniEntry!=null  && currentUniEntry.getUniId()!=null) {
                        currentUniEntry.setSequence(currentSequence.toString());
                        if (currentUniEntry.getSequence().length()==0) {
                            logger.warn("Sequence for uni id {} has 0 length!", currentUniEntry.getUniId());
                        }
                        list.add(currentUniEntry);
                        if (list.size() == BATCH_SIZE) {
                            persistWrapper(dao, list);
                            list = new ArrayList<>();

                            if (countDone%1000000 == 0) {
                                logger.info("Done processing {} entries from FASTA file", countDone);
                            }
                        }
                    }

                    // reset last sequence
                    currentSequence = new StringBuilder();
                    readSequence = false;
                    currentUniEntry = new UniProtInfoDB();

                    // tag line, parse id to see if we want to persist it
                    // >UniRef90_P20848 Putative alpha-1-antitrypsin-related protein n=1 Tax=Homo sapiens TaxID=9606 RepID=A1ATR_HUMAN
                    String tag = line.substring(1, line.indexOf(" ")).trim();
                    Matcher m = UNIREF_SUBJECT_PATTERN.matcher(tag);
                    if (m.matches()) {
                        String uniId = m.group(1);
                        if (uniqueIds.contains(uniId)) {
                            currentUniEntry.setUniId(uniId);
                            readSequence = true;
                            Matcher taxMatcher = TAXONOMY_PATTERN.matcher(line);
                            if (taxMatcher.matches()) {
                                currentUniEntry.setLastTaxon(taxMatcher.group(1).split(" ")[0]);
                            } else {
                                logger.warn("Could not match taxonomy for uni id {}, won't have taxonomy info for it. Full tag is: {}", uniId, line);
                            }

                            uniIdsPresentInFasta.add(uniId);
                        }
                    } else {
                        logger.warn("Tag {} does not conform to expected UniRef identifier format", tag);
                    }
                } else {
                    // line with sequence data
                    if (readSequence) {
                        currentSequence.append(line.trim());
                    }
                }
            }

            // make sure we add the last sequence if it is one of the required ids
            if (readSequence && currentUniEntry!=null && currentUniEntry.getUniId()!=null) {
                currentUniEntry.setSequence(currentSequence.toString());
                list.add(currentUniEntry);
            }

            if (list.size()>0) {
                // and persist the last batch
                persistWrapper(dao, list);
            }
        }

        notInFastaUniIds = new HashSet<>(uniqueIds);
        notInFastaUniIds.removeAll(uniIdsPresentInFasta);

    }

    /**
     * Retrieve UniProt info from UniProt REST API for given notInFastaUniIds and
     * subsequently persist the info to db via JPA.
     * @param notInFastaUniIds a list of UniRef ids that were not found in FASTA file
     */
    private static void retrieveFromRestApiAndPersist(MongoDatabase mongoDb, Set<String> notInFastaUniIds) {

        UniProtConnection uc = new UniProtConnection();
        UniProtInfoDAO dao = new UniProtInfoDAOMongo(mongoDb);

        int count = 0;
        for (String uniId : notInFastaUniIds) {
            count++;
            if (count%1000 == 0) {
                logger.info("Done processing {} UniProt ids by retrieving data from JAPI", count);
            }

            try {
                UnirefEntry entry = uc.getUnirefEntryWithRetry(uniId);

                final UniProtInfoDB uniEntry = new UniProtInfoDB();
                uniEntry.setSequence(entry.getSequence());
                uniEntry.setUniId(entry.getUniId());
                uniEntry.setLastTaxon(entry.getLastTaxon());
                persistOne(dao, uniEntry);

            } catch (NoMatchFoundException e) {
                logger.warn("Could not find UniProt id {} via JAPI", uniId);
                couldntFindInRestApi++;
            } catch (IOException e) {
                logger.warn("Problem retrieving UniProt info from JAPI for UniProt id {}. Error: {}", uniId, e.getMessage());
                couldntFindInRestApi++;
            }
        }

    }

    private static void persistWrapper(UniProtInfoDAO dao, List<UniProtInfoDB> uniEntries) {
        // TODO what to do if it's a isoform id? (i.e. with a "-")

        if (full) {
            persistList(dao, uniEntries);
        } else {
            uniEntries.forEach(u -> persistOne(dao, u));
        }
    }

    private static void persistOne(UniProtInfoDAO dao, UniProtInfoDB uniEntry) {

        countDone ++;

        try {
            UniProtInfoDB uniProtInfo = dao.getUniProtInfo(uniEntry.getUniId());

            if (uniProtInfo!=null) {
                logger.debug("Id already present in db: {}. Skipping", uniEntry.getUniId());
                alreadyPresent++;
                if (alreadyPresent % 1000000 == 0) {
                    logger.info("Gone through {} already present entries in db", alreadyPresent);
                }
                return;
            }

            dao.insertUniProtInfo(uniEntry.getUniId(), uniEntry.getSequence(), null, uniEntry.getLastTaxon());
            didInsert++;

        } catch (DaoException e) {
            logger.warn("Could not insert to db UniProt id {}. Error: {}", uniEntry.getUniId(), e.getMessage());
            couldntInsert++;
        }
    }

    private static void persistList(UniProtInfoDAO dao, List<UniProtInfoDB> uniEntries) {

        countDone += uniEntries.size();

        try {
            dao.insertUniProtInfos(uniEntries);
            didInsert += uniEntries.size();

        } catch (DaoException e) {
            logger.warn("Could not insert to db UniProt id batch (first is {}). Error: {}", uniEntries.get(0).getUniId(), e.getMessage());
            couldntInsert += uniEntries.size();
        }
    }

    private static Set<String> getUniqueUniRefIds(File blastTabFile) throws IOException {
        Set<String> uniqueIds = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(blastTabFile))) {

            String line;
            int lineNum = 0;

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

                // we also want to add the queries, if we use UniRef90 or lower levels of clustering
                // the search will not have necessarily find the query, since it might not be a representative
                String[] queryTokens = queryId.split("_");
                if (queryTokens.length == 2) {
                    uniqueIds.add(queryTokens[0]);
                } else {
                    logger.warn("Query id {} did not split into 2 tokens at '_' delimiter. Ignoring it.", queryId);
                }

                Matcher m = UNIREF_SUBJECT_PATTERN.matcher(subjectId);
                if (m.matches()) {
                    String uniId = m.group(1);
                    uniqueIds.add(uniId);
                } else {
                    logger.warn("Unexpected subject id format for {}. Will skip.", subjectId);
                }
            }
        }
        return uniqueIds;
    }

}
