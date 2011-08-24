package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import model.InterfaceResidueItem;
import model.PDBScoreItem;
import model.ProcessingData;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAOImpl;
import ch.systemsx.sybit.crkwebui.server.util.IPVerifier;
import ch.systemsx.sybit.crkwebui.server.util.RandomDirectoryNameGenerator;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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

//	private String dataSource;

	// list of running  threads
	private CrkThreadGroup runInstances;
	
	private String crkApplicationLocation;
	
	private SessionFactory sgeFactory;
	private Session sgeSession;
	
	private String protocol = "http";
	
	private boolean doIPBasedVerification;
	private int defaultNrOfAllowedSubmissionsForIP;

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

		runInstances = new CrkThreadGroup("instances");
		getServletContext().setAttribute("instances", runInstances);
		
		if(properties.getProperty("protocol") != null)
		{
			protocol = properties.getProperty("protocol");
		}
		
		doIPBasedVerification = Boolean.parseBoolean(properties.getProperty("limit_access_by_ip","false"));
		defaultNrOfAllowedSubmissionsForIP = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_for_ip","100"));
		
//		dataSource = properties.getProperty("data_source");
//		DBUtils.setDataSource(dataSource);
		
//		sgeFactory = SessionFactory.getFactory();
//		sgeSession = sgeFactory.getSession();
//		try 
//		{
//			sgeSession.init("");
//		} 
//		catch (DrmaaException e) 
//		{
//			e.printStackTrace();
//		}
		
//		**********************
//		* Hibernate pure
//		**********************
//		try {
//			org.hibernate.classic.Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();
//			
//            // Begin unit of work
//			hibernateSession.beginTransaction();
//			
//			Job job = (Job)hibernateSession.get(Job.class, "1");
//			System.out.println(job.getJobId());
//            
//            // Process request and render page...
//
//            // End unit of work
//			hibernateSession.getTransaction().commit();
//        }
//        catch (Exception ex) {
//            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
//            if ( ServletException.class.isInstance( ex ) ) {
////                throw ( ServletException ) ex;
//            }
//            else {
////                throw new ServletException( ex );
//            }
//        }
		
//		***********************
//		* Hibernate JPA
//		***********************
		
	}

