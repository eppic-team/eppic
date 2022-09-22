package eppic.db.tools;

import eppic.commons.sequence.NoMatchFoundException;
import eppic.commons.sequence.UniProtConnection;
import eppic.commons.sequence.UnirefEntry;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.jpa.UniProtInfoDAOJpa;
import eppic.db.jpautils.DbConfigGenerator;
import eppic.db.tools.helpers.MonitorThread;
import eppic.model.dto.UniProtInfo;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    private static final long SLEEP_TIME = 10000;

    private static final Pattern UNIREF_SUBJECT_PATTERN = Pattern.compile("^UniRef\\d+_([0-9A-Z\\-]+)$");
    private static final Pattern TAXONOMY_PATTERN = Pattern.compile(".*Tax=(.*)TaxID=.*");

    private static AtomicInteger countDone = new AtomicInteger(0);

    private static AtomicInteger alreadyPresent = new AtomicInteger(0);
    private static AtomicInteger couldntInsert = new AtomicInteger(0);
    private static AtomicInteger couldntFindInRestApi = new AtomicInteger(0);
    private static AtomicInteger didInsert = new AtomicInteger(0);

    private static Set<String> notInFastaUniIds = new HashSet<>();


    public static void main(String[] args) throws IOException, InterruptedException {

        String help =
                        "Usage: UploadUniprotInfoToDb\n" +
                        "  -D <string>  : the database name to use\n"+
                        "  -f <file>    : the blast tabular format file containing all hits\n" +
                        "  -a <file>    : UniRef FASTA file (plain text or gzipped, if gzipped must have .gz suffix)\n" +
                        " [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
                        "                 the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n" +
                        " [-n <int>]    : number of workers. Default 1. \n" +
                        "Reads all unique UniRef ids present in the .m8 file (output of blast/mmseqs), \n" +
                        "then retrieves the sequence and taxonomy data (only last taxon) from the UniRef FASTA file and\n" +
                        "finally persists the data to db\n";


        File blastTabFile = null;
        File unirefFastaFile = null;
        String dbName = null;
        File configFile = DBHandler.DEFAULT_CONFIG_FILE;
        int numWorkers = 1;

        Getopt g = new Getopt("UploadSearchSeqCacheToDb", args, "D:f:a:g:n:h?");
        int c;
        while ((c = g.getopt()) != -1) {
            switch(c){
                case 'D':
                    dbName = g.getOptarg();
                    break;
                case 'f':
                    blastTabFile = new File(g.getOptarg());
                    break;
                case 'a':
                    unirefFastaFile = new File(g.getOptarg());
                    break;
                case 'g':
                    configFile = new File(g.getOptarg());
                    break;
                case 'n':
                    numWorkers = Integer.parseInt(g.getOptarg());
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

        logger.info("Parsing file {}", blastTabFile);

        Set<String> uniqueIds = getUniqueUniRefIds(blastTabFile);

        logger.info("A total of {} unique UniProt/Parc ids were found in file {}", uniqueIds.size(), blastTabFile);

        initJpaConnection(configFile);

        retrieveInfoAndPersist(unirefFastaFile, uniqueIds, numWorkers);

        logger.info("A total of {} ids were not present in FASTA file. Proceeding to grab them from UniProt REST API and persist them", notInFastaUniIds.size());

        retrieveFromRestApiAndPersist(notInFastaUniIds, numWorkers);

        if (countDone.get() + couldntFindInRestApi.get() < uniqueIds.size()) {
            logger.warn("Count of processed entries ({}) after retrieving info from FASTA file and UniProt REST API is less than the number of entries needed ({}). " +
                    "There's something wrong!", countDone.get() + couldntFindInRestApi.get(), uniqueIds.size());
        }
        logger.info("Done processing {} UniProt/Parc entries to database. Actually inserted {} in db", uniqueIds.size(), didInsert.get());
        if (couldntInsert.get()>0) {
            logger.info("{} entries could not be persisted to db", couldntInsert.get());
        }
        if (alreadyPresent.get()>0) {
            logger.info("{} entries were already present in db and were not reloaded", alreadyPresent.get());
        }
        if (couldntFindInRestApi.get()>0) {
            logger.info("{} entries (out of {}) could not be retrieved via UniProt REST API", couldntFindInRestApi.get(), notInFastaUniIds.size());
        }

    }

    /**
     * Parses the FASTA file extracting id, taxonomy and sequence for ids present in input uniqueIds,
     * subsequently persisting the info to db via JPA.
     * @param fastaFile the UniRef FASTA file, either plain text or gzipped (must have .gz extension)
     * @param uniqueIds the ids for which we want to extract info from the FASTA file
     * @param numWorkers the number of workers for concurrent persistence
     * @throws IOException if problems when parsing FASTA file
     * @throws InterruptedException
     */
    private static void retrieveInfoAndPersist(File fastaFile, Set<String> uniqueIds, int numWorkers) throws IOException, InterruptedException {

        logger.info("Proceeding to parse info from FASTA file {} and persist it.", fastaFile);

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ThreadPoolExecutor executorPool = new ThreadPoolExecutor(numWorkers, numWorkers, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000000), threadFactory);

        MonitorThread monitor = new MonitorThread(executorPool, 60);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        UniProtInfoDAO dao = new UniProtInfoDAOJpa();

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
            UniEntry currentUniEntry = null;
            StringBuilder currentSequence = null;
            int lineNum = 0;
            while ((line = br.readLine())!=null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                if (line.startsWith(">")) {
                    if (currentUniEntry!=null  && currentUniEntry.uniId!=null) {
                        currentUniEntry.sequence = currentSequence.toString();
                        if (currentUniEntry.sequence.length()==0) {
                            logger.warn("Sequence for uni id {} has 0 length!", currentUniEntry.uniId);
                        }
                        final UniEntry uniEntry = currentUniEntry;
                        executorPool.submit(() -> persist(dao, uniEntry));
                    }

                    // reset last sequence
                    currentSequence = new StringBuilder();
                    readSequence = false;
                    currentUniEntry = new UniEntry();

                    // tag line, parse id to see if we want to persist it
                    // >UniRef90_P20848 Putative alpha-1-antitrypsin-related protein n=1 Tax=Homo sapiens TaxID=9606 RepID=A1ATR_HUMAN
                    String tag = line.substring(1, line.indexOf(" ")).trim();
                    Matcher m = UNIREF_SUBJECT_PATTERN.matcher(tag);
                    if (m.matches()) {
                        String uniId = m.group(1);
                        if (uniqueIds.contains(uniId)) {
                            currentUniEntry.uniId = uniId;
                            readSequence = true;
                            Matcher taxMatcher = TAXONOMY_PATTERN.matcher(line);
                            if (taxMatcher.matches()) {
                                currentUniEntry.lastTaxon = taxMatcher.group(1).split(" ")[0];
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

                if (lineNum % 100 == 0) {
                    int remCap = executorPool.getQueue().remainingCapacity();
                    if (remCap<100) {
                        logger.info("Remaining capacity in queue is {}. Halting main thread for {} s to give time to queue to process submitted jobs", remCap, SLEEP_TIME / 1000);
                        Thread.sleep(SLEEP_TIME);
                    }
                }
            }

            // make sure we persist the last sequence if it is one of the required ids
            if (readSequence && currentUniEntry!=null && currentUniEntry.uniId!=null) {
                currentUniEntry.sequence = currentSequence.toString();
                final UniEntry uniEntry = currentUniEntry;
                executorPool.submit(() -> persist(dao, uniEntry));
            }

        } finally {
            executorPool.shutdown();

            while (!executorPool.isTerminated()) {

            }
            monitor.shutDown();

        }

        notInFastaUniIds = new HashSet<>(uniqueIds);
        notInFastaUniIds.removeAll(uniIdsPresentInFasta);

    }

    /**
     * Retrieve UniProt info from UniProt REST API for given notInFastaUniIds and
     * subsequently persist the info to db via JPA.
     * @param notInFastaUniIds a list of UniRef ids that were not found in FASTA file
     * @param numWorkers the number of workers for concurrent persistence
     */
    private static void retrieveFromRestApiAndPersist(Set<String> notInFastaUniIds, int numWorkers) {

        ExecutorService executorService = Executors.newFixedThreadPool(numWorkers);

        UniProtConnection uc = new UniProtConnection();
        UniProtInfoDAO dao = new UniProtInfoDAOJpa();


        for (String uniId : notInFastaUniIds) {

            try {
                UnirefEntry entry = uc.getUnirefEntryWithRetry(uniId);

                final UniEntry uniEntry = new UniEntry();
                uniEntry.sequence = entry.getSequence();
                uniEntry.uniId = entry.getUniId();
                uniEntry.lastTaxon = entry.getLastTaxon();
                executorService.submit(() -> persist(dao, uniEntry));

            } catch (NoMatchFoundException e) {
                logger.warn("Could not find UniProt id {} via UniProt REST API", uniId);
                couldntFindInRestApi.incrementAndGet();
            } catch (IOException e) {
                logger.warn("Problem retrieving UniProt info from UniProt REST API for UniProt id {}. Error: {}", uniId, e.getMessage());
                couldntFindInRestApi.incrementAndGet();
            }
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {

        }

    }

    private static void persist(UniProtInfoDAO dao, UniEntry uniEntry) {

        // TODO what to do if it's a isoform id? (i.e. with a "-")

        int countProcessed = countDone.incrementAndGet();

        if (countProcessed%1000000 == 0) {
            logger.info("Done processing {} entries", countProcessed);
        }

        try {

            UniProtInfo uniProtInfo = dao.getUniProtInfo(uniEntry.uniId);

            if (uniProtInfo!=null) {
                logger.debug("Id already present in db: {}. Skipping", uniEntry.uniId);
                int countPresent = alreadyPresent.incrementAndGet();
                if (countPresent % 1000000 == 0) {
                    logger.info("Gone through {} already present entries in db", countPresent);
                }
                return;
            }

            dao.insertUniProtInfo(uniEntry.uniId, uniEntry.sequence, null, uniEntry.lastTaxon);
            didInsert.incrementAndGet();


        } catch (DaoException e) {
            logger.warn("Could not insert to db UniProt id {}. Error: {}", uniEntry.uniId, e.getMessage());
            couldntInsert.incrementAndGet();
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

    private static void initJpaConnection(File configFile) throws IOException {
        Map<String,String> props = DbConfigGenerator.createDatabaseProperties(configFile);
        EntityManagerHandler.initFactory(props);
    }

    private static class UniEntry {
        String uniId;
        String lastTaxon;
        String sequence;
    }
}
