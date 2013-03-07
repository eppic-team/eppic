package ch.systemsx.sybit.crkwebui.server.db.util;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserJobDBHandler {

	private static DBHandler dbhOnline = new DBHandler();
	private static DBHandler dbhOffline = new DBHandler(DBHandler.DEFAULT_OFFLINE_JPA);
	
	public static boolean isUserJob(String str){
		return str.matches("^[a-zA-Z]\\w{29}$");
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		String help = 
				"Usage: UserJobDBHandler\n" +
				"Perform operations on user-jobs in the database \n" +
				" (In conflicting cases, options you specify later override the earlier ones)\n" +
				"   -d <dir>   : Root Directory of eppic output files with subdirectories as job names \n" +
				" Mode of FUNCTION \n" +
				"  [-b]        : (Default) Back-up jobs (Copies from online to offline database)\n" +
				"  [-s]        : Restores jobs (Copies from offline to online database)\n" +
				"  [-r]        : Removes jobs from online database\n" +
				"  [-R]		   : Removes jobs from offline database\n" +
				" SELECTION of jobs \n" +
				"  [-a]		   : (Default) Selects all jobs in Database\n" +
				"  [-t <int>]  : Selects jobs from last <int> days\n" +
				"  [-f <file>] : Selects job-ids from a file\n" +
				" OTHER OPTIONS \n" +
				"  [-F]		   : Forces to remove the entries from destination database if already present\n\n";
		
		File jobDirectoriesRoot = null;
		boolean backup = true;
		boolean restore = false;
		boolean remove = false;
		boolean removeBackup = false;
		boolean force = false;
		
		boolean selAll = true;
		
		String userDays = null;
		Date afterDate = null;
		boolean selTime = false;
		
		boolean selFile = false;
		File selFromFile = null;
		
		Getopt g = new Getopt("UseJobDBHandler", args, "d:bsrRat:f:Fh?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'd':
				jobDirectoriesRoot = new File(g.getOptarg());
				break;
			case 'b':
				backup = true;
				restore = false;
				break;
			case 's':
				restore = true;
				backup = false;
				break;
			case 'r':
				remove = true;
				backup = false;
				break;
			case 'R':
				removeBackup = true;
				backup = false;
				break;
			case 'a':
				selAll = true;
				selTime = false;
				selFile = false;
				break;
			case 't':
				selTime = true;
				selAll = false;
				userDays = g.getOptarg();
				break;
			case 'f':
				selFile = true;
				selAll = false;
				selFromFile = new File(g.getOptarg());
				break;
			case 'F':
				force = true;
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
		
		//Check if Job-Directory provided
		if (jobDirectoriesRoot == null || ! jobDirectoriesRoot.isDirectory() ){
			System.err.println("\n\nHey Jim, Output Directory not specified correctly! \n");
			System.err.println(help);
			System.exit(1);
		}
		
		//Check if remove options are specified correctly
		if( (backup && removeBackup) || (restore && remove) ) {
			System.err.println("\n\nRemoving and adding to the destination directory at the same time is not possible\n");
			System.exit(1);
		}
		
		if(selTime){
			try{
				//Convert Days to epoc-time in milliseconds
				long userSecs = Math.abs(Long.parseLong(userDays) * 86400000) ;
				afterDate = new Date(new Date().getTime() - userSecs) ;
			}
			catch(NumberFormatException ex){
				System.err.println("\n\nError in reading time with option -t; Expecting an integer\n");
				System.err.println(help);
				System.exit(1);
			}
		}
		
		if ( (selFile && selFromFile == null) || (selFile && !selFromFile.isFile()) ){
			System.err.println("\n\nHey Jim, Option -f not specified correctly! Could not find the file!! \n");
			System.err.println(help);
			System.exit(1);
		}
		
		//Print Mode of Usage
		if(backup) System.out.println("\n\nBacking up data from Online to Offline DB\n");
		if(restore) System.out.println("\n\nRestoring data from Offline to Online DB\n");
		if(remove) System.out.println("\n\nRemoving data from Online DB\n");
		if(removeBackup) System.out.println("\n\nRemoving data from Offline DB\n");
		if(force) System.out.println("Forcing to delete and copy data");
		
		// Get list of jobid's from database to be processed
		List<String> jobs = new ArrayList<String>();
		
		if (selAll) {
			System.out.println("Selecting all User-Jobs from database..");
			if (backup || remove) jobs = dbhOnline.getUserJobList();
			if (restore || removeBackup) jobs = dbhOffline.getUserJobList();
		}
		
		if (selTime) {
			System.out.println("Selecting User-Jobs from database after " + afterDate +" ..");
			if (backup || remove) jobs = dbhOnline.getUserJobList(afterDate);
			if (restore || removeBackup) jobs = dbhOffline.getUserJobList(afterDate);
		}
		
		if (selFile) {
			System.out.println("Selecting User-Jobs from file " + selFromFile +" ..");
			BufferedReader selFromFileRead = new BufferedReader(new FileReader(selFromFile));
			String line;
			while ((line=selFromFileRead.readLine())!=null) {
				if(isUserJob(line)) jobs.add(line);
				else System.err.println("Line in File < " + line + " > is not a User-Job.. Skipping..");
			}
			selFromFileRead.close();
		}
		
		System.out.println("Total jobs selected for processing: " + jobs.size() );
		
		//Start Processing
		
		for (String job:jobs){
			long start = System.currentTimeMillis();
			
			System.out.print(job + " : ");
			
			File jobDir = new File(jobDirectoriesRoot, job);
			if(!jobDir.isDirectory()){
				System.out.println(" Directory for job output nor present.. Skipping.. ");
				continue;
			}
			
			//Check if jobs are present in the DB
			if( ((backup || remove) && !dbhOnline.checkfromDB(job) ) ||
				((restore || removeBackup) && !dbhOffline.checkfromDB(job)) ){
					System.out.println(" Not present in Database.. Skipping..");
					continue;
			}
			
			//MODE BACKUP
			if(backup){
				if(dbhOffline.checkfromDB(job)){
					System.out.print(" Already present in Offline DB.. ");
					if (force){
						System.out.print(" Removing and Copying.. ");
						dbhOffline.removefromDB(job);
						dbhOnline.copytoDB(dbhOffline, jobDir);
					}
					else System.out.print(" Skipping.. ");
				}
				else {
					System.out.print(" Copying.. ");
					dbhOnline.copytoDB(dbhOffline, jobDir);
				}
				//continue;
				
			}
			
			//MODE RESTORE
			if(restore){
				if(dbhOnline.checkfromDB(job)){
					System.out.print(" Already present in Online DB.. ");
					if (force){
						System.out.print(" Removing and Copying.. ");
						dbhOnline.removefromDB(job);
						dbhOffline.copytoDB(dbhOnline, jobDir);
					}
					else System.out.print(" Skipping.. ");
				}
				else {
					System.out.print(" Copying.. ");
					dbhOffline.copytoDB(dbhOnline, jobDir);
				}
				//continue;
			}
			
			//MODE REMOVE
			if(remove){
				if(dbhOnline.checkfromDB(job)){
					System.out.print(" Present in Online DB.. Removing.. ");
					dbhOnline.removefromDB(job);
				}
				else{
					System.out.print(" Not present in Online DB.. Skipping.. ");
				}
				//continue;
			}
			
			//MODE REMOVE BACKUP
			if(removeBackup){
				if(dbhOffline.checkfromDB(job)){
					System.out.print(" Present in Offline DB.. Removing.. ");
					dbhOffline.removefromDB(job);
				}
				else{
					System.out.print(" Not present in Offline DB.. Skipping.. ");
				}
				//continue;
			}
			
			long end = System.currentTimeMillis();
			System.out.print(((end-start)/1000)+"s\n");
			
		}
		
	}//end Main

}