//	public String greetServer(String input) throws IllegalArgumentException {
//		// // Verify that the input is valid.
//		// if (!FieldVerifier.isValidName(input)) {
//		// // If the input is not valid, throw an IllegalArgumentException back
//		// to
//		// // the client.
//		// throw new IllegalArgumentException(
//		// "Name must be at least 4 characters long");
//		// }
//		//
//		// String serverInfo = getServletContext().getServerInfo();
//		// String userAgent = getThreadLocalRequest().getHeader("User-Agent");
//		//
//		// // Escape data from the client to avoid cross-site script
//		// vulnerabilities.
//		// input = escapeHtml(input);
//		// userAgent = escapeHtml(userAgent);
//		//
//		// return "Hello, " + input + "!<br><br>I am running " + serverInfo
//		// + ".<br><br>It looks like you are using:<br>" + userAgent;
//		return "";
//	}

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
	public ApplicationSettings loadSettings() throws CrkWebException 
	{
		ApplicationSettings settings = new ApplicationSettings();

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

		String supportedMethods = gridProperties.getProperty("supported_methods");

		if (supportedMethods != null) 
		{
			String[] scoringMethods = supportedMethods.split(",");
			settings.setScoresTypes(scoringMethods);
		}
		else
		{
			throw new CrkWebException("Scoring methods not set");
		}

		// default input parameters values
		InputStream defaultInputParametersStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/META-INF/input_default_parameters.properties");

		Properties defaultInputParametersProperties = new Properties();

		try 
		{
			defaultInputParametersProperties.load(defaultInputParametersStream);
		}
		catch (IOException e) 
		{
			throw new CrkWebException("Error during reading default values of input parameters");
		}

		InputParameters defaultInputParameters = new InputParameters();

		boolean useTcoffee = Boolean
				.parseBoolean((String) defaultInputParametersProperties
						.get("use_tcoffee"));
		boolean usePisa = Boolean
				.parseBoolean((String) defaultInputParametersProperties
						.get("use_pisa"));
		boolean useNaccess = Boolean
				.parseBoolean((String) defaultInputParametersProperties
						.get("use_naccess"));

		int asaCalc = Integer
				.parseInt((String) defaultInputParametersProperties
						.get("asa_calc"));
		int maxNrOfSequences = Integer
				.parseInt((String) defaultInputParametersProperties
						.get("max_nr_of_sequences"));
		int reducedAlphabet = Integer
				.parseInt((String) defaultInputParametersProperties
						.get("reduced_alphabet"));

		float identityCutoff = Float
				.parseFloat((String) defaultInputParametersProperties
						.get("identity_cutoff"));
		float selecton = Float
				.parseFloat((String) defaultInputParametersProperties
						.get("selecton"));
		
		String defaultMethodsList = defaultInputParametersProperties
			.getProperty("methods");
		String[] defaultMethodsValues = defaultMethodsList.split(",");
		
		defaultInputParameters.setMethods(defaultMethodsValues);

		defaultInputParameters.setUseTCoffee(useTcoffee);
		defaultInputParameters.setUsePISA(usePisa);
		defaultInputParameters.setUseNACCESS(useNaccess);
		defaultInputParameters.setAsaCalc(asaCalc);
		defaultInputParameters.setMaxNrOfSequences(maxNrOfSequences);
		defaultInputParameters.setReducedAlphabet(reducedAlphabet);
		defaultInputParameters.setIdentityCutoff(identityCutoff);
		defaultInputParameters.setSelecton(selecton);

		settings.setDefaultParametersValues(defaultInputParameters);

		String reducedAlphabetList = defaultInputParametersProperties
				.getProperty("reduced_alphabet_list");
		
		if(reducedAlphabetList != null)
		{
			String[] reducedAlphabetValues = reducedAlphabetList.split(",");
	
			List<Integer> reducedAlphabetConverted = new ArrayList<Integer>();
			for (String value : reducedAlphabetValues) 
			{
				reducedAlphabetConverted.add(Integer.parseInt(value));
			}
	
			settings.setReducedAlphabetList(reducedAlphabetConverted);
		}
		
		JobDAO jobDAO = new JobDAOImpl();
		int nrOfJobsForSession = jobDAO.getNrOfJobsForSessionId(getThreadLocalRequest().getSession().getId()).intValue();
//		int nrOfJobsForSession = DBUtils.getNrOfJobsForSessionId(getThreadLocalRequest().getSession().getId());
		settings.setNrOfJobsForSession(nrOfJobsForSession);
		
		boolean useCaptcha = Boolean.parseBoolean(properties.getProperty("use_captcha"));
		String captchaPublicKey = properties.getProperty("captcha_public_key");
		int nrOfAllowedSubmissionsWithoutCaptcha = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_without_captcha"));
		
		settings.setCaptchaPublicKey(captchaPublicKey);
		settings.setUseCaptcha(useCaptcha);
		settings.setNrOfAllowedSubmissionsWithoutCaptcha(nrOfAllowedSubmissionsWithoutCaptcha);
		
		settings.setResultsLocation(properties.getProperty("results_location"));
		
		settings.setJmolScript(properties.getProperty("jmol_script",""));
		
		return settings;
	}
	
	@Override
	public String runJob(RunJobData runJobData) throws CrkWebException 
	{
		if (runJobData != null) 
		{
			boolean wasFileUploaded = true;
			
			if(runJobData.getJobId() == null)
			{
				String randomDirectoryName = RandomDirectoryNameGenerator.generateRandomDirectoryName(generalDestinationDirectoryName);

				String localDestinationDirName = generalDestinationDirectoryName
						+ "/" + randomDirectoryName;
				File localDestinationDir = new File(localDestinationDirName);
				localDestinationDir.mkdir();
				
				runJobData.setJobId(randomDirectoryName);
				
				wasFileUploaded = false;
			}
			
			if(doIPBasedVerification)
			{
				String verificationError = IPVerifier.checkIfCanBeSubmitted(getThreadLocalRequest().getRemoteAddr(), 
																			defaultNrOfAllowedSubmissionsForIP);
				
				if(verificationError !=  null)
				{
					throw new CrkWebException(verificationError);
				}
			}
			
			EmailData emailData = new EmailData();
			emailData.setEmailSender(properties.getProperty("email_username", ""));
			emailData.setEmailSenderPassword(properties.getProperty("email_password", ""));
			emailData.setHost(properties.getProperty("email_host"));
			emailData.setPort(properties.getProperty("email_port"));
			emailData.setEmailRecipient(runJobData.getEmailAddress());

			String localDestinationDirName = generalDestinationDirectoryName + "/" + runJobData.getJobId();

			EmailSender emailSender = new EmailSender(emailData);

			Date currentDate = new Date();
			
			JobDAO jobDAO = new JobDAOImpl();
			jobDAO.insertNewJob(runJobData.getJobId(),
								getThreadLocalRequest().getSession().getId(),
								emailData.getEmailRecipient(), 
								runJobData.getInput(),
								getThreadLocalRequest().getRemoteAddr(),
								currentDate);
//			DBUtils.insertNewJob(runJobData.getJobId(),
//								 getThreadLocalRequest().getSession().getId(),
//								 emailData.getEmailRecipient(), 
//								 runJobData.getInput(),
//								 getThreadLocalRequest().getRemoteAddr());

			String serverName = getThreadLocalRequest().getServerName();
			int serverPort = getThreadLocalRequest().getServerPort();
			
			String resultsLocation = protocol + "://" + serverName + ":" + serverPort + "/crkwebui/Crkwebui.html";
			
			CrkRunner crkRunner = new CrkRunner(emailSender,
					runJobData.getInput(), 
					resultsLocation + "#id=" + runJobData.getJobId(),
					localDestinationDirName, 
					runJobData.getJobId(),
					runJobData.getInputParameters(),
					crkApplicationLocation,
					sgeSession,
					wasFileUploaded);

			CrkThread crkRunnerThread = new CrkThread(runInstances, 
					crkRunner,
					runJobData.getJobId());

			File logFile = new File(localDestinationDirName + "/crklog");
			
			try 
			{
				logFile.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
				
			crkRunnerThread.start();
			
			return runJobData.getJobId();
		}
		
		return null;
	}

	public ProcessingData getResultsOfProcessing(String jobId) throws CrkWebException 
	{
		JobDAO jobDAO = new JobDAOImpl();
		
		String status = null;
		
		try
		{
			status = jobDAO.getStatusForJob(jobId);
		}
		catch(PersistenceException e)
		{
			
		}
		
		if(status == null)
		{
			status = StatusOfJob.NONEXISTING;
		}
		else
		{
			jobDAO.updateSessionIdForSelectedJob(getThreadLocalRequest().getSession().getId(), jobId);
		}
		
//		String status = DBUtils.getStatusForJob(jobId, getThreadLocalRequest().getSession().getId());

		if(status != null)
		{
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

	private ProcessingInProgressData getStatusData(String jobId, String status) throws CrkWebException 
	{
		ProcessingInProgressData statusData = null;

		if((jobId != null) && (!jobId.equals("")))
		{
			String dataDirectory = generalDestinationDirectoryName + "/" + jobId;
	
			if (checkIfDirectoryExist(dataDirectory)) 
			{
				statusData = new ProcessingInProgressData();
	
				statusData.setJobId(jobId);
	
				statusData.setStatus(status);
	
				if (checkIfFileExist(dataDirectory + "/crklog")) 
				{
					try 
					{
						File logFile = new File(dataDirectory + "/crklog");
	
						StringBuffer log = new StringBuffer();
						
						FileReader inputStream = new FileReader(logFile);
				        BufferedReader bufferedInputStream = new BufferedReader(inputStream);
				        
				        String line = "";
				        
				        while ((line = bufferedInputStream.readLine()) != null)
				        {
				        	log.append(line + "\n");
				        }

				        bufferedInputStream.close();
				        inputStream.close();
				        
						statusData.setLog(log.toString());
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
						throw new CrkWebException(e);
					}
				}
			}
		}

		return statusData;
	}
	
	private PDBScoreItem getResultData(String jobId) throws CrkWebException 
	{
		PDBScoreItem resultsData = null;

		if ((jobId != null) && (jobId.length() != 0)) 
		{
			File resultFileDirectory = new File(
					generalDestinationDirectoryName + "/" + jobId);

			if (resultFileDirectory.exists()
					&& resultFileDirectory.isDirectory())
			{
				String[] directoryContent = resultFileDirectory
						.list(new FilenameFilter() {

							public boolean accept(File dir, String name) {
								if (name.endsWith(".webui.dat")) {
									return true;
								} else {
									return false;
								}
							}
						});

				if (directoryContent != null && directoryContent.length > 0) 
				{
					File resultFile = new File(resultFileDirectory + "/" + directoryContent[0]);
					
					if (resultFile.exists()) 
					{
						try 
						{
							FileInputStream fileInputStream = new FileInputStream(resultFile);
							ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
							resultsData = (PDBScoreItem)inputStream.readObject();
							resultsData.setJobId(jobId);
							inputStream.close();
							fileInputStream.close();
						} 
						catch (Throwable e)
						{
							throw new CrkWebException(e);
						}
					}
				}
			}
		}

		return resultsData;
	}
	
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(
			String jobId, final int interfaceId) throws CrkWebException
	{
		HashMap<Integer, List<InterfaceResidueItem>> structures = null;
		
		if ((jobId != null) && (jobId.length() != 0)) 
		{
			File resultFileDirectory = new File(
					generalDestinationDirectoryName + "/" + jobId);
			
			String[] directoryContent = resultFileDirectory.list(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.endsWith("." + interfaceId + ".resDetails.dat")) {
						return true;
					} else {
						return false;
					}
				}
			});

			if (directoryContent != null && directoryContent.length > 0)
			{
				try
				{
					FileInputStream file = new FileInputStream(generalDestinationDirectoryName + "/" + jobId + "/" + directoryContent[0]);
					ObjectInputStream in = new ObjectInputStream(file);
					Object object = in.readObject();
					structures = (HashMap<Integer, List<InterfaceResidueItem>>) object;
					in.close();
					file.close();
				}
				catch(Throwable e)
				{
					throw new CrkWebException(e);
				}
			}
		}
		
		return structures;
	}
	
	@Override
	public List<ProcessingInProgressData> getJobsForCurrentSession() throws CrkWebException 
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		JobDAO jobDAO = new JobDAOImpl();
		return jobDAO.getJobsForSession(sessionId);
