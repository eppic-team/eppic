package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.PersistenceContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import eppic.db.dao.*;
import eppic.db.dao.jpa.*;
import eppic.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebService;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirectoryContentReader;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.FileContentReader;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.IOUtil;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.RandomDirectoryGenerator;
import ch.systemsx.sybit.crkwebui.server.commons.util.log.LogHandler;
import ch.systemsx.sybit.crkwebui.server.commons.validators.PreSubmitValidator;
import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.commons.validators.SessionValidator;
import ch.systemsx.sybit.crkwebui.server.email.data.EmailData;
import ch.systemsx.sybit.crkwebui.server.email.data.EmailMessageData;
import ch.systemsx.sybit.crkwebui.server.email.managers.EmailSender;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.JobManagerFactory;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobStatusUpdater;
import ch.systemsx.sybit.crkwebui.server.runners.CrkRunner;
import ch.systemsx.sybit.crkwebui.server.settings.generators.ApplicationSettingsGenerator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;
import eppic.model.shared.InputType;
import eppic.model.shared.StatusOfJob;

import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;

import eppic.EppicParams;
import eppic.db.EntityManagerHandler;
import eppic.db.jpautils.DbConfigGenerator;

/**
 * The server side implementation of the RPC service.
 *
 * @author srebniak_a
 */
