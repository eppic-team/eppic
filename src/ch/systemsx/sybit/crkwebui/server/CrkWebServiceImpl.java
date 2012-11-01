package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.persistence.PersistenceContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import ch.systemsx.sybit.crkwebui.server.db.model.HomologsInfoItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.HomologsInfoItemDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.InputWithType;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceItemDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceResidueItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceResidueItemDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.PDBScoreDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.PDBScoreDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.UserSessionDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.UserSessionDAOImpl;
import ch.systemsx.sybit.crkwebui.server.generators.ApplicationSettingsGenerator;
import ch.systemsx.sybit.crkwebui.server.generators.DirectoryContentGenerator;
import ch.systemsx.sybit.crkwebui.server.generators.RandomDirectoryNameGenerator;
import ch.systemsx.sybit.crkwebui.server.managers.JobManager;
import ch.systemsx.sybit.crkwebui.server.managers.JobManagerFactory;
import ch.systemsx.sybit.crkwebui.server.managers.JobStatusUpdater;
import ch.systemsx.sybit.crkwebui.server.util.FileContentReader;
import ch.systemsx.sybit.crkwebui.server.util.IOUtil;
import ch.systemsx.sybit.crkwebui.server.util.LogHandler;
import ch.systemsx.sybit.crkwebui.server.validators.IPVerifier;
import ch.systemsx.sybit.crkwebui.server.validators.PreSubmitValidator;
import ch.systemsx.sybit.crkwebui.server.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.validators.SessionValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;
import ch.systemsx.sybit.crkwebui.shared.model.JobsForSession;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;
import ch.systemsx.sybit.crkwebui.shared.model.StepStatus;

import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;

import crk.CRKParams;

/**
 * The server side implementation of the RPC service.
 *
 * @author srebniak_a
 */
@PersistenceContext(name="crkjpa", unitName="crkjpa")
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
	
