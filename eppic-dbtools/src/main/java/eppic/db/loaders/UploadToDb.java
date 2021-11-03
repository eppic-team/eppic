package eppic.db.loaders;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.mongoutils.DbPropertiesReader;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.db.PdbInfoDB;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// TODO this now needs to write both PdbInfoDB and InterfaceResidueFeatures (stored in separate files and separate collections in mongo)
public class UploadToDb {

    private static final Logger logger = LoggerFactory.getLogger(UploadToDb.class);

	// the name of the dir that is the root of the divided dirs (indexed by the pdbCode middle 2-letters) 
	private static final String DIVIDED_ROOT = "divided";
	
	// after this number of entry uploads time statistics will be produced
	private static final int TIME_STATS_EVERY1 = 100;
	private static final int TIME_STATS_EVERY2 = 1000;


	private static boolean modeNew = true;
	private static boolean modeEverything = false;
	private static boolean modeRemove = false;
	private static String dbName = null;
	private static File configFile = null;
	private static File choosefromFile = null;
	private static int numWorkers = 1;

	private static PDBInfoDAO dao;

	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
		
		String help = 
				"Usage: UploadToDB\n" +
				"  -D <string>  : the database name to use\n"+
				"  -d <dir>     : root dir of eppic output files with subdirs as job dirs (user jobs only, no PDB ids jobs) \n" +
				" [-l]          : if specified, subdirs under root dir (-d) are considered to be in PDB divided \n" +
				"                 layout and leaf dirs must be PDB ids (this affects the behaviour of -d and -f).\n"+
				"                 Default: subdirs under root are taken directly as job dirs (user jobs) \n"+
				" [-f <file>]   : file specifying a list of PDB ids indicating subdirs of root \n"+
				"                 directory to take (default: uses all subdirs in the root directory) \n" +
				" [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
				"                 the config will be read from file "+ DbPropertiesReader.DEFAULT_CONFIG_FILE_NAME+" in home dir\n" +
				" [-n <int>]    : number of workers. Default 1. \n"+
				" OPERATION MODE\n" +
				" Default operation: only entries not already present in database will be inserted \n"+
				" [-F]          : forces everything chosen to be inserted, deletes previous entries if present\n" +
				" [-r]          : removes the specified entries from database\n";
				

		boolean isDividedLayout = false;
		
		File jobDirectoriesRoot = null;

