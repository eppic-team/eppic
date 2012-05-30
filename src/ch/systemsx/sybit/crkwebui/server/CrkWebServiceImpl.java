package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import ch.systemsx.sybit.crkwebui.server.db.model.HomologsInfoItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.HomologsInfoItemDAOImpl;
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
import ch.systemsx.sybit.crkwebui.server.generators.DirectoryContentGenerator;
import ch.systemsx.sybit.crkwebui.server.generators.RandomDirectoryNameGenerator;
import ch.systemsx.sybit.crkwebui.server.managers.JobManager;
import ch.systemsx.sybit.crkwebui.server.managers.JobManagerFactory;
import ch.systemsx.sybit.crkwebui.server.managers.JobStatusUpdater;
import ch.systemsx.sybit.crkwebui.server.parsers.InputParametersParser;
import ch.systemsx.sybit.crkwebui.server.util.IOUtil;
import ch.systemsx.sybit.crkwebui.server.util.LogHandler;
import ch.systemsx.sybit.crkwebui.server.validators.IPVerifier;
import ch.systemsx.sybit.crkwebui.server.validators.PreSubmitValidator;
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
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;
import ch.systemsx.sybit.crkwebui.shared.model.StepStatus;
import ch.systemsx.sybit.crkwebui.shared.validators.InputParametersComparator;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import crk.CRKParams;

/**
 * The server side implementation of the RPC service.
 *
 * @author srebniak_a
 */
@PersistenceContext(name="crkjpa", unitName="crkjpa")
@SuppressWarnings("serial")
public class CrkWebServiceImpl extends RemoteServiceServlet implements CrkWebService
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

		try
		{
			jobManager = JobManagerFactory.getJobManager("drmaa", generalDestinationDirectoryName);
		}
		catch(JobManagerException e)
		{
			throw new ServletException(e);
		}

//		String serverName =  getThreadLocalRequest().getServerName();
//		int serverPort = getThreadLocalRequest().getServerPort();
		
		String serverName =  getServletContext().getInitParameter("serverName");
		int serverPort = Integer.parseInt(getServletContext().getInitParameter("serverPort"));

		resultsPathUrl = protocol + "://" + serverName + ":" + serverPort + "/crkwebui/Crkwebui.html";

		EmailData emailData = new EmailData();
		emailData.setEmailSender(properties.getProperty("email_username", ""));
		emailData.setEmailSenderPassword(properties.getProperty("email_password", ""));
		emailData.setHost(properties.getProperty("email_host"));
		emailData.setPort(properties.getProperty("email_port"));
		emailSender = new EmailSender(emailData);

		crkRunner = new CrkRunner(jobManager, crkApplicationLocation);

		jobStatusUpdater = new JobStatusUpdater(jobManager,
				new JobDAOImpl(),
				resultsPathUrl,
				emailSender,
				generalDestinationDirectoryName);
		jobDaemon = new Thread(jobStatusUpdater);
		jobDaemon.start();
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
		ApplicationSettings settings = null;

		try
		{
			// default input parameters values
			InputStream inputParametersStream = getServletContext()
					.getResourceAsStream(
							"/WEB-INF/classes/META-INF/input_parameters.xml");

			settings = InputParametersParser.prepareApplicationSettings(inputParametersStream);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			throw new CrkWebException("Error during preparing input parameters");
		}

		InputStream propertiesStream = getServletContext()
		.getResourceAsStream(
				"/WEB-INF/classes/META-INF/grid.properties");

		Properties gridProperties = new Properties();

		try
		{
			gridProperties.load(propertiesStream);
		}
		catch(IOException e)
		{
			throw new CrkWebException(e);
		}

		Map<String, String> gridPropetiesMap = new HashMap<String, String>();
		for (Object key : gridProperties.keySet())
		{
			gridPropetiesMap.put((String) key, (String) gridProperties.get(key));
		}

		settings.setGridProperties(gridPropetiesMap);

		JobDAO jobDAO = new JobDAOImpl();
		int nrOfJobsForSession = jobDAO.getNrOfJobsForSessionId(getThreadLocalRequest().getSession().getId()).intValue();