//	private JobDAO jobDao;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		
//		WebApplicationContext webContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
//		AutowireCapableBeanFactory beanFactory = ctx.getAutowireCapableBeanFactory();
//		beanFactory.autowireBean(this);
//		jobDao = (JobDAO)webContext.getBean("jobDao");

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

		// String realPath =
		// getServletContext().getRealPath(properties.getProperty("destination_path"));
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
		
		Properties crkProperties = new Properties();

		try
		{
			File crkPropertiesFile = new File(System.getProperty("user.home"), CRKParams.CONFIG_FILE_NAME);
			crkProperties.load(new FileInputStream(crkPropertiesFile));
		}
		catch (IOException e)
		{
			throw new ServletException("Crk properties file can not be read");
		}

		localCifDir = crkProperties.getProperty("LOCAL_CIF_DIR");

		
		InputStream queuingSystemPropertiesStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/META-INF/" + queuingSystem + "_queuing_system.properties");

		Properties queuingSystemProperties = new Properties();

		try
		{
			queuingSystemProperties.load(queuingSystemPropertiesStream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ServletException("Properties file for " + queuingSystem + " can not be read");
		}

		
		try
		{
			jobManager = JobManagerFactory.getJobManager(queuingSystem,
														 queuingSystemProperties,
														 generalDestinationDirectoryName);
		}
		catch(JobManagerException e)
		{
			throw new ServletException(e);
		}

//		String serverName =  getThreadLocalRequest().getServerName();
//		int serverPort = getThreadLocalRequest().getServerPort();
		
		String serverName = getServletContext().getInitParameter("serverName");
		
		resultsPathUrl = protocol + "://" + serverName;
		
		String serverPortValue = getServletContext().getInitParameter("serverPort");
		if((serverPortValue != null) &&
		   (!serverPortValue.equals("")))
		{
			int serverPort = Integer.parseInt(serverPortValue);
			resultsPathUrl += ":" + serverPort;
		}

		resultsPathUrl += "/crkwebui/";

		EmailData emailData = new EmailData();
		emailData.setEmailSender(properties.getProperty("email_username", ""));
		emailData.setEmailSenderPassword(properties.getProperty("email_password", ""));
		emailData.setHost(properties.getProperty("email_host"));
		emailData.setPort(properties.getProperty("email_port"));
		emailSender = new EmailSender(emailData);

		crkRunner = new CrkRunner(jobManager, 
								  crkApplicationLocation,
								  nrOfThreadForSubmission);

		jobStatusUpdater = new JobStatusUpdater(jobManager,
				new JobDAOImpl(),
				resultsPathUrl,
				emailSender,
				generalDestinationDirectoryName);
		jobDaemon = new Thread(jobStatusUpdater);
		jobDaemon.start();
		
//		try
//		{		
//			ObjectInputStream in = new ObjectInputStream(new FileInputStream("c:/results/1smt/1smt.webui.dat"));
//		
//			PDBScoreItemDB pdbScoreItem = (PDBScoreItemDB)in.readObject();
//
//			EntityManager entityManager = EntityManagerHandler.getEntityManager();
//
//			entityManager.getTransaction().begin();
//			entityManager.persist(pdbScoreItem);
//	
//			String pdbCode = pdbScoreItem.getPdbName();
//
//			JobDB job = new JobDB();
//			job.setJobId(pdbCode);
//			job.setEmail(null);
//			job.setInput(pdbCode);
//			job.setIp("localhost");
//			job.setStatus(StatusOfJob.FINISHED.getName());
//			job.setSubmissionDate(new Date());
//			job.setInputType(InputType.PDBCODE.getIndex());
//			job.setSubmissionId("-1");
//
//			pdbScoreItem.setJobItem(job);
//			job.setPdbScoreItem(pdbScoreItem);
//			entityManager.persist(job);
//
//			entityManager.getTransaction().commit();
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 *
	 * @param html
	 *            the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	@Override
	public ApplicationSettings loadSettings() throws Exception
	{
		ApplicationSettingsGenerator applicationSettingsGenerator = new ApplicationSettingsGenerator(properties);
		
		InputStream inputParametersStream = getServletContext()
				.getResourceAsStream("/WEB-INF/classes/META-INF/input_parameters.xml");
		
		InputStream helpPageStream = getServletContext()
				.getResourceAsStream("/WEB-INF/classes/META-INF/help.html");
		
		InputStream downloadsPageStream = getServletContext()
				.getResourceAsStream("/WEB-INF/classes/META-INF/downloads.html");
		
		InputStream gridPropertiesInputStream = getServletContext()
				.getResourceAsStream("/WEB-INF/classes/META-INF/grid.properties");

		ApplicationSettings settings = applicationSettingsGenerator.generateApplicationSettings(inputParametersStream, 
																								helpPageStream,
																								downloadsPageStream,
																								gridPropertiesInputStream);


		JobDAO jobDAO = new JobDAOImpl();
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
				String randomDirectoryName = RandomDirectoryNameGenerator.generateRandomDirectory(generalDestinationDirectoryName);
				runJobData.setJobId(randomDirectoryName);
				inputType = InputType.PDBCODE.getIndex();
			}

			RunJobDataValidator.validateJobId(runJobData.getJobId());
			
			String localDestinationDirName = generalDestinationDirectoryName + File.separator + runJobData.getJobId();

			Date currentDate = new Date();

			JobDAO jobDAO = new JobDAOImpl();

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
			
			String emailTitle = "EPPIC: " + runJobData.getInput();
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
				
				emailTitle += " submitted";
				emailMessage = runJobData.getInput()
								 + " job submitted. To see the status of the processing please go to: "
								 + resultsPathUrl + "#id=" + runJobData.getJobId();
			}
			catch(Throwable e)
			{
				LogHandler.writeToLogFile(logFile, e.getMessage());

				submissionStatus = StatusOfJob.ERROR;

				emailTitle += " - error while submitting the job.\n\n";
				emailMessage = e.getMessage() + 
							   "  To see more details go to: "
								 + resultsPathUrl + "#id=" + runJobData.getJobId();
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

		JobDAO jobDAO = new JobDAOImpl();
		status = jobDAO.getStatusForJob(jobId);

		if(status != null)
		{
			UserSessionDAO sessionDAO = new UserSessionDAOImpl();
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
		JobDAO jobDAO = new JobDAOImpl();

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
				statusData.setInput(inputWithType.getInput());
				
				statusData.setStep(new StepStatus());

				try
				{
					List<File> filesToRead = new ArrayList<File>();

					File logFile = new File(dataDirectory, "crklog");
					
					if (IOUtil.checkIfFileExist(logFile))
					{
						filesToRead.add(new File(dataDirectory, "crklog"));
					}

					File[] directoryContent = DirectoryContentGenerator.getFilesNamesWithPrefix(dataDirectory, jobId + ".e");
						
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
	private PDBScoreItem getResultData(String jobId) throws Exception
	{
		JobDAO jobDAO = new JobDAOImpl();
		int inputType = jobDAO.getInputTypeForJob(jobId);

		PDBScoreDAO pdbScoreDAO = new PDBScoreDAOImpl();
		PDBScoreItem pdbScoreItem = pdbScoreDAO.getPDBScore(jobId);

		InterfaceItemDAO interfaceItemDAO = new InterfaceItemDAOImpl();
		List<InterfaceItem> interfaceItems = interfaceItemDAO.getInterfacesWithScores(pdbScoreItem.getUid());
		pdbScoreItem.setInterfaceItems(interfaceItems);

		HomologsInfoItemDAO homologsInfoItemDAO = new HomologsInfoItemDAOImpl();
		List<HomologsInfoItem> homologsInfoItems = homologsInfoItemDAO.getHomologsInfoItems(pdbScoreItem.getUid());
		pdbScoreItem.setHomologsInfoItems(homologsInfoItems);

		pdbScoreItem.setInputType(inputType);

		return pdbScoreItem;
	}

	@Override
	public InterfaceResiduesItemsList getAllResidues(int pdbScoreId) throws Exception
	{
		InterfaceResidueItemDAO interfaceResidueItemDAO = new InterfaceResidueItemDAOImpl();
		InterfaceResiduesItemsList interfaceResiduesItemsList = interfaceResidueItemDAO.getResiduesForAllInterfaces(pdbScoreId);
		return interfaceResiduesItemsList;
	}

	@Override
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(int interfaceUid) throws Exception
	{
		InterfaceResidueItemDAO interfaceResidueItemDAO = new InterfaceResidueItemDAOImpl();
		List<InterfaceResidueItem> interfaceResidues = interfaceResidueItemDAO.getResiduesForInterface(interfaceUid);

		HashMap<Integer, List<InterfaceResidueItem>> structures = new HashMap<Integer, List<InterfaceResidueItem>>();

		List<InterfaceResidueItem> firstStructureResidues = new ArrayList<InterfaceResidueItem>();
		List<InterfaceResidueItem> secondStructureResidues = new ArrayList<InterfaceResidueItem>();

		for(InterfaceResidueItem interfaceResidueItem : interfaceResidues)
		{
			if(interfaceResidueItem.getStructure() == 1)
			{
				firstStructureResidues.add(interfaceResidueItem);
			}
			else if(interfaceResidueItem.getStructure() == 2)
			{
				secondStructureResidues.add(interfaceResidueItem);
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
		JobDAO jobDAO = new JobDAOImpl();
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
			JobDAO jobDAO = new JobDAOImpl();
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

		JobDAO jobDAO = new JobDAOImpl();
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
		JobDAO jobDAO = new JobDAOImpl();
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
}
