package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.persistence.PersistenceContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebService;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirectoryContentReader;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.FileContentReader;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.IOUtil;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.RandomDirectoryGenerator;
import ch.systemsx.sybit.crkwebui.server.commons.util.log.LogHandler;
import ch.systemsx.sybit.crkwebui.server.commons.validators.PreSubmitValidator;
import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.commons.validators.SessionValidator;
import ch.systemsx.sybit.crkwebui.server.db.dao.ChainClusterDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceClusterDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.ResidueDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.UserSessionDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.ChainClusterDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceClusterDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.ResidueDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBInfoDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.UserSessionDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.data.InputWithType;
import ch.systemsx.sybit.crkwebui.server.email.data.EmailData;
import ch.systemsx.sybit.crkwebui.server.email.data.EmailMessageData;
import ch.systemsx.sybit.crkwebui.server.email.managers.EmailSender;
import ch.systemsx.sybit.crkwebui.server.ip.validators.IPVerifier;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.JobManagerFactory;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobStatusUpdater;
import ch.systemsx.sybit.crkwebui.server.runners.CrkRunner;
import ch.systemsx.sybit.crkwebui.server.settings.generators.ApplicationSettingsGenerator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import ch.systemsx.sybit.crkwebui.shared.model.Residue;
import ch.systemsx.sybit.crkwebui.shared.model.ResiduesList;
import ch.systemsx.sybit.crkwebui.shared.model.JobsForSession;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;
import ch.systemsx.sybit.crkwebui.shared.model.StepStatus;

import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

import eppic.EppicParams;

/**
 * The server side implementation of the RPC service.
 *
 * @author srebniak_a
 */
@PersistenceContext(name="eppicjpa", unitName="eppicjpa")
@SuppressWarnings("serial")
public class CrkWebServiceImpl extends XsrfProtectedServiceServlet implements CrkWebService
{
	// general server settings
	private Properties properties;

	private String generalTmpDirectoryName;
	private String generalDestinationDirectoryName;

	private String crkApplicationLocation;

	private String protocol = "http";

	private boolean doIPBasedVerification;
	private int defaultNrOfAllowedSubmissionsForIP;

	private String localCifDir;

	private JobManager jobManager;
	private CrkRunner crkRunner;
	
	private JobStatusUpdater jobStatusUpdater;
	private Thread jobDaemon;
	private EmailSender emailSender;

	private String resultsPathUrl;
	
	private EmailMessageData emailMessageData;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		InputStream propertiesStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/META-INF/server.properties");

		properties = new Properties();

