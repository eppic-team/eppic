package ch.systemsx.sybit.crkwebui.server.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.List;

import model.PDBScoreItemDB;
import ch.systemsx.sybit.crkwebui.server.EmailSender;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.JobStatusDetails;
import ch.systemsx.sybit.crkwebui.server.generators.DirectoryContentGenerator;
import ch.systemsx.sybit.crkwebui.server.util.LogHandler;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

/**
 * Daemon used to update status of submitted jobs.
 * @author AS
 *
 */
public class JobStatusUpdater implements Runnable
{
	private volatile boolean running;
	private volatile boolean isUpdating;
	private JobManager jobManager;
	private JobDAO jobDAO;
	private String resultsPathUrl;
	private EmailSender emailSender;
	private String generalDestinationDirectoryName;

	public JobStatusUpdater(JobManager jobManager,
							JobDAO jobDAO,
							String resultsPathUrl,
							EmailSender emailSender,
							String generalDestinationDirectoryName)
	{
		this.running = true;
		this.jobManager = jobManager;
		this.jobDAO = jobDAO;
		this.resultsPathUrl = resultsPathUrl;
		this.emailSender = emailSender;
		this.generalDestinationDirectoryName = generalDestinationDirectoryName;
	}

	@Override
	public void run()
	{
		while(running)
		{
			isUpdating = true;
			
			try
			{
				List<JobStatusDetails> unfinishedJobs = jobDAO.getListOfUnfinishedJobs();

				for(JobStatusDetails unfinishedJob : unfinishedJobs)
				{
					try
					{
						StatusOfJob savedStatus = StatusOfJob.getByName(unfinishedJob.getStatus());

						StatusOfJob currentStatus = jobManager.getStatusOfJob(unfinishedJob.getJobId(), unfinishedJob.getSubmissionId());

						if(savedStatus != currentStatus)
						{
							File logFileDirectory = new File(generalDestinationDirectoryName, unfinishedJob.getJobId());
							File logFile = new File(logFileDirectory, "crklog");

							if(currentStatus == StatusOfJob.FINISHED)
							{
								handleJobFinishedSuccessfully(unfinishedJob, logFile);
							}
							else if(currentStatus == StatusOfJob.ERROR)
							{
								File jobDirectory = new File(generalDestinationDirectoryName, unfinishedJob.getJobId());
								
								File directoryContent[] = DirectoryContentGenerator.getFilesNamesWithPrefix(jobDirectory,
																									  		unfinishedJob.getJobId() + ".e");
								File errorLogFile = null;
								if((directoryContent != null) && (directoryContent.length > 0))
								{
									errorLogFile = directoryContent[0];
								}

								handleJobFinishedWithError(unfinishedJob, logFile, errorLogFile);
							}
							else if(currentStatus == StatusOfJob.RUNNING)
							{
								handleRunningJob(unfinishedJob.getJobId());
							}
							else if(currentStatus == StatusOfJob.WAITING)
							{
								handleWaitingJob(unfinishedJob.getJobId());
							}
						}
					}
					catch (JobHandlerException e)
					{
						e.printStackTrace();
					}
					catch (DaoException e)
					{
						e.printStackTrace();
					}
				}
				
				Thread.sleep(2000);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			
			isUpdating = false;
		}
	}

	/**
	 * Handles successfully finished job.
	 * @param jobStatusDetails details of the job
	 * @param logFile file where job execution log is stored
	 * @throws DaoException when can not appropriately handle results of the job
	 */
	private void handleJobFinishedSuccessfully(JobStatusDetails jobStatusDetails,
											   File logFile) throws DaoException
	{
		String webuiFileName = jobStatusDetails.getInput();

	    if(webuiFileName.contains("."))
	    {
	    	webuiFileName = webuiFileName.substring(0, webuiFileName.lastIndexOf("."));
	    }

	    webuiFileName += ".webui.dat";

	    File resultsDirectory = new File(generalDestinationDirectoryName, jobStatusDetails.getJobId());
	    File resultsFile = new File(resultsDirectory, webuiFileName);
		PDBScoreItemDB pdbScoreItem = retrieveResult(resultsFile);
		jobDAO.setPdbScoreItemForJob(jobStatusDetails.getJobId(), pdbScoreItem);

		LogHandler.writeToLogFile(logFile, "Processing finished\n");

		String message = jobStatusDetails.getInput() +
				  		 " processing finished. To see the status of the processing please go to: " +
				  		 resultsPathUrl + "#id=" + jobStatusDetails.getJobId();

		emailSender.send(jobStatusDetails.getEmailAddress(),
						 "EPPIC: " + jobStatusDetails.getInput() + " processing finished",
						 message);
	}

	/**
	 * Handles job which finished with error status.
	 * @param jobStatusDetails details of the job
	 * @param logFile file where job execution log is stored
	 * @param errorLogFile file where job execution error log is stored
	 * @throws DaoException when can not appropriately handle results of the job
	 */
	private void handleJobFinishedWithError(JobStatusDetails jobStatusDetails,
											File logFile,
											File errorLogFile) throws DaoException
	{
		StringBuffer message = new StringBuffer(jobStatusDetails.getInput() + " - error while processing the data.\n\n");

		if(errorLogFile != null)
		{
			FileReader inputStream = null;
	        BufferedReader bufferedInputStream = null;

	        try
	        {
	        	inputStream = new FileReader(errorLogFile);
		        bufferedInputStream = new BufferedReader(inputStream);

		        String line = "";

		        while ((line = bufferedInputStream.readLine()) != null)
		        {
		        	message.append(line + "\n");
		        }
	        }
	        catch(Throwable t)
	        {
	        	t.printStackTrace();
	        }
	        finally
	        {
	        	if(bufferedInputStream != null)
				{
					try
					{
						bufferedInputStream.close();
					}
					catch(Throwable t)
					{
						t.printStackTrace();
					}
				}
	        }
		}

		jobDAO.updateStatusOfJob(jobStatusDetails.getJobId(), StatusOfJob.ERROR.getName());
		
		emailSender.send(jobStatusDetails.getEmailAddress(),
						 "EPPIC: " + jobStatusDetails.getInput() + ", error while processing",
						 message + "\n\n" + resultsPathUrl + "#id=" + jobStatusDetails.getJobId());
	}

	/**
	 * Handles job with running status.
	 * @param jobId identifier of the job
	 * @throws DaoException when can not appropriately handle running job
	 */
	private void handleRunningJob(String jobId) throws DaoException
	{
		jobDAO.updateStatusOfJob(jobId, StatusOfJob.RUNNING.getName());
	}
	
	/**
	 * Handles job with waiting status.
	 * @param jobId identifier of the job
	 * @throws DaoException when can not appropriately handle waiting job
	 */
	private void handleWaitingJob(String jobId) throws DaoException
	{
		jobDAO.updateStatusOfJob(jobId, StatusOfJob.WAITING.getName());
	}


	/**
	 * Retrieves result of processing from specified file.
	 * @param resultFileName file containing result of processing
	 * @return pdb score item
	 * @throws DaoException when can not retrieve result from specified file
	 */
	private PDBScoreItemDB retrieveResult(File resultFile) throws DaoException
	{
		PDBScoreItemDB pdbScoreItem = null;

		if (resultFile.exists())
		{
			FileInputStream fileInputStream = null;
			ObjectInputStream inputStream = null;

			try
			{
				fileInputStream = new FileInputStream(resultFile);
				inputStream = new ObjectInputStream(fileInputStream);
				pdbScoreItem = (PDBScoreItemDB)inputStream.readObject();
			}
			catch (Throwable e)
			{
				throw new DaoException(e);
			}
			finally
			{
				if(inputStream != null)
				{
					try
					{
						inputStream.close();
					}
					catch(Throwable t)
					{
						t.printStackTrace();
					}
				}
			}
		}
		else
		{
			throw new DaoException("WebUI dat file can not be found");
		}

		return pdbScoreItem;
	}
	
	public void setRunning(boolean running)
	{
		this.running = running;
	}

	public void setUpdating(boolean isUpdating) {
		this.isUpdating = isUpdating;
	}

	public boolean isUpdating() {
		return isUpdating;
	}
}
