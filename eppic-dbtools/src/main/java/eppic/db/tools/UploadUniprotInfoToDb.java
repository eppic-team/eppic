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

    private static AtomicInteger failureCounter = new AtomicInteger(0);

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

        logger.info("Done retrieving info and persisting {} UniProt/Parc entries to database", uniqueIds.size());
        if (failureCounter.get()>0) {
            logger.info("{} entries could not be retrieved or persisted", failureCounter.get());
        }

    }

    private static void retrieveInfoAndPersist(UniProtConnection uc, UniProtInfoDAO dao, String uniId) {

        // TODO what to do if it's a isoform id? (i.e. with a "-")

        try {

            UniProtInfo uniProtInfo = dao.getUniProtInfo(uniId);

            if (uniProtInfo!=null) {
                logger.info("Id already present in db: {}. Skipping", uniId);
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
            failureCounter.incrementAndGet();
        } catch (NoMatchFoundException e) {
            logger.warn("Could not find UniProt id {} via JAPI", uniId);
            failureCounter.incrementAndGet();
        } catch (ServiceException e) {
            logger.warn("Problem retrieving UniProt info from JAPI for UniProt id {}", uniId);
            failureCounter.incrementAndGet();
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

                String subjectId = tokens[1];

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
