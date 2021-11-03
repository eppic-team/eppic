package eppic.db.loaders;

import eppic.db.mongoutils.DbPropertiesReader;
import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO implement with mongo
public class UserJobCopier {

	private static boolean isUserJob(String str){
		return str.matches("^[a-zA-Z]\\w{29}$");
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{

		String help = 
				"Usage: UserJobCopier\n" +
						" Script to copy or remove user jobs from database \n" +						
						"   -d <dir>   : root dir of eppic output files with subdirs as job names \n" +
						"  [-s]        : source DB name from where to copy jobs\n" +
						"  [-g]        : target DB name into which jobs will be copied\n" +
						"  [-r]        : remove jobs from source database. Will only remove jobs \n"+
						"                selected in source DB without copying anything (-g is ignored)\n"+
						"  [-t <int>]  : select jobs from last <int> days (default: select all jobs)\n" +
						"  [-f <file>] : select only the job identifiers listed in given file\n" +
						"  [-F]        : force removal of entries in target database if already present\n"+
						"  [-c <file>] : a configuration file containing the database access parameters, if not provided\n" +
						"                the config will be read from file "+ DbPropertiesReader.DEFAULT_CONFIG_FILE_NAME +" in home dir\n";

		File jobDirectoriesRoot = null;
		
		boolean remove = false;

		boolean force = false;

		String userDays = null;
		Date afterDate = null;
		boolean selTime = false;

		File selFromFile = null;
		
		String sourceDbName = null;
		String targetDbName = null;
		
		File configFile = null;

		Getopt g = new Getopt("UserJobCopier", args, "d:s:g:rt:f:Fc:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'd':
				jobDirectoriesRoot = new File(g.getOptarg());
				break;
			case 's':
				sourceDbName = g.getOptarg();
				break;
			case 'g':
				targetDbName = g.getOptarg();
				break;
			case 'r':
				remove = true;
				break;
			case 't':
				selTime = true;
				userDays = g.getOptarg();
				break;
			case 'f':
				selFromFile = new File(g.getOptarg());
				break;
			case 'F':
				force = true;
				break;
			case 'c':
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

		if (!remove && (sourceDbName == null || targetDbName == null) ) {
			System.err.println("Need both a source db name (-s) and a target db name (-g)");
			System.exit(1);
		}
		
		if (remove && sourceDbName == null) {
			System.err.println("If in remove mode (-r) then a source db name is needed (-s)");
			System.exit(1);
		}
		
		//Check if Job-Directory provided
		if (jobDirectoriesRoot == null || ! jobDirectoriesRoot.isDirectory() ){
			System.err.println("\n\nNo directory specified or directory specified with -d does not exist \n");
			System.err.println(help);
			System.exit(1);
		}

		if(selTime){
			try {
				//Convert Days to epoc-time in milliseconds
				long userSecs = Math.abs(Long.parseLong(userDays) * 86400000) ;
				afterDate = new Date(new Date().getTime() - userSecs) ;
			} catch (NumberFormatException ex) {
				System.err.println("\n\nError in reading option -t. Expecting an integer\n");
				System.err.println(help);
				System.exit(1);
			}
		}

		if ( selFromFile!=null && !selFromFile.isFile() ) {
			System.err.println("\n\nFile given with -f can not be found \n");
			System.err.println(help);
			System.exit(1);
		}
		
		// initialising the db connections
//		DBHandler sourceDbh = new DBHandler(sourceDbName, configFile);
//		DBHandler targetDbh = new DBHandler(targetDbName, configFile);
//
//
//		//Print Mode of Usage
//
//		if(remove) {
//			System.out.println("\n\nRemoving data from source DB '"+sourceDbName+"'\n");
//		} else {
//			System.out.println("\n\nCopying data from source DB '"+sourceDbName+"' to target DB '"+targetDbName+"'\n");
//		}
//		if(force) System.out.println("Forcing to delete and copy data");
//
//		// Get list of jobid's from database to be processed
//		List<String> jobs = new ArrayList<String>();
//
//		if (selTime) {
//			jobs = sourceDbh.getUserJobList(afterDate);
//			System.out.println("Selected user jobs from source DB after date " + afterDate +
//					": "+jobs.size()+" jobs in total.");
//		} else {
//			jobs = sourceDbh.getUserJobList();
//			System.out.println("Selected all user jobs from source DB: "+jobs.size()+" jobs in total.");
//		}
//
//		if (selFromFile!=null) {
//			System.out.println("Selecting user jobs from file " + selFromFile +" ...");
//			BufferedReader selFromFileRead = new BufferedReader(new FileReader(selFromFile));
//			String line;
//			while ((line=selFromFileRead.readLine())!=null) {
//				if(isUserJob(line)) jobs.add(line);
//				else System.err.println("Line in File < " + line + " > is not a user job. Skipping...");
//			}
//			selFromFileRead.close();
//			System.out.println("Read "+jobs.size()+" valid user job identifiers from file ");
//		}
//
//		//Start Processing
//
//		for (String job:jobs) {
//			long start = System.currentTimeMillis();
//
//			System.out.print(job + " : ");
//
//			File jobDir = new File(jobDirectoriesRoot, job);
//			if(!jobDir.isDirectory()){
//				System.out.println(" Directory "+jobDir+" not present. Skipping... ");
//				continue;
//			}
//
//			//Check if jobs are really present in the source DB (can only happen when selecting jobs from file)
//			if (selFromFile!=null && sourceDbh.checkJobExist(job)==0) {
//				System.out.println(" Job "+job+" not present in source DB '"+sourceDbName+"'. Skipping... ");
//				continue;
//			}
//
//			try {
//
//				if (!remove) {
//					// MODE COPY
//					if(targetDbh.checkJobExist(job)!=0) {
//						System.out.print(" Already present in target DB. ");
//						if (force) {
//							System.out.print(" Removing and copying... ");
//							targetDbh.removeJob(job);
//							sourceDbh.copytoDB(targetDbh, jobDir);
//						} else System.out.print(" Skipping... ");
//
//					} else {
//						System.out.print(" Copying... ");
//						sourceDbh.copytoDB(targetDbh, jobDir);
//					}
//
//				} else {
//					//MODE REMOVE
//					if(sourceDbh.checkJobExist(job)!=0) {
//						System.out.print(" Present in source DB. Removing... ");
//						sourceDbh.removeJob(job);
//					} else {
//						System.out.print(" Not present in source DB. Skipping... ");
//					}
//				}
//
//
//			} catch (Exception e) {
//				System.err.print("Unexpected error occured while processing job "+job);
//				System.err.println(e.getMessage());
//				//System.err.println(e.getStackTrace());
//			}
//
//			long end = System.currentTimeMillis();
//			System.out.print(((end-start)/1000)+"s\n");
//
//		}
//
	}//end Main

}