//		return DBUtils.getJobsForCurrentSession(sessionId);
	}
	
	@Override
	public String killJob(String jobId) throws CrkWebException 
	{
		String result = null;

		int estimatedNrOfCurrentThreads = runInstances.activeCount();
		Thread[] activeInstances = new Thread[estimatedNrOfCurrentThreads];

		int nrOfCurrentThreads = runInstances.enumerate(activeInstances);
		
		while(nrOfCurrentThreads > estimatedNrOfCurrentThreads)
		{
			estimatedNrOfCurrentThreads = nrOfCurrentThreads;
			activeInstances = new Thread[nrOfCurrentThreads];
			nrOfCurrentThreads = runInstances.enumerate(activeInstances);
		}

		if (activeInstances != null)
		{
			int i = 0;
			boolean wasFound = false;

			while ((i < activeInstances.length) && (!wasFound)) 
			{
				if ((activeInstances[i] != null) && (activeInstances[i].getName().equals(jobId))) 
				{
					if(!activeInstances[i].isInterrupted())
					{
						((CrkThread)activeInstances[i]).interrupt();
					}
					
					wasFound = true;
					result = "Job " + jobId + " stopped";

					File killFile = new File(
							generalDestinationDirectoryName + "/" + jobId
									+ "/crkkilled");
					try 
					{
						killFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}

					JobDAO jobDAO = new JobDAOImpl();
					jobDAO.updateStatusOfJob(jobId, StatusOfJob.STOPPED);
//					DBUtils.updateStatusOfJob(jobId, StatusOfJob.STOPPED);
				}

				i++;
			}

			if (!wasFound) 
			{
				result = "No job " + jobId + " or can not be stopped";
			}
		}

		return result;
	}
	
	@Override
	public void untieJobsFromSession() throws CrkWebException 
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		JobDAO jobDAO = new JobDAOImpl();
		jobDAO.untieJobsFromSession(sessionId);
