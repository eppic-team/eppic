package ch.systemsx.sybit.crkwebui.server.jobs.managers.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import ch.systemsx.sybit.crkwebui.server.jobs.managers.NativeJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirectoryContentReader;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.FileContentReader;
import ch.systemsx.sybit.crkwebui.server.commons.util.log.LogHandler;
import ch.systemsx.sybit.crkwebui.server.email.data.EmailMessageData;
import ch.systemsx.sybit.crkwebui.server.email.managers.EmailSender;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DeserializationException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import eppic.model.dto.JobStatusDetails;
import eppic.model.shared.StatusOfJob;
import eppic.db.dao.DaoException;
import eppic.db.dao.JobDAO;
import eppic.model.db.PdbInfoDB;

/**
 * Daemon used to update status of submitted jobs.
 * @author AS
 *
 */
public class JobStatusUpdater implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(JobStatusUpdater.class);
	
	/**
	 * The polling interval in milliseconds
	 */
	public static final int POLLING_INTERVAL = 2000;

	/**
	 * The default interval to log the queue state, in milliseconds
	 */
	public static final int LOG_QUEUE_INTERVAL = 60 * 60 * 1000; // every hour
	
	private volatile boolean running;
	private volatile boolean isUpdating;
	private JobManager jobManager;
	private JobDAO jobDAO;
	private String resultsPathUrl;
	private EmailSender emailSender;
	private EmailMessageData emailMessageData;
	private String generalDestinationDirectoryName;
	private int logQueueInterval;

	public JobStatusUpdater(JobManager jobManager,
							JobDAO jobDAO,
							String resultsPathUrl,
							EmailSender emailSender,
							EmailMessageData emailMessageData,
							String generalDestinationDirectoryName)
	{
		this.running = true;
		this.jobManager = jobManager;
		this.jobDAO = jobDAO;
		this.resultsPathUrl = resultsPathUrl;
		this.emailSender = emailSender;
		this.emailMessageData = emailMessageData;
		this.generalDestinationDirectoryName = generalDestinationDirectoryName;
		this.logQueueInterval = LOG_QUEUE_INTERVAL;
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
							File logFileDirectory = DirLocatorUtil.getJobDir(new File(generalDestinationDirectoryName), unfinishedJob.getJobId());
							File logFile = new File(logFileDirectory, CrkWebServiceImpl.PROGRESS_LOG_FILE_NAME);

							if(currentStatus == StatusOfJob.FINISHED)
							{
								handleJobFinishedSuccessfully(unfinishedJob, logFile);
							}
							else if(currentStatus == StatusOfJob.ERROR)
							{
								handleJobFinishedWithError(unfinishedJob);
							}
							else if(currentStatus == StatusOfJob.RUNNING)
							{
								handleRunningJob(unfinishedJob.getJobId());
							}
							else if(currentStatus == StatusOfJob.WAITING)
							{
								handleWaitingJob(unfinishedJob.getJobId());
							}
							else if(currentStatus == StatusOfJob.STOPPED)
							{
								handleStoppedJob(unfinishedJob.getJobId());
							}
						}
					}
					catch(DeserializationException e)
					{
						handleJobFinishedWithError(unfinishedJob);
					}
					catch (JobHandlerException|DaoException e)
					{
						logger.error(e.getMessage(), e);
					}

				}
				
				Thread.sleep(POLLING_INTERVAL);
			}
			catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}

			// log queue state every LOG_QUEUE_INTERVAL
			if (System.currentTimeMillis()%logQueueInterval < POLLING_INTERVAL) {
				if (jobManager instanceof NativeJobManager) {
					((NativeJobManager) jobManager).logJobHistory();
				}
			}
			
			isUpdating = false;
		}
	}

	/**
	 * Handles successfully finished job.
	 * @param jobStatusDetails details of the job
	 * @param logFile file where job execution log is stored
	 * @throws DaoException when can not appropriately handle results of the job
	 * @throws DeserializationException when can not deserialize results file
	 */
	private void handleJobFinishedSuccessfully(JobStatusDetails jobStatusDetails,
											   File logFile) throws DaoException, DeserializationException
	{
		String webuiFileName = jobStatusDetails.getInput();

	    if(webuiFileName.contains("."))
	    {
	    	webuiFileName = webuiFileName.substring(0, webuiFileName.lastIndexOf("."));
	    }

	    webuiFileName += ".webui.dat";

	    File resultsDirectory =  DirLocatorUtil.getJobDir(new File(generalDestinationDirectoryName), jobStatusDetails.getJobId());
	    File resultsFile = new File(resultsDirectory, webuiFileName);
		PdbInfoDB pdbScoreItem = retrieveResult(resultsFile);
		jobDAO.setPdbScoreItemForJob(jobStatusDetails.getJobId(), pdbScoreItem);

		LogHandler.writeToLogFile(logFile, "Processing finished\n");

		String emailTitle = emailMessageData.getEmailJobFinishedTitle().replaceFirst("%s", jobStatusDetails.getInput());
		String emailMessage = emailMessageData.getEmailJobFinishedMessage().replaceFirst("%s", jobStatusDetails.getInput());
		emailMessage = emailMessage.replaceFirst("%s", resultsPathUrl);
		emailMessage = emailMessage.replaceFirst("%s", jobStatusDetails.getJobId());

		emailSender.sendInSeparateThread(jobStatusDetails.getEmailAddress(),
						 emailTitle,
						 emailMessage);
	}

	/**
	 * Handles job which finished with error status.
	 * @param jobStatusDetails details of the job
	 * @throws DaoException when can not appropriately handle results of the job
	 */
	private void handleJobFinishedWithError(JobStatusDetails jobStatusDetails) throws DaoException
	{
		File jobDirectory = DirLocatorUtil.getJobDir(new File(generalDestinationDirectoryName), jobStatusDetails.getJobId());
		
		File[] directoryContent = DirectoryContentReader.getFilesNamesWithPrefix(jobDirectory,
																					jobStatusDetails.getJobId() + ".e");
		File errorLogFile = null;
		if((directoryContent != null) && (directoryContent.length > 0))
		{
			errorLogFile = directoryContent[0];
		}
		
		
		String errorMsg = null;
		
		if(errorLogFile != null)
		{
			try
			{
				errorMsg = FileContentReader.readContentOfFile(errorLogFile, true);
			}
			catch(Throwable t)
	        {
				errorMsg = "Unknown error";
	        	logger.error("Exception caught. Error: {}", t.getMessage());
	        }
		}

		jobDAO.updateStatusOfJob(jobStatusDetails.getJobId(), StatusOfJob.ERROR);
		
		String emailTitle = emailMessageData.getEmailJobErrorTitle().replaceFirst("%s", jobStatusDetails.getInput());
		String emailMessage = emailMessageData.getEmailJobErrorMessage().replaceFirst("%s", errorMsg);
		emailMessage = emailMessage.replaceFirst("%s", resultsPathUrl);
		emailMessage = emailMessage.replaceFirst("%s", jobStatusDetails.getJobId());
		
		emailSender.sendInSeparateThread(jobStatusDetails.getEmailAddress(),
						 emailTitle,
						 emailMessage);
	}

	/**
	 * Handles job with running status.
	 * @param jobId identifier of the job
	 * @throws DaoException when can not appropriately handle running job
	 */
	private void handleRunningJob(String jobId) throws DaoException
	{
		jobDAO.updateStatusOfJob(jobId, StatusOfJob.RUNNING);
	}
	
	/**
	 * Handles job with waiting status.
	 * @param jobId identifier of the job
	 * @throws DaoException when can not appropriately handle waiting job
	 */
	private void handleWaitingJob(String jobId) throws DaoException
	{
		jobDAO.updateStatusOfJob(jobId, StatusOfJob.WAITING);
	}
	
	/**
	 * Handles job with stopped status.
	 * @param jobId identifier of the job
	 * @throws DaoException when can not appropriately handle stopped job
	 */
	private void handleStoppedJob(String jobId) throws DaoException
	{
		jobDAO.updateStatusOfJob(jobId, StatusOfJob.STOPPED);
	}


	/**
	 * Retrieves result of processing from specified file.
	 * @param resultFile file containing result of processing
	 * @return pdb score item
	 * @throws DeserializationException when can not retrieve result from specified file
	 */
	private PdbInfoDB retrieveResult(File resultFile) throws DeserializationException
	{
		PdbInfoDB pdbScoreItem = null;

		if (resultFile.exists())
		{
			FileInputStream fileInputStream = null;
			ObjectInputStream inputStream = null;

			try
			{
				fileInputStream = new FileInputStream(resultFile);
				inputStream = new ObjectInputStream(fileInputStream);
				pdbScoreItem = (PdbInfoDB)inputStream.readObject();
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				throw new DeserializationException(e);
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
			throw new DeserializationException("WebUI dat file can not be found");
		}

		return pdbScoreItem;
	}
	
	public void setRunning(boolean running)
	{
		this.running = running;
	}

	/**
	 * Retrieves information whether daemon is currently updating statuses of the jobs.
	 * @return information whether daemon is currently updating statuses of the jobs
	 */
	public boolean isUpdating() {
		return isUpdating;
	}

	public void setLogQueueInterval(int logQueueInterval) {
		logger.info("Will report queue contents every {} ms", logQueueInterval);
		this.logQueueInterval = logQueueInterval;
	}
}
