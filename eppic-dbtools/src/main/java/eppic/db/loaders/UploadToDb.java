package eppic.db.loaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceResidueFeaturesDAO;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.mongo.InterfaceResidueFeaturesDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.mongoutils.ConfigurableMapper;
import eppic.db.mongoutils.DbPropertiesReader;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UploadToDb {

    private static final Logger logger = LoggerFactory.getLogger(UploadToDb.class);

	// the name of the dir that is the root of the divided dirs (indexed by the pdbCode middle 2-letters)
	private static final String DIVIDED_ROOT = "divided";

	// after this number of entry uploads time statistics will be produced
	private static final int TIME_STATS_EVERY = 1000;

	public static final String SERIALIZED_FILE_SUFFIX = ".json.zip";
	private static final String PDBINFO_SER_FILE_SUFFIX = ".pdbinfo.json";
	private static final String INTERFRESFEAT_SER_FILE_SUFFIX = ".interf_features.json";

	private static boolean full = false;

	private static final int DEFAULT_BATCH_SIZE = 1000;
	private static int batchSize = DEFAULT_BATCH_SIZE;

	private static final double MAX_FAILURE_RATE_TOLERATED = 0.10;

	private static File configFile = null;
	private static File choosefromFile = null;
	private static int numWorkers = 1;

	private static PDBInfoDAO dao;

	private static InterfaceResidueFeaturesDAO interfResDao;

	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

		String help =
				"Usage: UploadToDB\n" +
				"  -d <dir>     : root dir of eppic output files with subdirs as job dirs (user jobs only, no PDB ids jobs) \n" +
				" [-l]          : if specified, subdirs under root dir (-d) are considered to be in PDB divided \n" +
				"                 layout and leaf dirs must be PDB ids (this affects the behaviour of -d and -f).\n"+
				"                 Default: subdirs under root are taken directly as job dirs (user jobs) \n"+
				" [-f <file>]   : file specifying a list of PDB ids indicating subdirs of root \n"+
				"                 directory to take (default: uses all subdirs in the root directory) \n" +
				" [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
				"                 the config will be read from file "+ DbPropertiesReader.DEFAULT_CONFIG_FILE_NAME+" in home dir\n" +
				" [-n <int>]    : number of workers. Default 1. \n"+
				" [-F]          : whether FULL mode should be used. Otherwise INCREMENTAL mode. FULL is much faster \n" +
				"                 because it uses Mongo batch insert and avoids lookups. FULL will drop collections and \n" +
				"                 recreate them to ensure idempotency of process\n" +
				" [-b <int>]    : batch size for -F mode. Default is " +DEFAULT_BATCH_SIZE+".\n";


		boolean isDividedLayout = false;

		File jobDirectoriesRoot = null;

		Getopt g = new Getopt("UploadToDB", args, "d:lf:g:n:Fb:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'd':
				jobDirectoriesRoot = new File(g.getOptarg());
				break;
			case 'l':
				isDividedLayout = true;
				break;
			case 'f':
				choosefromFile = new File(g.getOptarg());
				break;
			case 'g':
				configFile = new File(g.getOptarg());
				break;
			case 'n':
				numWorkers = Integer.parseInt(g.getOptarg());
				break;
			case 'F':
				full = true;
				break;
			case 'b':
				batchSize = Integer.parseInt(g.getOptarg());
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

		if (jobDirectoriesRoot == null || ! jobDirectoriesRoot.isDirectory() ){
			System.err.println("\n\nHey Jim, Output Directory not specified correctly! \n");
			System.err.println(help);
			System.exit(1);
		}


		// Get the Directories to be processed
		List<File> jobsDirectories = new ArrayList<>();
		if(choosefromFile!=null){
			List<String> pdbCodes = readListFile(choosefromFile);
			for (String pdbCode:pdbCodes) {
				jobsDirectories.add(getDirectory(isDividedLayout, jobDirectoriesRoot, pdbCode));
			}
		}
		else {
			jobsDirectories = listAllDirs(isDividedLayout, jobDirectoriesRoot);
		}

		List<JobDir> jobDirs = listSerializedFiles(jobsDirectories, isDividedLayout);

		if (isDividedLayout) {
            logger.info("Directories under {} will be considered to have PDB divided layout, i.e. {}{}" + DIVIDED_ROOT + "{}<PDB-code-middle-2-letters>", jobDirectoriesRoot, jobDirectoriesRoot, File.separatorChar, File.separatorChar);
		} else {
            logger.info("Directories under {} will be considered to be user jobs, no PDB divided layout will be used. ", jobDirectoriesRoot);
		}

		logger.info("Will use " + numWorkers + " workers");

		DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
		String connUri = propsReader.getMongoUri();

		String dbName;
		if (isDividedLayout)
			dbName = propsReader.getDbName();
		else
			dbName = propsReader.getDbNameUserJobs();

		logger.info("Will use db name {}", dbName);
		MongoDatabase mongoDb = MongoUtils.getMongoDatabase(dbName, connUri);

		dao = new PDBInfoDAOMongo(mongoDb);
		interfResDao = new InterfaceResidueFeaturesDAOMongo(mongoDb);

		if (full) {
			// remove content and create collections and index
			// NOTE we do dropping of db data in FindPdbIdsToLoad too, but here it is still necessary to drop data to ensure
			// idempotency in -F case: if we restart the process it should still be able to load everything (in batches and
			// without lookups) and that requires an empty db
			logger.info("FULL mode was selected. Will drop all data and recreate collections and indexes");
			MongoUtils.dropCollection(mongoDb, PdbInfoDB.class);
			MongoUtils.dropCollection(mongoDb, InterfaceResidueFeaturesDB.class);
			MongoUtils.createIndices(mongoDb, PdbInfoDB.class);
			MongoUtils.createIndices(mongoDb, InterfaceResidueFeaturesDB.class);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(numWorkers);
		List<Future<Stats>> allResults = new ArrayList<>();

		long totalStart = System.currentTimeMillis();
		int setSize = jobDirs.size() / numWorkers;
		int remainder = jobDirs.size() % numWorkers;
		int j = 0;
		int offset = 0;

		for (int i=0; i<numWorkers; i++) {
			if (i == numWorkers-1) {
				setSize = setSize + remainder;
			}
			JobDir[] singleSet = new JobDir[setSize];
			int k = 0;
			for (; j<offset + setSize; j++) {
				singleSet[k] = jobDirs.get(j);
				k++;
			}
			offset = j;

			logger.info("Proceeding to submit to worker {}, set of size {}", i, singleSet.length);

			Future<Stats> future = executorService.submit(() -> loadSet(singleSet));
			allResults.add(future);
		}

		Stats allStats = new Stats();
		for (Future<Stats> future : allResults) {
			Stats singleStats = future.get();
			allStats.add(singleStats);
		}

		long totalEnd = System.currentTimeMillis();

		executorService.shutdown();

		while (!executorService.isTerminated()) {

		}

        logger.info("Completed all {} entries in {} s", jobDirs.size(), (totalEnd - totalStart) / 1000);
        logger.info("Uploaded: {}, couldn't insert: {}", allStats.countUploaded, allStats.pdbsWithWarnings.size());
        logger.info("There were {} error jobs in {} uploaded entries.", allStats.countErrorJob, allStats.countUploaded);

		if (!allStats.pdbsWithWarnings.isEmpty()) {
			logger.info("These PDBs had problems while inserting to db: {}", String.join(" ", allStats.pdbsWithWarnings));
		}

		// make sure we exit with an error state in cases with many failures
		double maxFailuresTolerated = jobDirs.size() * MAX_FAILURE_RATE_TOLERATED;
		if (allStats.pdbsWithWarnings.size() > maxFailuresTolerated) {
            logger.error("Total of {} failures, more than {} failures. Something must be wrong!", allStats.pdbsWithWarnings.size(), maxFailuresTolerated);
			System.exit(1);
		}

	}

	public static EntryData readSerializedFile(File serializedFile) {
		PdbInfoDB pdbScoreItem = null;
		List<InterfaceResidueFeaturesDB> interfResFeatures = null;
		try (ZipFile zipFile = new ZipFile(serializedFile)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(PDBINFO_SER_FILE_SUFFIX)) {
					pdbScoreItem = ConfigurableMapper.getMapper().readValue(zipFile.getInputStream(entry), PdbInfoDB.class);
				}
				if (entry.getName().endsWith(INTERFRESFEAT_SER_FILE_SUFFIX)) {
					interfResFeatures = ConfigurableMapper.getMapper().readValue(zipFile.getInputStream(entry), new TypeReference<>(){} );
				}
			}

			if (pdbScoreItem == null || interfResFeatures == null) {
                logger.error("Serialized zip file {} did not contain both needed json files: {}, {}", serializedFile, PDBINFO_SER_FILE_SUFFIX, INTERFRESFEAT_SER_FILE_SUFFIX);
			}

		} catch (IOException e) {
            logger.error("Problem reading serialized file, skipping entry {}. Error: {}", serializedFile, e.getMessage());
			return null;
		}
		return new EntryData(pdbScoreItem, interfResFeatures);
	}

	private static List<File> listAllDirs(boolean isDividedLayout, File rootDir) {
		List<File> allDirs = new ArrayList<>();
		if (isDividedLayout) {
			File dividedRoot = new File(rootDir, DIVIDED_ROOT);

			for (File indexDir:dividedRoot.listFiles()) {
				allDirs.addAll(Arrays.asList(indexDir.listFiles()));
			}

			return allDirs;

		} else {
			for (File dir:rootDir.listFiles()) {
				if (dir.getName().equals(DIVIDED_ROOT)) continue;
				allDirs.add(dir);
			}
		}
		return allDirs;
	}

	private static File getDirectory(boolean isDividedLayout, File rootDir, String pdbCode) {
		if (isDividedLayout) {
			String index = pdbCode.substring(1,	3);
			return new File(rootDir, DIVIDED_ROOT+File.separatorChar+index+File.separatorChar+pdbCode);
		} else {
			return new File(rootDir, pdbCode);
		}
	}

	private static List<String> readListFile(File file) {

		List<String> pdbCodes = new ArrayList<>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line=br.readLine())!=null) {
				if (line.trim().isEmpty()) continue;
				if (line.startsWith("#")) continue; // comments are allowed in file
				pdbCodes.add(line);

			}
			br.close();
		} catch (IOException e) {
            logger.error("Problem reading list file {}, can't continue", file);
			System.exit(1);
		}
		return pdbCodes;
	}

	private static List<JobDir> listSerializedFiles(List<File> dirs, boolean isDividedLayout) {

		List<JobDir> serializedFiles = new ArrayList<>();
		for (File dir : dirs) {
			//Check if it really is a directory
			if (!dir.isDirectory()) {
				if (choosefromFile != null)
					logger.warn("Job id {} specified in list file (-f), but directory {} is not present. Skipping",
							dir.getName(), dir);
				continue;
			}

			//Check for 4 letter code directories starting with a number, only in divided case
			if (isDividedLayout && !dir.getName().matches("^\\d\\w\\w\\w$")) {
				logger.info("Dir name doesn't look like a PDB code, skipping directory {}", dir);
				continue;
			}

			String inputName = null;
			File pdbInfoSerializedFile = null;
			if (isDividedLayout) {
				pdbInfoSerializedFile = new File(dir, dir.getName() + SERIALIZED_FILE_SUFFIX);
				if (!pdbInfoSerializedFile.exists())
					pdbInfoSerializedFile = null;

			} else {
				for (File f : dir.listFiles()) {
					if (f.getName().endsWith(SERIALIZED_FILE_SUFFIX)) {
						pdbInfoSerializedFile = f;
					}
				}

				Optional<File> mostRecentFile =
						Arrays
								.stream(dir.listFiles())
								.filter(File::isFile)
								.min(Comparator.comparingLong(File::lastModified));

				if (mostRecentFile.isPresent()) {
					inputName = mostRecentFile.get().getName();
					logger.info("Input name for job {} is {}", dir.getName(), inputName);
					if (!inputName.toLowerCase().endsWith(".pdb") &&
							!inputName.toLowerCase().endsWith(".cif") &&
							!inputName.toLowerCase().endsWith(".pdb.gz") &&
							!inputName.toLowerCase().endsWith(".cif.gz")) {
						logger.warn("Input name '{}' found in dir {} (via looking for oldest file in dir) does not have one of the usual pdb/cif extensions",
								inputName, dir);
					}
				} else {
					logger.warn("Could not find the oldest file in dir {}. inputName won't be available", dir);
				}
			}

			// we keep the null serializedFiles to persist error jobs
			serializedFiles.add(new JobDir(dir, pdbInfoSerializedFile, inputName));
		}
		return serializedFiles;
	}

	private static Stats loadSet(JobDir[] jobsDirectories) {

		Stats stats = new Stats();

		if (!full) {
			long avgTimeStart = 0;
			int i = -1;
			// insert 1 by 1
			for (JobDir jobDirectory : jobsDirectories) {
				i++;

				String jobId = jobDirectory.dir.getName();
				persistOne(stats, jobDirectory, jobId);

				if (i % TIME_STATS_EVERY == 0) {
					long avgTimeEnd = System.currentTimeMillis();
					if (i != 0) // no statistics before starting
                        logger.info("Last " + TIME_STATS_EVERY + " entries in {} s", (avgTimeEnd - avgTimeStart) / 1000);
					avgTimeStart = System.currentTimeMillis();
				}
			}
		} else {
			List<JobDir> currentBatch = new ArrayList<>();
			// insert by batches
			for (int i=0; i<jobsDirectories.length; i++) {
				currentBatch.add(jobsDirectories[i]);
				if (currentBatch.size() == batchSize) {
					persistBatch(stats, currentBatch, i);
					currentBatch = new ArrayList<>();
				}
			}
			// finally persist the last batch if there is one
			if (!currentBatch.isEmpty()) {
				persistBatch(stats, currentBatch, jobsDirectories.length-1);
			}
		}
		return stats;
	}

	/**
	 * Persist one job, either normally or with error job. Returns false if the serialized file
	 * exists but can't be deserialized.
	 * @param stats
	 * @param jobDirectory
	 * @param jobId
	 * @return
	 */
	private static void persistOne(Stats stats, JobDir jobDirectory, String jobId) {
		if(jobDirectory.serializedFile !=null) {

			EntryData entryData = readSerializedFile(jobDirectory.serializedFile);
			// if something goes wrong while reading the file (a warning message is already printed in readFromSerializedFile)
			if (entryData == null) {
				logger.error("Could not deserialize job {}. Skipping", jobDirectory.dir.getName());
				return;
			}
			try {
				long start = System.currentTimeMillis();
				try {
					// if this doesn't throw a NoResultException it means that it exists
					dao.getPDBInfo(jobId);
					logger.info("Existing PDBInfo document found for {}. Will remove all data for it before inserting a new one", jobId);
					long delPdbInfo = dao.remove(jobId);
					long delInterfRes = interfResDao.remove(jobId);
					logger.info("Done deleting data for id {}. Deleted {} PDBInfo documents and {} InterfaceResidueFeature documents", jobId, delPdbInfo, delInterfRes);
				} catch (NoResultException e) {
					logger.info("No PDBInfo document exists for id {}. No need to delete data from db for this id.", jobId);
				}
				dao.insertPDBInfo(entryData.getPdbInfoDB());
				interfResDao.insertInterfResFeatures(entryData.getInterfResFeaturesDB());
				long end = System.currentTimeMillis();
				logger.info("Done inserting {}. Time: {}ms", jobId, (end - start));
				stats.countUploaded++;

			} catch (DaoException e) {
                logger.warn("Problem persisting: {}: {}", jobId, e.getMessage());
				stats.pdbsWithWarnings.add(jobId);
			}
		} else {
			// TODO error jobs
			//dbh.persistErrorJob(em, jobId);
			stats.countErrorJob++;
		}
	}

	private static void persistBatch(Stats stats, List<JobDir> jobDirs, int endIndexOfBatch) {
		logger.info("Processing batch with end index {} (size {})", endIndexOfBatch, jobDirs.size());
		List<PdbInfoDB> pdbInfoBatch = new ArrayList<>();
		List<InterfaceResidueFeaturesDB> currentInterfResFeatBatch = new ArrayList<>();
		try {
			for (int i = 0; i < jobDirs.size(); i++) {
				if (jobDirs.get(i).serializedFile != null) {
					EntryData entryData = readSerializedFile(jobDirs.get(i).serializedFile);
					// if something goes wrong while reading the file (a warning message is already printed in readFromSerializedFile)
					if (entryData == null) {
						logger.error("Could not deserialize job {}. Skipping", jobDirs.get(i).dir.getName());
						continue;
					}
					pdbInfoBatch.add(entryData.getPdbInfoDB());
					currentInterfResFeatBatch.addAll(entryData.getInterfResFeaturesDB());

				} else {
					// TODO error jobs
					//dbh.persistErrorJob(em, jobId);
					stats.countErrorJob++;
				}

				if (currentInterfResFeatBatch.size() >= batchSize) {
					long start = System.currentTimeMillis();
					interfResDao.insertInterfResFeatures(currentInterfResFeatBatch);
					long end = System.currentTimeMillis();
					logger.info("Done inserting InterfaceResidueFeaturesDB sub-batch with end index {} (size {}), corresponding to batch with end index {}. Time: {}ms", i, currentInterfResFeatBatch.size(), endIndexOfBatch, (end - start));
					currentInterfResFeatBatch = new ArrayList<>();
				}
			}
			long start = System.currentTimeMillis();
			dao.insertPDBInfos(pdbInfoBatch);
			long end = System.currentTimeMillis();
			logger.info("Done inserting PdbInfoDB batch with end index {} (size {}). Time: {}ms", endIndexOfBatch, batchSize, (end - start));
			stats.countUploaded += pdbInfoBatch.size();
			// insert last batch of interf res features
			if (!currentInterfResFeatBatch.isEmpty()) {
				start = System.currentTimeMillis();
				interfResDao.insertInterfResFeatures(currentInterfResFeatBatch);
				end = System.currentTimeMillis();
				logger.info("Done inserting InterfaceResidueFeaturesDB sub-batch with end index {} (size {}), corresponding to batch with end index {}. Time: {}ms", jobDirs.size() - 1, currentInterfResFeatBatch.size(), endIndexOfBatch, (end - start));
			}
		} catch (DaoException e) {
			List<String> ids = jobDirs.stream().map(j->j.dir.getName()).toList();
			logger.warn("Failed to write batch with end index {} (size {}). List of ids: {}", endIndexOfBatch, jobDirs.size(), ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
			stats.pdbsWithWarnings.addAll(ids);
		}
	}

	private static class Stats {
		int countUploaded = 0;
		int countErrorJob = 0;
		List<String> pdbsWithWarnings = new ArrayList<>();

		public void add(Stats stats) {
			this.countUploaded += stats.countUploaded;
			this.countErrorJob += stats.countErrorJob;
			pdbsWithWarnings.addAll(stats.pdbsWithWarnings);
		}
	}

	private static class JobDir {
		File dir;
		File serializedFile;
		String inputName;
		public JobDir(File dir, File serializedFile, String inputName) {
			this.dir = dir;
			this.serializedFile = serializedFile;
			this.inputName = inputName;
		}
	}

}