//		DBUtils.untieJobsFromSession(sessionId);
	}

	

//	public String test(String test) {
//		return getThreadLocalRequest().getSession().getId();
//	}

	private boolean checkIfDirectoryExist(String directoryName) {
		File directory = new File(directoryName);

		if (directory.exists() && directory.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkIfFileExist(String fileName) {
		File file = new File(fileName);

		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		
		int estimatedNrOfCurrentThreads = runInstances.activeCount();
		Thread[] activeInstances = new Thread[estimatedNrOfCurrentThreads];

		int nrOfCurrentThreads = runInstances.enumerate(activeInstances);
		
		while(nrOfCurrentThreads > estimatedNrOfCurrentThreads)
		{
			estimatedNrOfCurrentThreads = nrOfCurrentThreads;
			activeInstances = new Thread[nrOfCurrentThreads];
			nrOfCurrentThreads = runInstances.enumerate(activeInstances);
		}
		
		for(Thread activeThread : activeInstances)
		{
			if((activeThread != null) && (!activeThread.isInterrupted()))
			{
				try 
				{
					JobDAO jobDAO = new JobDAOImpl();
					jobDAO.updateStatusOfJob(activeThread.getName(), StatusOfJob.STOPPED);
//					DBUtils.updateStatusOfJob(activeThread.getName(), "Stopped");
				}
				catch (CrkWebException e) 
				{
					e.printStackTrace();
				}
				
				activeThread.interrupt();
			}
		}
		
		try 
		{
			sgeSession.exit();
		} 
		catch (DrmaaException e) 
		{
			e.printStackTrace();
		}
		
//		runInstances.destroy();
	}
}
