package ch.systemsx.sybit.crkwebui.server.db.util;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import model.PDBScoreItemDB;

public class UploadToDb {

	private static DBHandler dbh;
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */

	public static void main(String[] args) throws IOException {
		
		String help = 
				"Usage: UploadToDB\n" +
				"Uploads a set of eppic output files to database specified in "+DBHandler.DEFAULT_OFFLINE_JPA+" persistence unit\n" +
				"  -d <dir>  	: Root Directory of eppic output files with subdirectories as pdb names \n" +
				" [-f <file>] 	: File specifying the sub-directories to be used from output directory \n" +
				"                 (Default: Uses all files in the root directory) \n" +
				" [-o]          : Use the "+DBHandler.DEFAULT_ONLINE_JPA+" persistence unit instead of "+DBHandler.DEFAULT_OFFLINE_JPA+" \n" +
				" OPERATION MODE\n" +
				" (In conflicting cases, options you specify later override the earlier ones)\n" +
				" [-i]          : Inserts only the entries which are not in the database (Default mode)\n" +
				" [-F]          : Forces everything chosen to be inserted, deletes previous entries if present\n " +
				" [-r]          : Removes the specified files from database\n";

		File jobDirectoriesRoot = null;
		File choosefromFile = null;
		boolean choosefrom = false;
		boolean modeNew = true;
		boolean modeEverything = false;
		boolean modeRemove = false;
		boolean useOnlineJpa = false;

		Getopt g = new Getopt("UploadToDB", args, "d:f:oiFrh?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'd':
				jobDirectoriesRoot = new File(g.getOptarg());
				break;
			case 'f':
				choosefrom = true;
				choosefromFile = new File(g.getOptarg());
				break;
			case 'o':
				useOnlineJpa = true;
				break;
			case 'i':
				modeNew = true;
				modeEverything = false;
				modeRemove = false;
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

		if (jobDirectoriesRoot == null || ! jobDirectoriesRoot.isDirectory() ){
			System.err.println("\n\nHey Jim, Output Directory not specified correctly! \n");
			System.err.println(help);
			System.exit(1);
		}
		
		if ( choosefrom && choosefromFile == null ){
			System.err.println("\n\nHey Jim, Option -f not specified correctly! \n");
			System.err.println(help);
			System.exit(1);
		}
		
		//only one of the three should be true
		if(!( ( (modeNew) && !(modeEverything) && !(modeRemove) ) || 
			  (!(modeNew) &&   modeEverything &&  !(modeRemove) ) ||
			  (!(modeNew) && !(modeEverything) &&   modeRemove  ) ) ){
			System.err.println("\n\nHey Jim, Combinations of MODE ( -n / -F / -r ) not acceptable! \n");
			System.err.println(help);
			System.exit(1);
		}
		
		// Get the Directories to be processed
		File[] jobsDirectories;
		if(choosefrom == true){
			BufferedReader choosefromFileRead = new BufferedReader(new FileReader(choosefromFile));
			String line;
			List<File> chosenFilesList = new ArrayList<File>();
			while ((line=choosefromFileRead.readLine())!=null) {
				File chosenFile = new File(jobDirectoriesRoot, line);
				chosenFilesList.add(chosenFile);
			}
			choosefromFileRead.close();
			
			jobsDirectories = chosenFilesList.toArray(new File[chosenFilesList.size()]);
		}
		else {
			jobsDirectories = jobDirectoriesRoot.listFiles();
		}
		
		//Print the MODE of usage
		if(modeNew) System.out.println("\n\nMODE SELECTED: Insert New Entries\n");
		if(modeEverything) System.out.println("\n\nMODE SELECTED: Force Insert, which will insert everything in DB\n");
		if(modeRemove) System.out.println("\n\nMODE SELECTED: Remove entried from DB\n");
		
		// starting the db handler
		if (!useOnlineJpa) {
			dbh = new DBHandler(DBHandler.DEFAULT_OFFLINE_JPA);
		} else {
			dbh = new DBHandler();
		}
		
		// Start the Process
		for (File jobDirectory : jobsDirectories)
		{	
			//Check if it really is a directory
			if (!jobDirectory.isDirectory()){
				if(choosefrom == true)	System.err.println("Warning: "+jobDirectory.getName()+" specified in JobsFile, " +
															"but directory is not present, Skipping");
				continue;
			}
			
			//Check for 4 letter code directories starting with a number
			if (!jobDirectory.getName().matches("^\\d\\w\\w\\w$")){
				System.out.println("Skipping Directory: " + jobDirectory.getName());
				continue; 
			}
			
			try 
			{
				System.out.print(jobDirectory.getName()+" ");
				
				long start = System.currentTimeMillis();
				
				String currentPDB = jobDirectory.getName();
				
								
				// Get the PDB-Score Item to be read
				File webuiFile = new File(jobDirectory, currentPDB + ".webui.dat");
				boolean isWuiPresent = webuiFile.isFile();
				
				Object toAdd = null;
				
				if(isWuiPresent) {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(webuiFile));
					PDBScoreItemDB pdbScoreItem = (PDBScoreItemDB)in.readObject();
					in.close();
					
					toAdd = pdbScoreItem;
				}
				else {
					toAdd = currentPDB;
				}
					
				//MODE FORCE
				if (modeEverything) {
					boolean ifRemoved = dbh.removefromDB(currentPDB);
					if (ifRemoved) System.out.print(" Found.. Removing and Updating.. ");
					else System.out.print(" Not Found.. Adding.. ");
					if(isWuiPresent) dbh.addToDB((PDBScoreItemDB) toAdd);
					else dbh.addToDB((String) toAdd);
					//continue;
				}
				
				//MODE NEW INSERT
				if (modeNew){
					boolean isPresent = dbh.checkfromDB(currentPDB);
					if(!isPresent){
						System.out.print(" Not Present.. Adding.. ");
						if(isWuiPresent) dbh.addToDB((PDBScoreItemDB) toAdd);
						else dbh.addToDB((String) toAdd);
					}
					else System.out.print(" Already Present.. Skipping.. ");
					//continue;
				}
				
				//MODE REMOVE
				if (modeRemove) {
					boolean ifRemoved = dbh.removefromDB(currentPDB);
					if (ifRemoved) System.out.print(" Removed.. ");
					else System.out.print(" Not Found.. ");
					//continue;
				}
				
				long end = System.currentTimeMillis();
				System.out.print(((end-start)/1000)+"s\n");
				
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}



		


	}


}
