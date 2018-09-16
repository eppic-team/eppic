package eppic.db.tools;

import eppic.commons.blast.BlastHit;
import eppic.commons.blast.BlastHitList;
import eppic.commons.blast.BlastHsp;
import eppic.commons.blast.SeqSearchCache;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.db.dao.jpa.HitHspDAOJpa;
import eppic.db.jpautils.DbConfigGenerator;
import eppic.model.db.HitHspDB;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 *
 * @author Jose Duarte
 * @since 3.2.0
 */
public class UploadSearchSeqCacheToDb {

    private static final Logger logger = LoggerFactory.getLogger(UploadSearchSeqCacheToDb.class);

    public static void main(String[] args) throws IOException {

        String help =
                "Usage: UploadSearchSeqCacheToDb\n" +
                        "  -D <string>  : the database name to use\n"+
                        "  -f <file>    : the blast tabular format file containing all hits\n" +
                        " [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
                        "                 the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n";


        File blastTabFile = null;
        String dbName = null;
        File configFile = DBHandler.DEFAULT_CONFIG_FILE;

        Getopt g = new Getopt("UploadSearchSeqCacheToDb", args, "D:f:g:h?");
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

        SeqSearchCache seqSearchCache = new SeqSearchCache();
        seqSearchCache.setDb(null); // TODO set db
        seqSearchCache.initCache(blastTabFile);

        HitHspDAO hitHspDAO = new HitHspDAOJpa();

        for (BlastHitList hitList : seqSearchCache.getAllHitLists()) {

            for (BlastHit hit : hitList.getHits()) {
                for (BlastHsp hsp : hit) {
                    try {
                    	// TODO convert from BlastHsp to HitHspDB
                    	HitHspDB hitHspDb = null;
                        hitHspDAO.insertHitHsp(hitHspDb);
                    } catch (DaoException e) {
                        logger.error("Could not persist HitHsp for query {}, subject {}. Error: {}", hit.getQueryId(), hit.getSubjectId(), e.getMessage());
                    }
                }
            }
        }

    }

    private static void initJpaConnection(File configFile) throws IOException {
        Map<String,String> props = DbConfigGenerator.createDatabaseProperties(configFile);
        EntityManagerHandler.initFactory(props);
    }
}