//		int nrOfJobsForSession = DBUtils.getNrOfJobsForSessionId(getThreadLocalRequest().getSession().getId());
		settings.setNrOfJobsForSession(nrOfJobsForSession);

		boolean useCaptcha = Boolean.parseBoolean(properties.getProperty("use_captcha","false"));
		String captchaPublicKey = properties.getProperty("captcha_public_key");
		int nrOfAllowedSubmissionsWithoutCaptcha = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_without_captcha"));
		String pdbLinkUrl = properties.getProperty("pdb_link_url");
		String uniprotLinkUrl = properties.getProperty("uniprot_link_url");

		settings.setCaptchaPublicKey(captchaPublicKey);
		settings.setUseCaptcha(useCaptcha);
		settings.setNrOfAllowedSubmissionsWithoutCaptcha(nrOfAllowedSubmissionsWithoutCaptcha);
		settings.setPdbLinkUrl(pdbLinkUrl);
		settings.setUniprotLinkUrl(uniprotLinkUrl);

		settings.setResultsLocation(properties.getProperty("results_location"));

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

			String localDestinationDirName = generalDestinationDirectoryName + "/" + runJobData.getJobId();

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
				
				emailTitle = "EPPIC: " + runJobData.getInput() + " submitted";
				emailMessage = runJobData.getInput()
								 + " job submitted. To see the status of the processing please go to: "
								 + resultsPathUrl + "#id=" + runJobData.getJobId();
			}
			catch(Throwable e)
			{
				LogHandler.writeToLogFile(logFile, e.getMessage());

				submissionStatus = StatusOfJob.ERROR;

				emailTitle = runJobData.getInput() + " - error while submitting the job.\n\n";
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
			String dataDirectory = generalDestinationDirectoryName + "/" + jobId;

			if (IOUtil.checkIfDirectoryExist(dataDirectory))
			{
				statusData = new ProcessingInProgressData();

				statusData.setJobId(jobId);
				statusData.setStatus(status);
				statusData.setInput(jobDAO.getInputForJob(jobId));
				statusData.setStep(new StepStatus());

				try
				{
					List<File> filesToRead = new ArrayList<File>();

					if (IOUtil.checkIfFileExist(dataDirectory + "/crklog"))
					{
						filesToRead.add(new File(dataDirectory, "crklog"));
					}

//					if(debug)
//					{
						File directory = new File(dataDirectory);
						File[] directoryContent = DirectoryContentGenerator.getFilesNamesWithPrefix(directory, jobId + ".e");
						
						if(directoryContent != null)
						{
							for(File fileToInclude : directoryContent)
							{
								filesToRead.add(fileToInclude);
							}
						}
//					}


					StringBuffer log = new StringBuffer();

					for(File logFile : filesToRead)
					{
						FileReader inputStream = null;
				        BufferedReader bufferedInputStream = null;

				        try
				        {
				        	inputStream = new FileReader(logFile);
					        bufferedInputStream = new BufferedReader(inputStream);

					        String line = "";

					        while ((line = bufferedInputStream.readLine()) != null)
					        {
					        	log.append(line + "\n");
					        }
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

		String dataDirectory = generalDestinationDirectoryName + "/" + jobId;

		if (IOUtil.checkIfDirectoryExist(dataDirectory))
		{
			try
			{
				File stepFile = new File(dataDirectory, input + ".steps.log");

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
	public List<ProcessingInProgressData> getJobsForCurrentSession() throws Exception
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		JobDAO jobDAO = new JobDAOImpl();
		return jobDAO.getJobsForSession(sessionId);
	}

	@Override
	public String stopJob(String jobId) throws Exception
	{
		String result = null;

		try
		{
			JobDAO jobDAO = new JobDAOImpl();
			String submissionId = jobDAO.getSubmissionIdForJobId(jobId);
			
			jobManager.stopJob(submissionId);

			File jobDirectory = new File(generalDestinationDirectoryName, jobId);
			File killFile = new File(jobDirectory, "crkkilled");
			
			try
			{
				killFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			jobDAO.updateStatusOfJob(jobId, StatusOfJob.ERROR.getName());

			result = "Job: " + jobId + " was stopped";
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

		stopJob(jobId);

		JobDAO jobDAO = new JobDAOImpl();
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

//	public void setJobDao(JobDAO jobDao) {
//		this.jobDao = jobDao;
//	}
}
