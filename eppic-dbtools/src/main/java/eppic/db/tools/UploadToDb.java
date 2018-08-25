package eppic.db.tools;

import eppic.model.db.PdbInfoDB;
import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;


public class UploadToDb {

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

	public static void main(String[] args) {
		
		String help = 
				"Usage: UploadToDB\n" +
				"  -D <string>  : the database name to use\n"+
				"  -d <dir>     : root directory of eppic output files with subdirectories as PDB codes \n" +
				" [-l]          : if specified subdirs under root dir (-d) are considered to be in PDB divided layout\n"+
				"                 (this affects the behaviour of -d and -f).\n"+
				"                 Default: subdirs under root are taken directly as PDB codes \n"+
				" [-f <file>]   : file specifying a list of PDB codes indicating subdirs of root \n"+
				"                 directory to take (default: uses all subdirs in the root directory) \n" +
				" [-g <file>]   : a configuration file containing the database access parameters, if not provided\n" +
				"                 the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n" +
				" OPERATION MODE\n" +
				" Default operation: only entries not already present in database will be inserted \n"+
				" [-F]          : forces everything chosen to be inserted, deletes previous entries if present\n" +
				" [-r]          : removes the specified entries from database\n";
				

		boolean isDividedLayout = false;
		
		File jobDirectoriesRoot = null;

		Getopt g = new Getopt("UploadToDB", args, "D:d:lf:g:Frh?");
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
		File[] jobsDirectories;
		if(choosefromFile!=null){
			List<String> pdbCodes = readListFile(choosefromFile);
			List<File> chosenFilesList = new ArrayList<>();
			for (String pdbCode:pdbCodes) {
				chosenFilesList.add(getDirectory(isDividedLayout, jobDirectoriesRoot, pdbCode));
			}
			jobsDirectories = chosenFilesList.toArray(new File[chosenFilesList.size()]);
		}
		else {
			jobsDirectories = listAllDirs(isDividedLayout, jobDirectoriesRoot);
		}
		
		//Print the MODE of usage
		if(modeNew) System.out.println("\n\nMODE SELECTED: Insert New Entries\n");
		if(modeEverything) System.out.println("\n\nMODE SELECTED: Force Insert, which will insert everything in DB\n");
		if(modeRemove) System.out.println("\n\nMODE SELECTED: Remove entries from DB\n");
		
		if (isDividedLayout) {
			System.out.println("Directories under "+jobDirectoriesRoot+" will be considered to have PDB divided layout, i.e. "+jobDirectoriesRoot+File.separatorChar+DIVIDED_ROOT+File.separatorChar+"<PDB-code-middle-2-letters>");
		} else {
			System.out.println("Directories under "+jobDirectoriesRoot+" will be considered to be PDB codes directly, no PDB divided layout will be used. ");
		}


		List<String> pdbsWithWarnings = new ArrayList<>();

		long totalStart = System.currentTimeMillis();

		Counts counts = loadSet(jobsDirectories, pdbsWithWarnings);
		
		long totalEnd = System.currentTimeMillis();
		
		System.out.println("Completed all "+jobsDirectories.length+" entries in "+((totalEnd-totalStart)/1000)+" s");
		if (modeNew) {
			System.out.println("Already present: "+counts.countPresent+", uploaded: "+counts.countUploaded+", couldn't insert: "+pdbsWithWarnings.size());
			System.out.println("There were "+counts.countErrorJob+" error jobs in "+counts.countUploaded+" uploaded entries.");
		}
		if (modeRemove) System.out.println("Removed: "+counts.countRemoved);

		if (!pdbsWithWarnings.isEmpty()) {
			System.out.println("These PDBs had problems while inserting to db: ");
			for (String pdb:pdbsWithWarnings) {
				System.out.print(pdb+" ");
			}
			System.out.println();
		}

		// make sure we exit with an error state in cases with many failures
		int maxFailuresTolerated = (counts.countPresent + counts.countUploaded)/2;
		if (pdbsWithWarnings.size() > maxFailuresTolerated) {
			System.err.println("Total of "+pdbsWithWarnings.size()+" failures, more than "+maxFailuresTolerated+" failures. Something must be wrong!"); 
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
	
	private static File[] listAllDirs(boolean isDividedLayout, File rootDir) {
		if (isDividedLayout) {
			File dividedRoot = new File(rootDir,DIVIDED_ROOT);
			
			List<File> allDirs = new ArrayList<File>();
			
			for (File indexDir:dividedRoot.listFiles()) {
				for (File dir:indexDir.listFiles()) {
					allDirs.add(dir);
				}
			}
						
			return allDirs.toArray(new File[allDirs.size()]);
			
		} else {
			return rootDir.listFiles();
		}
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
		
		List<String> pdbCodes = new ArrayList<String>();

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

	private static Counts loadSet(File[] jobsDirectories, List<String> pdbsWithWarnings) {
		DBHandler dbh = new DBHandler(dbName, configFile);

		// Start the Process
		int i = -1;
		long avgTimeStart1 = 0;
		long avgTimeEnd1 = 0;
		long avgTimeStart2 = 0;
		long avgTimeEnd2 = 0;

		Counts counts = new Counts();


		EntityManager em = dbh.getEntityManager();
		for (File jobDirectory : jobsDirectories) {
			i++;

			//Check if it really is a directory
			if (!jobDirectory.isDirectory()){
				if(choosefromFile!=null)
					System.err.println("Warning: "+jobDirectory.getName()+" specified in list file (-f), " +
							"but directory "+jobDirectory+"is not present, Skipping");
				continue;
			}

			//Check for 4 letter code directories starting with a number
			if (!jobDirectory.getName().matches("^\\d\\w\\w\\w$")){
				System.out.println("Dir name doesn't look like a PDB code, skipping directory " + jobDirectory);
				continue;
			}


			System.out.print(jobDirectory.getName()+" ");

			long start = System.currentTimeMillis();

			String currentPDB = jobDirectory.getName();


			// Get the PDB-Score Item to be read
			File webuiFile = new File(jobDirectory, currentPDB + ".webui.dat");
			boolean isSerializedFilePresent = webuiFile.isFile();

			try {


				//MODE FORCE
				if (modeEverything) {
					boolean ifRemoved = dbh.removeJob(currentPDB);
					if (ifRemoved) System.out.print(" Found.. Removing and Updating.. ");
					else System.out.print(" Not Found.. Adding.. ");
					if(isSerializedFilePresent) {
						PdbInfoDB pdbScoreItem = readFromSerializedFile(webuiFile);
						// if something goes wrong while reading the file (a warning message is already printed in readFromSerializedFile)
						if (pdbScoreItem == null) continue;

						dbh.persistFinishedJob(em,pdbScoreItem);
					}
					else {
						dbh.persistErrorJob(em, currentPDB);
					}
					//continue;
				}

				//MODE NEW INSERT
				if (modeNew){
					int isPresent = dbh.checkJobExist(currentPDB);
					if(isPresent == 0){ // not present
						System.out.print(" Not Present.. Adding.. ");
						if(isSerializedFilePresent) {
							PdbInfoDB pdbScoreItem = readFromSerializedFile(webuiFile);
							// if something goes wrong while reading the file (a warning message is already printed in readFromSerializedFile)
							if (pdbScoreItem == null) continue;

							dbh.persistFinishedJob(em,pdbScoreItem);
						}
						else {
							dbh.persistErrorJob(em, currentPDB);
							counts.countErrorJob++;
						}
						counts.countUploaded++;
					} else if (isPresent ==2) {
						// already present but as an error, we have to remove and reinsert
						boolean ifRemoved = dbh.removeJob(currentPDB);
						if (ifRemoved) System.out.print(" Found as error.. Removing and Updating.. ");
						else {
							System.err.print(" Warning! Not Found, but there should be an error job. Skipping..");
							continue;
						}

						if(isSerializedFilePresent) {
							PdbInfoDB pdbScoreItem = readFromSerializedFile(webuiFile);
							// if something goes wrong while reading the file (a warning message is already printed in readFromSerializedFile)
							if (pdbScoreItem == null) continue;

							dbh.persistFinishedJob(em,pdbScoreItem);
						}
						else {
							dbh.persistErrorJob(em, currentPDB);
							counts.countErrorJob++;
						}
						counts.countUploaded++;

					} else {
						// already present and is not an error
						counts.countPresent++;
						System.out.print(" Already Present.. Skipping.. ");
					}
					//continue;
				}

				//MODE REMOVE
				if (modeRemove) {
					boolean ifRemoved = dbh.removeJob(currentPDB);
					if (ifRemoved) {
						System.out.print(" Removed.. ");
						counts.countRemoved++;
					}
					else {
						System.out.print(" Not Found.. ");
					}
					//continue;
				}

				long end = System.currentTimeMillis();
				System.out.print(((end-start)/1000)+"s\n");

				if (i%TIME_STATS_EVERY1==0) {
					avgTimeEnd1 = System.currentTimeMillis();

					if (i!=0) // no statistics before starting
						System.out.println("Last "+TIME_STATS_EVERY1+" entries in "+((avgTimeEnd1-avgTimeStart1)/1000)+" s");

					avgTimeStart1 = System.currentTimeMillis();
				}

				if (i%TIME_STATS_EVERY2==0) {
					avgTimeEnd2 = System.currentTimeMillis();

					if (i!=0) // no statistics before starting
						System.out.println("Last "+TIME_STATS_EVERY2+" entries in "+((avgTimeEnd2-avgTimeStart2)/1000)+" s");

					avgTimeStart2 = System.currentTimeMillis();
				}


			} catch (Exception e) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				System.err.println("WARNING: problems while inserting "+currentPDB+". Error: "+e.getMessage());
				pdbsWithWarnings.add(currentPDB);
			}
		}
		return counts;
	}

	private static class Counts {
		int countPresent = 0;
		int countUploaded = 0;
		int countRemoved = 0;
		int countErrorJob = 0;
	}
}