		try
		{
			properties.load(propertiesStream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ServletException("Properties file can not be read");
		}

		generalTmpDirectoryName = properties.getProperty("tmp_path");
		File tmpDir = new File(generalTmpDirectoryName);

		if (!tmpDir.isDirectory())
		{
			throw new ServletException(generalTmpDirectoryName + " is not a directory");
		}

		generalDestinationDirectoryName = properties.getProperty("destination_path");
		File destinationDir = new File(generalDestinationDirectoryName);
		if (!destinationDir.isDirectory())
		{
			throw new ServletException(generalDestinationDirectoryName + " is not a directory");
		}

		crkApplicationLocation = properties.getProperty("crk_jar");

		if (crkApplicationLocation == null)
		{
			throw new ServletException("Location of crk application not specified");
		}

		if(properties.getProperty("protocol") != null)
		{
			protocol = properties.getProperty("protocol");
		}

		doIPBasedVerification = Boolean.parseBoolean(properties.getProperty("limit_access_by_ip","false"));
		defaultNrOfAllowedSubmissionsForIP = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_for_ip","100"));

		String queuingSystem = properties.getProperty("queuing_system");
		if(queuingSystem == null)
		{
			throw new ServletException("Queuing system not specified");
		}
		
		int nrOfThreadForSubmission = Integer.parseInt(properties.getProperty("nr_of_threads_for_submission","1"));
		int assignedMemory = Integer.parseInt(properties.getProperty("assigned_memory","512"));
		
		Properties crkProperties = new Properties();

		try
		{
			File crkPropertiesFile = new File(System.getProperty("user.home"), EppicParams.CONFIG_FILE_NAME);
			crkProperties.load(new FileInputStream(crkPropertiesFile));
		}
		catch (IOException e)
		{
			throw new ServletException("Crk properties file can not be read");
		}

		localCifDir = crkProperties.getProperty("LOCAL_CIF_DIR");

		if(!properties.containsKey(ApplicationSettingsGenerator.DEVELOPMENT_MODE))
		    initializeJobManager(queuingSystem);
		
		String serverName = getServletContext().getInitParameter("serverName");
		
		resultsPathUrl = protocol + "://" + serverName;
		
		String serverPortValue = getServletContext().getInitParameter("serverPort");
		if((serverPortValue != null) &&
		   (!serverPortValue.equals("")))
		{
			int serverPort = Integer.parseInt(serverPortValue);
			resultsPathUrl += ":" + serverPort;
		}

		String servletContPath = getServletContext().getInitParameter("servletContPath");
		resultsPathUrl += "/" + servletContPath + "/";

		EmailData emailData = new EmailData();
		emailData.setEmailSender(properties.getProperty("email_username", ""));
		emailData.setEmailSenderPassword(properties.getProperty("email_password", ""));
		emailData.setHost(properties.getProperty("email_host"));
		emailData.setPort(properties.getProperty("email_port"));
		emailSender = new EmailSender(emailData);

		
		InputStream emailPropertiesStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/META-INF/email.properties");

		Properties emailProperties = new Properties();

		emailMessageData = new EmailMessageData();
		
		try
		{
			emailProperties.load(emailPropertiesStream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ServletException("Email properties file can not be read. "+e.getMessage());
		}
		
		emailMessageData.setEmailJobSubmittedTitle(emailProperties.getProperty("email_job_submitted_title"));
		emailMessageData.setEmailJobSubmittedMessage(emailProperties.getProperty("email_job_submitted_message"));
		
		emailMessageData.setEmailJobSubmitErrorTitle(emailProperties.getProperty("email_job_submit_error_title"));
		emailMessageData.setEmailJobSubmitErrorMessage(emailProperties.getProperty("email_job_submit_error_message"));

		emailMessageData.setEmailJobErrorTitle(emailProperties.getProperty("email_job_error_title"));
		emailMessageData.setEmailJobErrorMessage(emailProperties.getProperty("email_job_error_message"));

		emailMessageData.setEmailJobFinishedTitle(emailProperties.getProperty("email_job_finished_title"));
		emailMessageData.setEmailJobFinishedMessage(emailProperties.getProperty("email_job_finished_message")); 


		if (	emailMessageData.getEmailJobSubmittedTitle() == null || emailMessageData.getEmailJobSubmittedMessage() == null ||
				emailMessageData.getEmailJobSubmitErrorTitle() == null || emailMessageData.getEmailJobSubmitErrorMessage() == null ||
				emailMessageData.getEmailJobErrorTitle() == null || emailMessageData.getEmailJobErrorMessage() == null ||
				emailMessageData.getEmailJobFinishedTitle() == null || emailMessageData.getEmailJobFinishedMessage() == null)
		{
			throw new ServletException("Email titles and messages have not been specified");
		}

		
		
		
		crkRunner = new CrkRunner(jobManager, 
								  crkApplicationLocation,
								  nrOfThreadForSubmission,
								  assignedMemory);
		if(!properties.containsKey(ApplicationSettingsGenerator.DEVELOPMENT_MODE)) {
		    jobStatusUpdater = new JobStatusUpdater(jobManager,
			    new JobDAOJpa(),
			    resultsPathUrl,
			    emailSender,
			    emailMessageData,
			    generalDestinationDirectoryName);
		    jobDaemon = new Thread(jobStatusUpdater);
		    jobDaemon.start();
		}

	}

	private void initializeJobManager(String queuingSystem) throws ServletException {
	    String queueingSystemConfigFile = "/WEB-INF/classes/META-INF/" + queuingSystem + "_queuing_system.properties";
	    InputStream queuingSystemPropertiesStream = getServletContext().getResourceAsStream(queueingSystemConfigFile );
	    Properties queuingSystemProperties = new Properties();
	    try {
	    	queuingSystemProperties.load(queuingSystemPropertiesStream);
	    }catch (IOException e) {
	    	e.printStackTrace();
	    	throw new ServletException("Properties file for " + queuingSystem + " can not be read");
	    }
	    try {
	    	jobManager = JobManagerFactory.getJobManager(queuingSystem, queuingSystemProperties, generalDestinationDirectoryName);
	    }catch(JobManagerException e) {
	    	throw new ServletException(e);
	    }
	}

	@Override
	public ApplicationSettings loadSettings() throws Exception
	{
		ApplicationSettingsGenerator applicationSettingsGenerator = new ApplicationSettingsGenerator(properties);
		
		InputStream inputParametersStream = getServletContext()
				.getResourceAsStream("/WEB-INF/classes/META-INF/input_parameters.xml");
		
		InputStream gridPropertiesInputStream = getServletContext()
				.getResourceAsStream("/WEB-INF/classes/META-INF/grid.properties");

		ApplicationSettings settings = applicationSettingsGenerator.generateApplicationSettings(inputParametersStream, 
																								gridPropertiesInputStream);


		JobDAO jobDAO = new JobDAOJpa();
		int nrOfJobsForSession = jobDAO.getNrOfJobsForSessionId(getThreadLocalRequest().getSession().getId()).intValue();
		settings.setNrOfJobsForSession(nrOfJobsForSession);
		
		SessionValidator.validateSession(getThreadLocalRequest().getSession());

		return settings;
	}

	@Override
	public String runJob(RunJobData runJobData) throws Exception
	{
		if (runJobData != null)
		{
			int inputType = InputType.FILE.getIndex();

			if(doIPBasedVerification)
			{
				IPVerifier.verifyIfCanBeSubmitted(getThreadLocalRequest().getRemoteAddr(),
												  defaultNrOfAllowedSubmissionsForIP);
			}

			if(runJobData.getJobId() == null)
			{
				String randomDirectoryName = RandomDirectoryGenerator.generateRandomDirectory(generalDestinationDirectoryName);
				runJobData.setJobId(randomDirectoryName);
				inputType = InputType.PDBCODE.getIndex();
			}

			RunJobDataValidator.validateJobId(runJobData.getJobId());
			
			String localDestinationDirName = generalDestinationDirectoryName + File.separator + runJobData.getJobId();

			Date currentDate = new Date();

			JobDAO jobDAO = new JobDAOJpa();

			File logFile = new File(localDestinationDirName, "crklog");

			try
			{
				logFile.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			String submissionId = null;
			StatusOfJob submissionStatus = StatusOfJob.QUEUING;
			

			String emailTitle = "";
			String emailMessage = "";
			
			try
			{				
				if(inputType == InputType.PDBCODE.getIndex())
				{
					PreSubmitValidator.checkIfSubmit(localCifDir, runJobData.getInput());
				}

				submissionId = crkRunner.run(runJobData,
				 							 localDestinationDirName,
											 inputType);
				
				emailTitle = emailMessageData.getEmailJobSubmittedTitle().replaceFirst("%s", runJobData.getInput());
				emailMessage = emailMessageData.getEmailJobSubmittedMessage().replaceFirst("%s", runJobData.getInput());
				emailMessage = emailMessage.replaceFirst("%s", resultsPathUrl);
				emailMessage = emailMessage.replaceFirst("%s", runJobData.getJobId());
				
			}
			catch(Throwable e)
			{
				LogHandler.writeToLogFile(logFile, e.getMessage());

				submissionStatus = StatusOfJob.ERROR;

				emailTitle = emailMessageData.getEmailJobSubmitErrorTitle().replaceFirst("%s", runJobData.getInput());
				emailMessage = emailMessageData.getEmailJobSubmitErrorMessage().replaceFirst("%s", e.getMessage());
				emailMessage = emailMessage.replaceFirst("%s", resultsPathUrl);
				emailMessage = emailMessage.replaceFirst("%s", runJobData.getJobId());
				
			}

			jobDAO.insertNewJob(runJobData.getJobId(),
					getThreadLocalRequest().getSession().getId(),
					runJobData.getEmailAddress(),
					runJobData.getInput(),
					getThreadLocalRequest().getRemoteAddr(),
					currentDate,
					inputType,
					submissionStatus,
					submissionId);
			
			emailSender.send(runJobData.getEmailAddress(),
							 emailTitle,
							 emailMessage);
			
			return runJobData.getJobId();
		}

		return null;
	}

	@Override
	public ProcessingData getResultsOfProcessing(String jobId) throws Exception
	{
		String status = null;

		JobDAO jobDAO = new JobDAOJpa();
		status = jobDAO.getStatusForJob(jobId);

		if(status != null)
		{
			UserSessionDAO sessionDAO = new UserSessionDAOJpa();
			sessionDAO.insertSessionForJob(getThreadLocalRequest().getSession().getId(), jobId);

			if(status.equals(StatusOfJob.FINISHED.getName()))
			{
				return getResultData(jobId);
			}
			else
			{
				return getStatusData(jobId, status);
			}
		}
		else
		{
			return null;
		}
	}

	private ProcessingInProgressData getStatusData(final String jobId, String status) throws Exception
	{
		JobDAO jobDAO = new JobDAOJpa();

		ProcessingInProgressData statusData = null;

		if((jobId != null) && (!jobId.equals("")))
		{
			File dataDirectory = new File(generalDestinationDirectoryName, jobId);

			if (IOUtil.checkIfDirectoryExist(dataDirectory))
			{
				statusData = new ProcessingInProgressData();

				statusData.setJobId(jobId);
				statusData.setStatus(status);
				
				InputWithType inputWithType = jobDAO.getInputWithTypeForJob(jobId);
				statusData.setInputType(inputWithType.getInputType());
				statusData.setInput(inputWithType.getInputName());
				
				statusData.setStep(new StepStatus());

				try
				{
					List<File> filesToRead = new ArrayList<File>();

					File logFile = new File(dataDirectory, "crklog");
					
					if (IOUtil.checkIfFileExist(logFile))
					{
						filesToRead.add(new File(dataDirectory, "crklog"));
					}

					File[] directoryContent = DirectoryContentReader.getFilesNamesWithPrefix(dataDirectory, jobId + ".e");
						
					if(directoryContent != null)
					{
						for(File fileToInclude : directoryContent)
						{
							filesToRead.add(fileToInclude);
						}
					}

						
					StringBuffer log = new StringBuffer();

					for(File fileToRead : filesToRead)
					{
						log.append(FileContentReader.readContentOfFile(fileToRead, true));
					}

					statusData.setLog(log.toString());

					if((status.equals(StatusOfJob.RUNNING.getName())) ||
					   (status.equals(StatusOfJob.WAITING.getName())) ||
					   (status.equals(StatusOfJob.QUEUING.getName())))
					{
						statusData.setStep(retrieveCurrentStep(jobId, statusData.getInput()));
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
					throw new CrkWebException(e);
				}
			}
		}

		return statusData;
	}

	/**
	 * Retrieves current step of job processing.
	 * @param jobId identifier of the job
	 * @param input job input
	 * @return current step
	 * @throws Exception when can not retrieve current step
	 */
	private StepStatus retrieveCurrentStep(String jobId, String input) throws Exception
	{
		StepStatus stepStatus = new StepStatus();
		stepStatus.setTotalNumberOfSteps(0);
		stepStatus.setCurrentStepNumber(0);
		stepStatus.setCurrentStep("Waiting");

		File dataDirectory = new File(generalDestinationDirectoryName, jobId);

		if (IOUtil.checkIfDirectoryExist(dataDirectory))
		{
			try
			{
				String stepFileName = input;
				
				if(stepFileName.contains("."))
			    {
					stepFileName = stepFileName.substring(0, stepFileName.lastIndexOf("."));
			    }
				
				File stepFile = new File(dataDirectory, stepFileName + ".steps.log");

				if(stepFile.exists())
				{
					Properties stepProperties = new Properties();
					FileInputStream inputStream = new FileInputStream(stepFile);
					stepProperties.load(inputStream);

					stepStatus.setCurrentStep(stepProperties.getProperty("step"));
					stepStatus.setCurrentStepNumber(Integer.parseInt(stepProperties.getProperty("step_num")));
					stepStatus.setTotalNumberOfSteps(Integer.parseInt(stepProperties.getProperty("step_total")));
				}
			}
			catch(Throwable t)
			{
				throw new CrkWebException(t);
			}
		}

		return stepStatus;
	}

	/**
	 * Retrieves pdb score item for job.
	 * @param jobId identifier of the job
	 * @return pdb score item
	 * @throws Exception when can not retrieve result of the job
	 */
	private PdbInfo getResultData(String jobId) throws Exception
	{
		JobDAO jobDAO = new JobDAOJpa();
		InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBScore(jobId);
		
		InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOJpa();
		List<InterfaceCluster> clusters = clusterDAO.getInterfaceClustersWithoutInterfaces(pdbInfo.getUid());
		pdbInfo.setInterfaceClusters(clusters);

		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
		for(InterfaceCluster cluster: clusters){
			List<Interface> interfaceItems = interfaceDAO.getInterfacesWithScores(cluster.getUid());
			cluster.setInterfaces(interfaceItems);
		}
		
		ChainClusterDAO chainClusterDAO = new ChainClusterDAOJpa();
		List<ChainCluster> chainClusters = chainClusterDAO.getChainClusters(pdbInfo.getUid());
		pdbInfo.setChainClusters(chainClusters);

		pdbInfo.setInputType(input.getInputType());
		pdbInfo.setInputName(input.getInputName());

		return pdbInfo;
	}

	@Override
	public ResiduesList getAllResidues(String jobId) throws Exception
	{
		ResidueDAO residueDAO = new ResidueDAOJpa();
		ResiduesList residuesList = residueDAO.getResiduesForAllInterfaces(jobId);
		return residuesList;
	}

	@Override
	public HashMap<Integer, List<Residue>> getInterfaceResidues(int interfaceUid) throws Exception
	{
		ResidueDAO residueDAO = new ResidueDAOJpa();
		List<Residue> interfaceResidues = residueDAO.getResiduesForInterface(interfaceUid);

		HashMap<Integer, List<Residue>> structures = new HashMap<Integer, List<Residue>>();

		List<Residue> firstStructureResidues = new ArrayList<Residue>();
		List<Residue> secondStructureResidues = new ArrayList<Residue>();

		for(Residue residue : interfaceResidues)
		{
			if(residue.getSide() == 1)
			{
				firstStructureResidues.add(residue);
			}
			else if(residue.getSide() == 2)
			{
				secondStructureResidues.add(residue);
			}
		}

		structures.put(1, firstStructureResidues);
		structures.put(2, secondStructureResidues);

		return structures;
	}

	@Override
	public JobsForSession getJobsForCurrentSession() throws Exception
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		JobDAO jobDAO = new JobDAOJpa();
		List<ProcessingInProgressData> jobs = jobDAO.getJobsForSession(sessionId);
		
		HttpSession session = getThreadLocalRequest().getSession();
		boolean isSessionNew = false;
		
		if(!SessionValidator.isSessionValid(session))
		{
			SessionValidator.validateSession(session);
			isSessionNew = true;
		}
		
		JobsForSession jobsForSession = new JobsForSession(isSessionNew, jobs);
		return jobsForSession;
	}

	@Override
	public String stopJob(String jobId) throws Exception
	{
		String result = null;

		try
		{
			JobDAO jobDAO = new JobDAOJpa();
			String submissionId = jobDAO.getSubmissionIdForJobId(jobId);
			
			File jobDirectory = new File(generalDestinationDirectoryName, jobId);
			File logFile = new File(jobDirectory, "crklog");
			File killFile = new File(jobDirectory, "killed");
			killFile.createNewFile();
			
			jobManager.stopJob(submissionId);
			
			result = "Job: " + jobId + " was stopped";
			
			LogHandler.writeToLogFile(logFile, result);

			jobDAO.updateStatusOfJob(jobId, StatusOfJob.STOPPED.getName());
		}
		catch(IOException e)
		{
			e.printStackTrace();
			result = "Job: " + jobId + " was not stopped";
		}
		catch(DaoException e)
		{
			e.printStackTrace();
			result = "Job: " + jobId + " was not stopped";
		}
		catch(JobHandlerException e)
		{
			e.printStackTrace();
			result = "Job: " + jobId + " was not stopped";
		}

		return result;
	}

	@Override
	public String deleteJob(String jobId) throws Exception
	{
		String sessionId = getThreadLocalRequest().getSession().getId();

		JobDAO jobDAO = new JobDAOJpa();
		String status = jobDAO.getStatusForJob(jobId);

		if((status.equals(StatusOfJob.RUNNING.getName())) ||
		   (status.equals(StatusOfJob.WAITING.getName())) ||
		   (status.equals(StatusOfJob.QUEUING.getName())))
		{
			stopJob(jobId);
		}

		jobDAO.untieSelectedJobFromSession(sessionId, jobId);

		String result = "Job: " + jobId + " was removed";
		return result;
	}

	@Override
	public void untieJobsFromSession() throws Exception
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		JobDAO jobDAO = new JobDAOJpa();
		jobDAO.untieJobsFromSession(sessionId);
	}

	@Override
	public void destroy()
	{
		super.destroy();

		jobStatusUpdater.setRunning(false);
		
		while(jobStatusUpdater.isUpdating())
		{
			
		}
		
		try
		{
			jobManager.finalize();
		}
		catch (JobHandlerException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public PagingLoadResult<PDBSearchResult> getListOfPDBsHavingAUniProt(
			FilterPagingLoadConfig config, String uniProtId) throws Exception {

		ChainClusterDAO homologsDAO = new ChainClusterDAOJpa();
		List<PDBSearchResult> resultList = homologsDAO.getPdbSearchItemsForUniProt(uniProtId);

		//get all the comments from the data store  
		//and sort this list according to sorting info  

		if (config.getSortInfo() != null && config.getSortInfo().size() > 0 ) {  
			SortInfo sortinfo = config.getSortInfo().get(0);
			final String propertyName = sortinfo.getSortField();
			final SortDir sortDir = sortinfo.getSortDir(); 
			if (propertyName != null) {  
				Collections.sort(resultList, new Comparator<PDBSearchResult>() {
					@Override
					public int compare(PDBSearchResult a, PDBSearchResult b) {
						int returnValue = 0;
						if("pdbCode".equals(propertyName))
							returnValue = new String(a.getPdbCode()).compareTo(b.getPdbCode());
						if("title".equals(propertyName))
							returnValue = new String(a.getTitle()).compareTo(b.getTitle());
						if("releaseDate".equals(propertyName))
							returnValue = (a.getReleaseDate().compareTo(b.getReleaseDate()));
						if("spaceGroup".equals(propertyName))
							returnValue = new String(a.getSpaceGroup()).compareTo(b.getSpaceGroup());
						if("resolution".equals(propertyName))
							returnValue = new Double(a.getResolution()).compareTo(b.getResolution());
						if("rfreeValue".equals(propertyName))
							returnValue = new Double(a.getRfreeValue()).compareTo(b.getRfreeValue());
						if("expMethod".equals(propertyName))
							returnValue = new String(a.getExpMethod()).compareTo(b.getExpMethod());
						
						if(SortDir.ASC == sortDir)
							return returnValue;
						else
							return -1 * returnValue;
					}

				});  
			}  
		}  

		//Create a sublist and add data to list according  
		//to the limit and offset value of the config  

		ArrayList<PDBSearchResult> sublist = new ArrayList<PDBSearchResult>();  
		int start = config.getOffset();  
		int limit = resultList.size();  
		if (config.getLimit() > 0) {  
			limit = Math.min(start + config.getLimit(), limit);  
		}  
		
		for (int i = config.getOffset(); i < limit; i++) {         
			sublist.add(resultList.get(i));       
		}         
		
		return new PagingLoadResultBean<PDBSearchResult>(sublist, resultList.size(),config.getOffset());  
	} 
}

