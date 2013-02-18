package ch.systemsx.sybit.crkwebui.server.db.util;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import model.JobDB;
import model.PDBScoreItemDB;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

public class UploadToDb {

	private static void addToDB(PDBScoreItemDB pdbScoreItem){
		Date submissionDate = new Date();
		EntityManager entityManager = EntityManagerHandler.getEntityManager();
		try
		{
			entityManager.getTransaction().begin();
			entityManager.persist(pdbScoreItem);
	
			String pdbCode = pdbScoreItem.getPdbName();

			JobDB job = new JobDB();
			job.setJobId(pdbCode);
			job.setEmail(null);
			job.setInput(pdbCode);
			job.setIp("localhost");
			job.setStatus(StatusOfJob.FINISHED.getName());
			job.setSubmissionDate(submissionDate);
			job.setInputType(InputType.PDBCODE.getIndex());
			job.setSubmissionId("-1");

			pdbScoreItem.setJobItem(job);
			job.setPdbScoreItem(pdbScoreItem);
			entityManager.persist(job);
			System.out.print(" Updating.. ");
			entityManager.getTransaction().commit();			
		}
		catch(Throwable e)
		{
			e.printStackTrace();

			try
			{
				entityManager.getTransaction().rollback();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}

		}
		finally
		{
			try
			{
				entityManager.close();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			System.out.println();
		}
	}
	
	private static void removefromDB(String pdbID){
		EntityManager entityManager = EntityManagerHandler.getEntityManager();
		entityManager.getTransaction().begin();
		
		String queryJobstr = "FROM " + JobDB.class.getName() + " WHERE jobId='" + pdbID +"'";
		TypedQuery<JobDB> queryJob = entityManager.createQuery(queryJobstr, JobDB.class);
						
		List<JobDB> queryJobList = queryJob.getResultList();
		int querySize = queryJobList.size();
		
		if(querySize>0){
			System.out.print(" Already present (" + querySize + " times) Removing ");
			for(JobDB itemJobDB:queryJobList){
				Long itemUid = itemJobDB.getUid();
				String queryPDBstr = "FROM " + PDBScoreItemDB.class.getName() + " WHERE jobItem_uid='" + itemUid +"'";
				TypedQuery<PDBScoreItemDB> queryPDB = entityManager.createQuery(queryPDBstr, PDBScoreItemDB.class);
				List<PDBScoreItemDB> queryPDBList = queryPDB.getResultList();
				if( queryPDBList != null)
				{
					for(PDBScoreItemDB itemPDB : queryPDBList)
					{
						entityManager.remove(itemPDB);
					}
				}
				entityManager.remove(itemJobDB);
			}
		}
		else System.out.print(" Not present in Data Base ");
		entityManager.getTransaction().commit();
	}
	
	private static boolean checkfromDB(String pdbID){
		EntityManager entityManager = EntityManagerHandler.getEntityManager();
		
		String queryJobstr = "FROM " + JobDB.class.getName() + " WHERE jobId='" + pdbID +"'";
		TypedQuery<JobDB> queryJob = entityManager.createQuery(queryJobstr, JobDB.class);
						
		List<JobDB> queryJobList = queryJob.getResultList();
		int querySize = queryJobList.size();
		
		if(querySize>0) {
			System.out.print(" Already present (" + querySize + " times) ");
			return true;
		}
		else {
			System.out.print(" Not present in DB ");
			return false;
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */

	public static void main(String[] args) throws IOException {
		
		String help = 
				"Usage: UploadToDB\n" +
				"Uploads a set of id's to Data Base \n" +
				" -d <dir>  	: Directory of eppic output files \n" +
				" [-f <file>] 	: File specifying the directories to be used from output directory \n" +
				"                  Default: Uses all files in the directory as \n" +
				" [-F]          : Force everything to be continued without any breaks\n" +
				" TYPE of ID's to be considered\n" +
				" 	[-p]        : (Default option) Uses PDB IDs only for operations \n" +
				" 	[-j]        : Uses User-Job-ID's only for operations \n" +
				" OPERATION MODE\n" +
				" 	[-n]        : (Default mode) Inserts the entries which are not in the database\n" +
				" 	[-e]        : Everything chosen is inserted, deletes previous entries if present\n " +
				"	[-r]        : Removes the specified files\n";

		File jobDirectoriesRoot = null;
		File choosefromFile = null;
		boolean force = false;
		boolean choosefrom = false;
		boolean typePDB = true;
		boolean typeJob = false;
		boolean modeNew = true;
		boolean modeEverything = false;
		boolean modeRemove = false;

		Getopt g = new Getopt("UploadToDB", args, "d:f:Fpjnerh?");
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
			case 'F':
				force = true;
				break;
			case 'p':
				typePDB = true;
				typeJob = false;
				break;
			case 'j':
				typeJob = true;
				typePDB = false;
				break;
			case 'n':
				modeNew = true;
				modeEverything = false;
				modeRemove = false;
				break;
			case 'e':
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
			System.err.println("Hey Jim, Output Directory not specified correctly! ");
			System.err.println(help);
			System.exit(1);
		}
		
		if ( choosefrom && choosefromFile == null ){
			System.err.println("Hey Jim, Option -f not specified correctly! ");
			System.err.println(help);
			System.exit(1);
		}
		
		if ( typePDB && typeJob ){
			System.err.println("Hey Jim, Combinations of TYPE ( -p/-j/-a ) not acceptable! ");
			System.err.println(help);
			System.exit(1);
		}
		
		//only one of the three should be true
		if(!( ( (modeNew) && !(modeEverything) && !(modeRemove) ) || 
			  (!(modeNew) &&   modeEverything &&  !(modeRemove) ) ||
			  (!(modeNew) && !(modeEverything) &&   modeRemove  ) ) ){
			System.err.println("Hey Jim, Combinations of MODE ( -n/-e/-r ) not acceptable! ");
			System.err.println(help);
			System.exit(1);
		}
		
		// -j (User-Job Mode) works with remove only!!
		if( (typeJob && modeNew) || (typeJob && modeEverything) ){
			System.err.println("Hey Jim, -j mode works with -r mode only");
			System.err.println(help);
			System.exit(1);
		}
		
		// Check if user really want to remove user-jobs
		if( typeJob && modeRemove && !(force)){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("You are trying to remove user-jobs from database. They cannot be added back." +
					" Are you sure to continue (type 'yes' to continue)? ");
			try {
				String answer = br.readLine();
				if (!answer.equals("yes")) {
					System.err.println("Exiting, Please carefully check the options you are trying to use!");
					System.err.println(help);
					System.exit(1);
				}
			} catch (IOException e) {
				System.err.println("Error while trying to read standard input");
				System.exit(1);
			}
		}
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
		
		for (File jobDirectory : jobsDirectories)
		{			
			if (!jobDirectory.isDirectory()){
				if(choosefrom == true){
					System.err.println("Warning: "+jobDirectory.getName()+" specified in JobsFile, but directory is not present, Skipping");
					continue;
				}
				else continue;
			}
			
			if (typeJob == true && !jobDirectory.getName().matches("^[a-zA-Z]\\w{29}$")){ 
				System.out.println("Skipping " + jobDirectory.getName()); 
				continue; }
			if (typePDB == true && !jobDirectory.getName().matches("^\\d\\w\\w\\w$")){
				System.out.println("Skipping " + jobDirectory.getName());
				continue; 
			}
			
			try 
			{
				System.out.print(jobDirectory.getName()+" ");
				
				long start = System.currentTimeMillis();
				
				String currentPDB = null;
				
				
				if (typePDB){
					currentPDB = jobDirectory.getName();
				}
				
				// Get pdbName in -j type
				if (typeJob){
					String getPDBQueryStr = "SELECT input FROM " + JobDB.class.getName() + " where jobId='"+jobDirectory.getName() +"'";
					TypedQuery<String> getPDBQuery = EntityManagerHandler.getEntityManager().createQuery(getPDBQueryStr, String.class);
					try{
						currentPDB = getPDBQuery.getSingleResult();
					}
					catch(NoResultException ex){
						System.out.print(": Not present in Database.. Skipping..\n");
						continue;
					}
				}
				
				
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(
						new File(jobDirectory, currentPDB + ".webui.dat")));
				PDBScoreItemDB pdbScoreItem = (PDBScoreItemDB)in.readObject();
				in.close();
				
				//MODE REMOVE
				if (modeRemove) {
					if (typeJob) removefromDB(jobDirectory.getName());
					else removefromDB(pdbScoreItem.getPdbName());
				}
				
				//MODE FORCE
				if (modeEverything) {
					removefromDB(pdbScoreItem.getPdbName());
					addToDB(pdbScoreItem);
				}
				
				//MODE NEW INSERT
				if (modeNew){
					boolean isPresent = checkfromDB(pdbScoreItem.getPdbName());
					if(!isPresent) addToDB(pdbScoreItem);
					else System.out.print(" Skipping ");
				}
				
				long end = System.currentTimeMillis();
				System.out.print(((end-start)/1000)+"s\n");
				
			}
			catch(IOException t)
			{
				System.err.println("Failed to read file "+t.getMessage());
			}

			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}



		


	}


}
