package ch.systemsx.sybit.crkwebui.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

import javax.persistence.EntityManager;
import model.JobDB;
import model.PDBScoreItemDB;

import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

public class UploadToDb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		if (args.length<1) {
			System.err.println("Usage: UploadToDb <dir name>");
			System.exit(1);
		}
		
		String jobDirectoriesRootName = args[0];
		
		Date submissionDate = new Date();
		


		File jobDirectoriesRoot = new File(jobDirectoriesRootName);
		
		File[] jobsDirectories = jobDirectoriesRoot.listFiles();
		
		for (File jobDirectory : jobsDirectories)
		{
			
			if (!jobDirectory.isDirectory()) continue;
			
			if (!jobDirectory.getName().matches("^\\d\\w\\w\\w$")) continue; 
			
			try 
			{
				System.out.print(jobDirectory.getName()+" ");
				
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(
						new File(jobDirectory, jobDirectory.getName() + ".webui.dat")));
				
				PDBScoreItemDB pdbScoreItem = (PDBScoreItemDB)in.readObject();
		
				EntityManager entityManager = EntityManagerHandler.getEntityManager();
		
				long start = System.currentTimeMillis();
				
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
		
					entityManager.getTransaction().commit();
					
					long end = System.currentTimeMillis();
					
					System.out.print(((end-start)/1000)+"s");
					
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
