package eppic.db.loaders;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.BlobsDao;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceResidueFeaturesDAO;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.mongo.BlobsDAOMongo;
import eppic.db.dao.mongo.InterfaceResidueFeaturesDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.mongoutils.DbPropertiesReader;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.PdbInfoDB;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


public class RemoveUserJobs {

    private static final Logger logger = LoggerFactory.getLogger(RemoveUserJobs.class);

	private static File configFile = null;

	private static PDBInfoDAO dao;
	private static InterfaceResidueFeaturesDAO interfResDao;
	private static BlobsDao blobsDao;

	public static void main(String[] args) throws IOException, DaoException {

		String help =
				"Usage: UploadToDB\n" +
				" -d <int>      : delete jobs older than this many days\n" +
				" [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
				"                 the config will be read from file "+ DbPropertiesReader.DEFAULT_CONFIG_FILE_NAME+" in home dir\n";
		Getopt g = new Getopt("UploadToDB", args, "d:g:h?");
		int c;

		int numDays = -1;

		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'd':
				numDays = Integer.parseInt(g.getOptarg());
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

		if (numDays<0) {
			System.err.println("Number of days must be provided with -d");
			System.exit(1);
		}

		LocalDate keepFromHere = LocalDate.now().minusDays(numDays);

		DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
		String connUri = propsReader.getMongoUriUserJobs();
		String dbName = propsReader.getDbNameUserJobs();

		logger.info("Will use db name {}", dbName);
		MongoDatabase mongoDb = MongoUtils.getMongoDatabase(dbName, connUri);

		dao = new PDBInfoDAOMongo(mongoDb);
		interfResDao = new InterfaceResidueFeaturesDAOMongo(mongoDb);
		blobsDao = new BlobsDAOMongo(mongoDb);

		List<PdbInfoDB> list;
		try {
			list = dao.getAll();
		} catch (DaoException e) {
			logger.error("Could not get PdbInfo documents");
			throw e;
		}

		logger.info("Total of {} user jobs found", list.size());

		Date dateKeepFromHere = Date.from(keepFromHere.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		logger.info("Will remove user jobs submitted before {}", dateKeepFromHere);

		List<String> idsToRemove = list.stream().filter(pdbInfoDB -> pdbInfoDB.getUploadDate().before(dateKeepFromHere)).map(PdbInfoDB::getEntryId).toList();

		logger.info("User jobs older than {} days: {}. Will proceed to remove them", numDays, idsToRemove.size());

		for (String id : idsToRemove) {
			long removed = dao.remove(id);
			if (removed!=1) {
				logger.warn("Was expecting to remove 1 PdbInfo object, but actually removed {}", removed);
			}
			removed = interfResDao.remove(id);
			logger.info("Removed InterfaceResidueFeature data for id {}. There were {} objects", id, removed);
			removed = blobsDao.remove(id);
			logger.info("Removed all data for id {}. There were {} blobs associated to it.", id, removed);
		}
	}

}
