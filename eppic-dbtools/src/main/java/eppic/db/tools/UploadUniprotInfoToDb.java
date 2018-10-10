package eppic.db.tools;

import eppic.commons.sequence.NoMatchFoundException;
import eppic.commons.sequence.UniProtConnection;
import eppic.commons.sequence.UnirefEntry;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.jpa.UniProtInfoDAOJpa;
import eppic.db.jpautils.DbConfigGenerator;
import eppic.model.dto.UniProtInfo;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.kraken.interfaces.uniparc.UniParcEntry;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadUniprotInfoToDb {

    private static final Logger logger = LoggerFactory.getLogger(UploadUniprotInfoToDb.class);

    private static final Pattern UNIREF_SUBJECT_PATTERN = Pattern.compile("^UniRef\\d+_([0-9A-Z\\-]+)$");

    private static AtomicInteger countDone = new AtomicInteger(0);

    private static AtomicInteger alreadyPresent = new AtomicInteger(0);
    private static AtomicInteger couldntInsert = new AtomicInteger(0);
    private static AtomicInteger couldntRetrieve = new AtomicInteger(0);
    private static AtomicInteger couldntFind = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {

        String help =
                "Usage: UploadUniprotInfoToDb\n" +
                        "  -D <string>  : the database name to use\n"+
                        "  -f <file>    : the blast tabular format file containing all hits\n" +
                        " [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
                        "                 the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n" +
                        " [-n <int>]    : number of workers. Default 1. \n";


        File blastTabFile = null;
        String dbName = null;
        File configFile = DBHandler.DEFAULT_CONFIG_FILE;
        int numWorkers = 1;

        Getopt g = new Getopt("UploadSearchSeqCacheToDb", args, "D:f:g:n:h?");
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

        initJpaConnection(configFile);

        logger.info("Parsing file {}", blastTabFile);

        Set<String> uniqueIds = getUniqueUniRefIds(blastTabFile);

        logger.info("A total of {} unique UniProt/Parc ids were found", uniqueIds.size());

        UniProtConnection uc = new UniProtConnection();

        // possibly more efficient but needs to be done via batches in loop below. We can't request many millions
        // of them in one go because the output of UniRefEntry would be very large and require much memory
        //List<UnirefEntry> unirefs = uc.getMultipleUnirefEntries(uniprotIds);

        ExecutorService executorService = Executors.newFixedThreadPool(numWorkers);

        UniProtInfoDAO dao = new UniProtInfoDAOJpa();

        for (String uniId : uniqueIds) {
            executorService.submit(() -> retrieveInfoAndPersist(uc, dao, uniId));
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {

        }

        int didInsert = uniqueIds.size() - couldntInsert.get() - couldntFind.get() - couldntRetrieve.get() - alreadyPresent.get();
        logger.info("Done processing {} UniProt/Parc entries to database. Actually inserted {} in db", uniqueIds.size(), didInsert);
        if (couldntInsert.get()>0) {
            logger.info("{} entries could not be persisted to db", couldntInsert.get());
        }
        if (couldntFind.get()>0) {
            logger.info("{} entries could not be found by querying JAPI", couldntFind.get());
        }
        if (couldntRetrieve.get()>0) {
            logger.info("{} entries failed to be retrieved from JAPI", couldntRetrieve.get());
        }
        if (alreadyPresent.get()>0) {
            logger.info("{} entries were already present in db and were not reloaded", alreadyPresent.get());
        }

    }

    private static void retrieveInfoAndPersist(UniProtConnection uc, UniProtInfoDAO dao, String uniId) {

        // TODO what to do if it's a isoform id? (i.e. with a "-")

        try {

            int countProcessed = countDone.incrementAndGet();

            if (countProcessed%1000000 == 0) {
                logger.info("Done processing {} entries", countProcessed);
            }

            UniProtInfo uniProtInfo = dao.getUniProtInfo(uniId);

            if (uniProtInfo!=null) {
                logger.debug("Id already present in db: {}. Skipping", uniId);
                int countPresent = alreadyPresent.incrementAndGet();
                if (countPresent % 1000000 == 0) {
                    logger.info("Gone through {} already present entries in db", countPresent);
                }
                return;
            }

            if (uniId.startsWith("UPI")) {
                UniParcEntry uniparcEntry = uc.getUniparcEntry(uniId);
                String seq = uniparcEntry.getSequence().getValue();
                dao.insertUniProtInfo(uniId, seq, null, null);
            } else {
                final UnirefEntry entry = uc.getUnirefEntry(uniId);
                dao.insertUniProtInfo(entry.getUniId(), entry.getSequence(), entry.getFirstTaxon(), entry.getLastTaxon());
            }

        } catch (DaoException e) {
            logger.warn("Could not insert to db UniProt id {}.", uniId);
            couldntInsert.incrementAndGet();
        } catch (NoMatchFoundException e) {
            logger.warn("Could not find UniProt id {} via JAPI", uniId);
            couldntFind.incrementAndGet();
        } catch (ServiceException e) {
            logger.warn("Problem retrieving UniProt info from JAPI for UniProt id {}", uniId);
            couldntRetrieve.incrementAndGet();
        }
    }

    private static Set<String> getUniqueUniRefIds(File blastTabFile) throws IOException {
        Set<String> uniqueIds = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(blastTabFile));) {

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
}