		Getopt g = new Getopt("UploadToDB", args, "D:d:lf:g:n:Frh?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'D':
				dbName = g.getOptarg();
				break;
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
				modeEverything = true;
				modeNew = false;
				modeRemove = false;
				break;
			case 'r':
				modeRemove = true;
				modeNew = false;
				modeEverything = false;
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
		
		if (jobDirectoriesRoot == null || ! jobDirectoriesRoot.isDirectory() ){
			System.err.println("\n\nHey Jim, Output Directory not specified correctly! \n");
			System.err.println(help);
			System.exit(1);
		}
		
		
		//only one of the three should be true
		if(!( ( modeNew && !modeEverything && !modeRemove ) || 
			  (!modeNew &&  modeEverything && !modeRemove ) ||
			  (!modeNew && !modeEverything &&  modeRemove ) ) ){
			System.err.println("\n\nHey Jim, Combinations of MODE -F / -r not acceptable! \n");
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

		//Print the MODE of usage
		if(modeNew) System.out.println("\n\nMODE SELECTED: Insert New Entries\n");
		if(modeEverything) System.out.println("\n\nMODE SELECTED: Force Insert, which will insert everything in DB\n");
		if(modeRemove) System.out.println("\n\nMODE SELECTED: Remove entries from DB\n");
		
		if (isDividedLayout) {
			System.out.println("Directories under "+jobDirectoriesRoot+" will be considered to have PDB divided layout, i.e. "+jobDirectoriesRoot+File.separatorChar+DIVIDED_ROOT+File.separatorChar+"<PDB-code-middle-2-letters>");
		} else {
			System.out.println("Directories under "+jobDirectoriesRoot+" will be considered to be user jobs, no PDB divided layout will be used. ");
		}

		System.out.println("Will use " + numWorkers + " workers");

		DbPropertiesReader propsReader = new DbPropertiesReader(configFile);
		String connUri = propsReader.getMongoUri();

		MongoDatabase mongoDb = MongoUtils.getMongoDatabase(dbName, connUri);

		dao = new PDBInfoDAOMongo(mongoDb);

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

			System.out.printf("Proceeding to submit to worker %d, set of size %d\n", i, singleSet.length);

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
		
		System.out.println("Completed all "+jobDirs.size()+" entries in "+((totalEnd-totalStart)/1000)+" s");
		if (modeNew) {
			System.out.println("Already present: "+ allStats.countPresent+", uploaded: "+ allStats.countUploaded+", couldn't insert: "+allStats.pdbsWithWarnings.size());
			System.out.println("There were "+ allStats.countErrorJob+" error jobs in "+ allStats.countUploaded+" uploaded entries.");
		}
		if (modeRemove) System.out.println("Removed: "+ allStats.countRemoved);

		if (!allStats.pdbsWithWarnings.isEmpty()) {
			System.out.println("These PDBs had problems while inserting to db: ");
			for (String pdb:allStats.pdbsWithWarnings) {
				System.out.print(pdb+" ");
			}
			System.out.println();
		}

		// make sure we exit with an error state in cases with many failures
		int maxFailuresTolerated = (allStats.countPresent + allStats.countUploaded)/2;
		if (allStats.pdbsWithWarnings.size() > maxFailuresTolerated) {
			System.err.println("Total of "+allStats.pdbsWithWarnings.size()+" failures, more than "+maxFailuresTolerated+" failures. Something must be wrong!");
			System.exit(1);
		}

	}
	
	private static PdbInfoDB readFromSerializedFile(File webuiFile) {
		PdbInfoDB pdbScoreItem = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(webuiFile));
			pdbScoreItem = (PdbInfoDB)in.readObject();
			in.close();
		} catch (IOException|ClassNotFoundException e) {
			System.err.println("Problem reading serialized file, skipping entry"+webuiFile+". Error: "+e.getMessage());
		}
		// will be null if an exception occurs
		return pdbScoreItem;
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
			System.err.println("Problem reading list file "+file+", can't continue");
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
			File serializedFile = null;
			if (isDividedLayout) {
				serializedFile = new File(dir, dir.getName() + ".webui.dat");
				if (!serializedFile.exists())
					serializedFile = null;

			} else {
				for (File f : dir.listFiles()) {
					if (f.getName().endsWith(".webui.dat")) {
						serializedFile = f;
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
			serializedFiles.add(new JobDir(dir, serializedFile, inputName));
		}
		return serializedFiles;
	}

	private static Stats loadSet(JobDir[] jobsDirectories) {

		// Start the Process
		int i = -1;
		long avgTimeStart1 = 0;
		long avgTimeEnd1 = 0;
		long avgTimeStart2 = 0;
		long avgTimeEnd2 = 0;

		Stats stats = new Stats();

		for (JobDir jobDirectory : jobsDirectories) {
			i++;

			long start = System.currentTimeMillis();

			String jobId = jobDirectory.dir.getName();

			try {

			    String msg = "";

				//MODE FORCE
//				if (modeEverything) {
//					boolean ifRemoved = dbh.removeJob(jobId);
//					if (ifRemoved) msg = "Found.. Removing and Updating.. ";
//					else msg = "Not Found.. Adding.. ";
//					if (!persistOne(stats, jobDirectory, jobId)) continue;
//				}

				//MODE NEW INSERT
				if (modeNew){
					// TODO
					int isPresent = 0;// dbh.checkJobExist(jobId);
					if(isPresent == 0){ // not present
						msg = "Not Present.. Adding.. ";
						if (!persistOne(stats, jobDirectory, jobId)) continue;

					} else if (isPresent ==2) {
						// already present but as an error, we have to remove and reinsert
						// TODO
						boolean ifRemoved = false;//dbh.removeJob(jobId);
						if (ifRemoved) msg = "Found as error.. Removing and Updating.. ";
						else {
							logger.warn("{} Not Found, but there should be an error job. Skipping..", jobId);
							continue;
						}

						if (!persistOne(stats, jobDirectory, jobId)) continue;

					} else {
						// already present and is not an error
						stats.countPresent++;
						msg = "Already Present.. Skipping.. ";
					}
				}

				//MODE REMOVE
//				if (modeRemove) {
//					boolean ifRemoved = dbh.removeJob(jobId);
//					if (ifRemoved) {
//						msg = "Removed.. ";
//						stats.countRemoved++;
//					}
//					else {
//						msg = "Not Found.. ";
//					}
//				}

				long end = System.currentTimeMillis();
                logger.info("{} {} : {} s", jobId, msg, ((end-start)/1000));

				if (i%TIME_STATS_EVERY1==0) {
					avgTimeEnd1 = System.currentTimeMillis();

					if (i!=0) // no statistics before starting
                        logger.info("Last "+TIME_STATS_EVERY1+" entries in "+((avgTimeEnd1-avgTimeStart1)/1000)+" s");

					avgTimeStart1 = System.currentTimeMillis();
				}

				if (i%TIME_STATS_EVERY2==0) {
					avgTimeEnd2 = System.currentTimeMillis();

					if (i!=0) // no statistics before starting
						logger.info("Last "+TIME_STATS_EVERY2+" entries in "+((avgTimeEnd2-avgTimeStart2)/1000)+" s");

					avgTimeStart2 = System.currentTimeMillis();
				}


			} catch (Exception e) {
				logger.warn("Problems while inserting "+jobId+". Error: "+e.getMessage());
				stats.pdbsWithWarnings.add(jobId);
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
	private static boolean persistOne(Stats stats, JobDir jobDirectory, String jobId) {
		if(jobDirectory.serializedFile!=null) {
			PdbInfoDB pdbScoreItem = readFromSerializedFile(jobDirectory.serializedFile);
			// if something goes wrong while reading the file (a warning message is already printed in readFromSerializedFile)
			if (pdbScoreItem == null) return false;
			try {
				dao.insertPDBInfo(pdbScoreItem);
			} catch (DaoException e) {
				System.err.println("Error persisting: " + jobId + ": " + e.getMessage());
			}
			//dbh.persistFinishedJob(em, pdbScoreItem, jobId, jobDirectory.inputName);
		}
		else {
			// TODO error jobs
			//dbh.persistErrorJob(em, jobId);
			stats.countErrorJob++;
		}
		stats.countUploaded++;
		return true;
	}

	private static class Stats {
		int countPresent = 0;
		int countUploaded = 0;
		int countRemoved = 0;
		int countErrorJob = 0;
		List<String> pdbsWithWarnings = new ArrayList<>();

		public void add(Stats stats) {
			this.countPresent += stats.countPresent;
			this.countUploaded += stats.countUploaded;
			this.countRemoved += stats.countRemoved;
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