@PersistenceContext(name="eppicjpa", unitName="eppicjpa")
public class CrkWebServiceImpl extends XsrfProtectedServiceServlet implements CrkWebService
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(CrkWebServiceImpl.class);

	// NOTE for the unaware reader: the EPPIC program used to be call CRK, thus the prevalence of that name in the code

	// config file locations
	
	// the within-war location for server config files, read with getServletContext().getResourceAsStream()
	private static final String CONFIG_FILES_RESOURCE_LOCATION = "/WEB-INF/classes/META-INF";

	/**
	 * The property that needs to be passed to JVM with a -D to specify the 
	 * directory where all config files are located (the "config profile").
	 */
	public static final String CONFIG_DIR_PROPERTY	  = "eppicWuiConfigDir";
	
	// the off-war location for server config files
	private static final String CONFIG_FILES_LOCATION = System.getProperty(CONFIG_DIR_PROPERTY);
	
	static {
		if (CONFIG_FILES_LOCATION==null || CONFIG_FILES_LOCATION.isEmpty()) {
			logger.error("Property {} wasn't specified correctly. The property must be passed to the JVM with a -D parameter", CONFIG_DIR_PROPERTY);
			throw new RuntimeException("Can't continue without a valid config file dir");
		} else if (! new File(CONFIG_FILES_LOCATION).isDirectory()) {
			logger.error("The value '{}' specified for the config files directory with system property {} is not a directory!", CONFIG_FILES_LOCATION , CONFIG_DIR_PROPERTY);
			throw new RuntimeException("Can't continue without a valid config file dir");
		} else {
			logger.info("Reading configuration files from dir {}, passed through property {}", CONFIG_FILES_LOCATION, CONFIG_DIR_PROPERTY);
		}
	}

	public static final String SERVER_PROPERTIES_FILE 	= CONFIG_FILES_LOCATION+"/server.properties";
	private static final String EMAIL_PROPERTIES_FILE   = CONFIG_FILES_LOCATION + "/email.properties";
	private static final String INPUT_PARAMS_FILE 		= CONFIG_FILES_LOCATION+"/input_parameters.xml";

	// note: grid.properties we read from within-war file, then from server-config dir if there is one
	// that way we can still externally configure, whilst keeping a default config in the packed war file
	private static final String GRID_PROPERTIES_FILE_RESOURCE 	= CONFIG_FILES_RESOURCE_LOCATION+"/grid.properties";
	private static final String GRID_PROPERTIES_FILE 	= CONFIG_FILES_LOCATION+"/grid.properties";

	// the file with the eppicjpa settings for database access through hibernate
	public static final String DB_PROPERTIES_FILE = CONFIG_FILES_LOCATION + "/eppic-db.properties";

	/**
	 * The settings to be passed to EntityManagerHandler to initialise the JPA connection
	 */
	public static Map<String,String> dbSettings;

	static {
		// initialising db settings
		try {
			File dbPropertiesFile =  new File(DB_PROPERTIES_FILE);
			if (!dbPropertiesFile.exists()) {
				logger.error("The db properties file {} does not exist!",dbPropertiesFile);
			} else {
				logger.info("Reading db properties file {}", dbPropertiesFile);
				dbSettings = DbConfigGenerator.createDatabaseProperties(dbPropertiesFile);
			}
		} catch (IOException e) {
			logger.error("Could not read all needed properties from db config file {}. Error: {}",
					DB_PROPERTIES_FILE, e.getMessage());

		}
	}

	// the file where the progress log of the eppic CLI program is written to (using -L option), used to be called 'crklog'
	public static final String PROGRESS_LOG_FILE_NAME 	= "eppic_wui_progress.log";

	// the file to signal a killed job
	public static final String KILLED_FILE_NAME 		= "killed";

	// the file that the eppic CLI writes upon successful completion, used to signal that the queuing job finished successfully
	public static final String FINISHED_FILE_NAME 		= EppicParams.FINISHED_FILE_NAME;

	// the suffix of the filename used for writing the steps for the progress animation
	public static final String STEPS_FILE_NAME_SUFFIX 	= ".steps.log";

	// general server settings
	private Properties properties;

	private String generalTmpDirectoryName;
	private String generalDestinationDirectoryName;

	private String javaVMExec;

	private String crkApplicationLocation;

	private String protocol = "http";

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
		
		logger.info("EPPIC wui server property files will be read from directory {}", CONFIG_FILES_LOCATION);
		
		if ( !new File(CONFIG_FILES_LOCATION).exists() || !new File(CONFIG_FILES_LOCATION).isDirectory() )  {
			throw new ServletException("The configuration files directory "+CONFIG_FILES_LOCATION+" does not exist or is not a directory.");
		}

		
		properties = new Properties();

		try	{
			logger.info("Reading server properties file "+SERVER_PROPERTIES_FILE);
			InputStream propertiesStream = 
					new FileInputStream(new File(SERVER_PROPERTIES_FILE));
			properties.load(propertiesStream);
		}
		catch (IOException e) {
			logger.error("Could not read server properties file "+SERVER_PROPERTIES_FILE);
			throw new ServletException("Properties file '"+SERVER_PROPERTIES_FILE+"' can not be read. Error: "+e.getMessage());
		}

		generalTmpDirectoryName = properties.getProperty("tmp_path");
		File tmpDir = new File(generalTmpDirectoryName);

		if (!tmpDir.isDirectory())
		{
			throw new ServletException("Temp path set in config file ('tmp_path' option): "+ generalTmpDirectoryName + " is not a directory");
		}

		generalDestinationDirectoryName = properties.getProperty("destination_path");
		File destinationDir = new File(generalDestinationDirectoryName);
		if (!destinationDir.isDirectory())
		{
			throw new ServletException("Destination path set in config file ('destination_path' option): "+generalDestinationDirectoryName + " is not a directory");
		}

		crkApplicationLocation = properties.getProperty("crk_jar");

		if (crkApplicationLocation == null)
		{
			throw new ServletException("Location of EPPIC jar file not specified (set the 'crk_jar' option in config file)");
		}

		if(properties.getProperty("java_VM_exec") != null && !properties.getProperty("java_VM_exec").equals("")){
			javaVMExec = properties.getProperty("java_VM_exec");
			logger.info("Using java VM given in config file ('java_VM_exec' option): "+javaVMExec);
		} else{
			logger.info("No 'java_VM_exec' option specified in config file, using system's default java");
			javaVMExec = "java";
		}

		if(properties.getProperty("protocol") != null)
		{
			protocol = properties.getProperty("protocol");
		}

		int numWorkersJobManager;
		String numWorkers = properties.getProperty("num_workers");
		if(numWorkers == null)
		{
			throw new ServletException("Number of workers for job manager not specified");
		}
		try {
			numWorkersJobManager = Integer.parseInt(numWorkers);
		} catch (NumberFormatException e) {
			throw new ServletException("Property num_workers did not specify an integer correctly");
		}

		int nrOfThreadForSubmission = Integer.parseInt(properties.getProperty("nr_of_threads_for_submission","1"));
		int assignedMemory = Integer.parseInt(properties.getProperty("assigned_memory","512"));

		Properties eppicProperties = new Properties();

		try
		{
			File eppicConfigFile = new File(System.getProperty("user.home"), EppicParams.CONFIG_FILE_NAME);
			eppicProperties.load(new FileInputStream(eppicConfigFile));
		}
		catch (IOException e)
		{
			throw new ServletException("EPPIC config file ("+EppicParams.CONFIG_FILE_NAME+") can not be read in home directory. Error: "+e.getMessage());
		}

		localCifDir = eppicProperties.getProperty("LOCAL_CIF_DIR");

		if (localCifDir==null || !new File(localCifDir).isDirectory()) {
			logger.warn("The LOCAL_CIF_DIR path is either not set or not pointing to a readable directory.");	
		}

		if(!properties.containsKey(ApplicationSettingsGenerator.DEVELOPMENT_MODE) ||
			properties.getProperty(ApplicationSettingsGenerator.DEVELOPMENT_MODE).equals("true")) {
		
			initializeJobManager(numWorkersJobManager);
		} else {
			logger.warn("{} is set to true in config file. There will be no queuing system available!",ApplicationSettingsGenerator.DEVELOPMENT_MODE);
		}

		//String serverName = getServletContext().getInitParameter("serverName");

		String serverName = "eppicserver";
		if (properties.getProperty("server_name")!=null && !properties.getProperty("server_name").equals("")) {
			serverName = properties.getProperty("server_name");
		} else {
			logger.warn("The server_name property was not set in file '{}'. The URL sent in emails will be wrong!",SERVER_PROPERTIES_FILE );
		}
		
		resultsPathUrl = protocol + "://" + serverName;
		
		if (properties.getProperty("server_port")!=null && !properties.getProperty("server_port").equals("")) {
		
			int serverPort = Integer.parseInt(properties.getProperty("server_port"));
			resultsPathUrl += ":" + serverPort;
		}

		// this comes from the web.xml file
		String servletContPath = getServletContext().getInitParameter("servletContPath");
		resultsPathUrl += "/" + servletContPath + "/";
		
		logger.info("Initialised URL suffix for email messages to {}",resultsPathUrl);

		EmailData emailData = new EmailData();
		emailData.setEmailSenderUserName(properties.getProperty("email_username", ""));
		emailData.setEmailSenderPassword(properties.getProperty("email_password", ""));
		emailData.setReplyToAddress(properties.getProperty("email_replyto_address", ""));
		emailData.setHost(properties.getProperty("email_host"));
		emailData.setPort(properties.getProperty("email_port"));
		emailSender = new EmailSender(emailData);



		//InputStream emailPropertiesStream = getServletContext()
		//	.getResourceAsStream(EMAIL_PROPERTIES_FILE);

		Properties emailProperties = new Properties();

		emailMessageData = new EmailMessageData();

		try	{
			
			logger.info("Reading email property file "+EMAIL_PROPERTIES_FILE);
			InputStream emailPropertiesStream = 
					new FileInputStream(new File(EMAIL_PROPERTIES_FILE));

			emailProperties.load(emailPropertiesStream);
		}
		catch (IOException e) {
			
			logger.error("Could not read email properties file "+EMAIL_PROPERTIES_FILE);
			throw new ServletException("Email properties file '"+EMAIL_PROPERTIES_FILE+"' can not be read. Error: "+e.getMessage());
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

		logger.info("Initialising jpa/hibernate");
		EntityManagerHandler.initFactory(dbSettings);
		logger.info("Done initialising jpa/hibernate");

		crkRunner = new CrkRunner(jobManager, 
				crkApplicationLocation,
				nrOfThreadForSubmission,
				assignedMemory,
				javaVMExec);
		if(!properties.containsKey(ApplicationSettingsGenerator.DEVELOPMENT_MODE) ||
			properties.getProperty(ApplicationSettingsGenerator.DEVELOPMENT_MODE).equals("true")) {

			logger.info("Proceeding to spawn the job status updater daemon");
			jobStatusUpdater = new JobStatusUpdater(jobManager,
					new JobDAOJpa(),
					resultsPathUrl,
					emailSender,
					emailMessageData,
					generalDestinationDirectoryName);

			if (properties.containsKey("queue_reporting_interval")) {
				try {
					int interval = Integer.parseInt(properties.getProperty("queue_reporting_interval"));
					jobStatusUpdater.setLogQueueInterval(interval);
				} catch (NumberFormatException e) {
					logger.warn("Property 'queue_reporting_interval' not correctly specified, will use default. Error: {}", e.getMessage());
				}
			}


			jobDaemon = new Thread(jobStatusUpdater);
			jobDaemon.start();
		} else {
			logger.warn("{} is set to true in config file. No job status updater daemon will run.",ApplicationSettingsGenerator.DEVELOPMENT_MODE);
		}

	}

	private void initializeJobManager(int numWorkersJobManager) throws ServletException {
		logger.info("Proceeding to initialise job manager ");

		try {
			jobManager = JobManagerFactory.getJobManager(generalDestinationDirectoryName, numWorkersJobManager);
		}catch(JobManagerException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public ApplicationSettings loadSettings() throws Exception
	{
		ApplicationSettingsGenerator applicationSettingsGenerator = new ApplicationSettingsGenerator(properties);

		InputStream inputParametersStream = new FileInputStream(new File(INPUT_PARAMS_FILE));

		// grid properties: we read them first from the war-packed resource. Then from the file system (which will override them)
		InputStream gridPropertiesInputStream = getServletContext()
				.getResourceAsStream(GRID_PROPERTIES_FILE_RESOURCE);
		
		File gridPropertiesFile = new File(GRID_PROPERTIES_FILE);
		if (gridPropertiesFile.exists()) {
			logger.warn("Grid properties are being taken from file in config dir: {}", GRID_PROPERTIES_FILE); 
			gridPropertiesInputStream = new FileInputStream(gridPropertiesFile);
		}

		ApplicationSettings settings = applicationSettingsGenerator.generateApplicationSettings(inputParametersStream, 
				gridPropertiesInputStream);

		try {
			UniProtMetadataDAO uniDAO = new UniProtMetadataDAOJpa();
			UniProtMetadata uniProtMetadata = uniDAO.getUniProtMetadata();
			settings.setUniprotVersion(uniProtMetadata.getVersion());
		} catch (DaoException e) {
			settings.setUniprotVersion("");
			logger.warn("Could not get UniProt version from database UniProtMetadata table. Version won't be available in home page. Error: {}", e.getMessage());
		}

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

			File logFile = new File(localDestinationDirName, PROGRESS_LOG_FILE_NAME);

			try
			{
				logFile.createNewFile();
			}
			catch(IOException e)
			{
				logger.warn("Problems creating progress log file "+logFile+". Error: "+e.getMessage());
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
				logger.error("Something went wrong during job submission. Error: " + e.getMessage(), e);
				
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

			
			emailSender.sendInSeparateThread(runJobData.getEmailAddress(),
						emailTitle,
						emailMessage);

			return runJobData.getJobId();
		}

		return null;
	}

	@Override
	public ProcessingData getResultsOfProcessing(String jobId) throws Exception //whaveter calls this fails
	{
		StatusOfJob status = null;

		JobDAO jobDAO = new JobDAOJpa();
		status = jobDAO.getStatusForJob(jobId);

		if(status != null)
		{
			UserSessionDAO sessionDAO = new UserSessionDAOJpa();
			sessionDAO.insertSessionForJob(getThreadLocalRequest().getSession().getId(), jobId, getThreadLocalRequest().getRemoteAddr());
			if(status.equals(StatusOfJob.FINISHED))
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

	private ProcessingInProgressData getStatusData(final String jobId, StatusOfJob status) throws Exception
	{
		
		JobDAO jobDAO = new JobDAOJpa();

		ProcessingInProgressData statusData = null;

		if((jobId != null) && (!jobId.equals("")))
		{
			File dataDirectory = DirLocatorUtil.getJobDir(new File(generalDestinationDirectoryName), jobId);

			if (IOUtil.checkIfDirectoryExist(dataDirectory))
			{
				statusData = new ProcessingInProgressData();

				statusData.setJobId(jobId);
				statusData.setStatus(status.getName());

				InputWithType inputWithType = jobDAO.getInputWithTypeForJob(jobId);
				statusData.setInputType(inputWithType.getInputType());
				statusData.setInputName(inputWithType.getInputName());

				statusData.setStep(new StepStatus());

				try
				{
					List<File> filesToRead = new ArrayList<File>();

					File logFile = new File(dataDirectory, PROGRESS_LOG_FILE_NAME);

					if (IOUtil.checkIfFileExist(logFile))
					{
						filesToRead.add(logFile);
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

					if((status.equals(StatusOfJob.RUNNING)) ||
							(status.equals(StatusOfJob.WAITING)) ||
							(status.equals(StatusOfJob.QUEUING)))
					{
						statusData.setStep(retrieveCurrentStep(jobId, statusData.getInputName()));
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

		File dataDirectory = DirLocatorUtil.getJobDir(new File(generalDestinationDirectoryName), jobId);

		if (IOUtil.checkIfDirectoryExist(dataDirectory))
		{
			try
			{
				String stepFileName = input;

				if(stepFileName.contains("."))
				{
					stepFileName = stepFileName.substring(0, stepFileName.lastIndexOf("."));
				}

				File stepFile = new File(dataDirectory, stepFileName + STEPS_FILE_NAME_SUFFIX);

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
	 * Retrieves pdbInfo item for a given job id.
	 * @param jobId identifier of the job
	 * @return pdbInfo item
	 * @throws Exception when can not retrieve result of the job
	 */
	private PdbInfo getResultData(String jobId) throws Exception
	{
		JobDAO jobDAO = new JobDAOJpa();
		InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
		
		AssemblyDAO assemblyDAO = new AssemblyDAOJpa();
		List<Assembly> assemblies = assemblyDAO.getAssemblies(pdbInfo.getUid(), true, false);
		pdbInfo.setAssemblies(assemblies); 

		InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOJpa();
		List<InterfaceCluster> clusters = clusterDAO.getInterfaceClusters(pdbInfo.getUid(), true, false);

		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
		for(InterfaceCluster cluster: clusters){
			List<Interface> interfaceItems = interfaceDAO.getInterfacesForCluster(cluster.getUid(), true, false);
			cluster.setInterfaces(interfaceItems);
		}
		pdbInfo.setInterfaceClusters(clusters);

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
			if(residue.getSide() == false)
			{
				firstStructureResidues.add(residue);
			}
			else if(residue.getSide() == true)
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

			File jobDirectory = DirLocatorUtil.getJobDir(new File(generalDestinationDirectoryName), jobId);
			File logFile = new File(jobDirectory, PROGRESS_LOG_FILE_NAME);
			File killFile = new File(jobDirectory, KILLED_FILE_NAME);
			killFile.createNewFile();

			jobManager.stopJob(submissionId);

			result = "Job: " + jobId + " was stopped";

			LogHandler.writeToLogFile(logFile, result);

			jobDAO.updateStatusOfJob(jobId, StatusOfJob.STOPPED);
		}
		catch(IOException e)
		{
			logger.warn("The kill file ("+KILLED_FILE_NAME+") to signal the job stopping could not be written. Error: "+e.getMessage()); 
			result = "Job: " + jobId + " was not stopped";
		}
		catch(DaoException e)
		{
			logger.warn("Job could not be stopped due to a DaoException. Error: "+e.getMessage());	    
			result = "Job: " + jobId + " was not stopped";
		}
		catch(JobHandlerException e)
		{
			logger.warn("Job could not be stopped due to a JobHandlerException. Error: "+e.getMessage());
			result = "Job: " + jobId + " was not stopped";
		}

		return result;
	}

	@Override
	public String deleteJob(String jobId) throws Exception
	{
		String sessionId = getThreadLocalRequest().getSession().getId();

		JobDAO jobDAO = new JobDAOJpa();
		StatusOfJob status = jobDAO.getStatusForJob(jobId);

		if((status.equals(StatusOfJob.RUNNING)) ||
				(status.equals(StatusOfJob.WAITING)) ||
				(status.equals(StatusOfJob.QUEUING)))
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

		// TODO it seems that destroy() doesn't get called upon jetty killing. So the app is not shutting down correctly

		logger.info("Destroying CrkWebService");

		jobStatusUpdater.setRunning(false);

		while(jobStatusUpdater.isUpdating())
		{

		}

		try
		{
			jobManager.close();
		}
		catch (JobHandlerException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public List<PDBSearchResult> getListOfPDBsHavingAUniProt(String uniProtId) throws Exception {

		ChainClusterDAO homologsDAO = new ChainClusterDAOJpa();
		List<PDBSearchResult> resultList = homologsDAO.getPdbSearchItemsForUniProt(uniProtId);

		List<PDBSearchResult> data = new ArrayList<PDBSearchResult>();

		if (resultList != null) {
			for(PDBSearchResult result: resultList){
				if(result.getPdbCode() != null){
					data.add(result);
				}
			}
		}

		return data;
	}

	@Override
	public List<PDBSearchResult> getListOfPDBs(String pdbCode, String chain) throws Exception {
		long start = System.currentTimeMillis();
		ChainClusterDAO homologsDAO = new ChainClusterDAOJpa();
		List<PDBSearchResult> data = new ArrayList<PDBSearchResult>();
		for(SequenceClusterType cl : SequenceClusterType.values()) {
			List<PDBSearchResult> resultList = homologsDAO.getPdbSearchItems(pdbCode, chain, cl);
			HashMap<Integer, PDBSearchResult> results = new HashMap<Integer, PDBSearchResult>();
			if (resultList != null) {
				for(PDBSearchResult pdbSearchResult: resultList)
					if(pdbSearchResult.getPdbCode() != null)
						results.put(pdbSearchResult.getUid(), pdbSearchResult);
				for(PDBSearchResult p : data)
					if(results.containsKey(p.getUid()))
						results.remove(p.getUid());
					else {
						p.setSuspicious(true);
						logger.warn("PDBSearchResult with uid " + p.getUid() + " and pdbCode " + p.getPdbCode() + " is not in the " + cl + " sequenceClusterType level but it's present in the stricter levels.");
					}
				for(PDBSearchResult p : results.values())
					data.add(p);
			}
		}
		long durration = System.currentTimeMillis() - start;
		logger.debug("Length of data: " + data.size() + " (fetching took " + durration + "ms)");
		return data;
	} 
}

