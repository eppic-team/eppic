package eppic.db.tools;

import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.db.dao.jpa.HitHspDAOJpa;
import eppic.db.jpautils.DbConfigGenerator;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 * @author Jose Duarte
 * @since 3.2.0
 */
public class UploadSearchSeqCacheToDb {

    private static final Logger logger = LoggerFactory.getLogger(UploadSearchSeqCacheToDb.class);

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        String help =
                "Usage: UploadSearchSeqCacheToDb\n" +
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

        String db = null;
        String fileName = blastTabFile.getName();
        // e.g. pdbAll_UniRef90_2018_09.m8
        Pattern p = Pattern.compile("^.*_(UniRef\\d+_\\d\\d\\d\\d_\\d\\d)\\.m8$");
        Matcher m = p.matcher(fileName);
        if (m.matches()) {
            db = m.group(1);
            logger.info("UniRef version parsed from file name is {}", db);
        } else {
            logger.warn("Could not parse UniRef version from file name {}. UniRef version won't be available in db. ", fileName);
        }

        initJpaConnection(configFile);

        HitHspDAO hitHspDAO = new HitHspDAOJpa();

        ExecutorService executorService = Executors.newFixedThreadPool(numWorkers);
        List<Future<Boolean>> allResults = new ArrayList<>();

        long start = System.currentTimeMillis();

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

                    // so that lambda works
                    final String dbToWrite = db;

                    Future<Boolean> future = executorService.submit(() -> persist(hitHspDAO, dbToWrite, queryId, subjectId, identity, length, mismatches, gapOpenings, qStart, qEnd, sStart, sEnd, eValue, bitScore));
                    allResults.add(future);



                } catch (NumberFormatException e) {
                    logger.warn("Wrong number format for line {} of file {}. Query id is {}. Will ignore line. Error: {}", lineNum, blastTabFile, queryId, e.getMessage());
                    cantParse++;
                }

                if (lineNum % 10000 == 0) {
                    logger.info("Done processing {} lines", lineNum);
                }
            }

            for (Future<Boolean> future : allResults) {
                if (!future.get()) cantPersist++;
            }

            executorService.shutdown();

            while (!executorService.isTerminated());

            long end = System.currentTimeMillis();

            logger.info("Processed a total of {} lines in file {}. Time taken: {} s", lineNum, blastTabFile, (end-start)/1000.0);

            if (cantParse!=0 || cantPersist!=0) {
                logger.info("Could not parse {} lines and could not persist {} lines", cantParse, cantPersist);
            }
        }



    }

    private static void initJpaConnection(File configFile) throws IOException {
        Map<String,String> props = DbConfigGenerator.createDatabaseProperties(configFile);
        EntityManagerHandler.initFactory(props);
    }

    private static boolean persist(HitHspDAO hitHspDAO,
                                   String db,
                                   String queryId, String subjectId,
                                   double identity, int length, int mismatches, int gapOpenings,
                                   int qStart, int qEnd, int sStart, int sEnd, double eValue, int bitScore) {
        try {
            hitHspDAO.insertHitHsp(
                    db,
                    queryId,
                    subjectId,
                    identity,
                    length,
                    mismatches,
                    gapOpenings,
                    qStart,
                    qEnd,
                    sStart,
                    sEnd,
                    eValue,
                    bitScore
            );

            return true;

        } catch (DaoException e) {
            logger.error("Could not persist HitHsp for query {}, subject {}. Error: {}", queryId, subjectId, e.getMessage());
            return false;
        }
    }

}
