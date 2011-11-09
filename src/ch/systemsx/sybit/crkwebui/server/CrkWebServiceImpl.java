package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import model.InterfaceScoreItemDB;
import model.PDBScoreItemDB;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import ch.systemsx.sybit.crkwebui.server.data.Step;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceItemDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceResidueItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.InterfaceResidueItemDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.NumHomologsStringsDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.NumHomologsStringsDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.PDBScoreDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.PDBScoreDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.UserSessionDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.UserSessionDAOImpl;
import ch.systemsx.sybit.crkwebui.server.util.IPVerifier;
import ch.systemsx.sybit.crkwebui.server.util.InputParametersParser;
import ch.systemsx.sybit.crkwebui.server.util.RandomDirectoryNameGenerator;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;
import ch.systemsx.sybit.crkwebui.shared.model.NumHomologsStringItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;
import ch.systemsx.sybit.crkwebui.shared.model.StepStatus;

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
	
	private String[] downloadFileZipExcludeSufixes;
	
	private HashMap<Integer, Step> steps;
	
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
		
		String downloadFileZipExcludeSufixesList = properties.getProperty("download_file_zip_exclude_sufixes", null);
		
		if(downloadFileZipExcludeSufixesList != null)
		{
			downloadFileZipExcludeSufixes = downloadFileZipExcludeSufixesList.split(",");
		}
		
		propertiesStream = getServletContext()
		.getResourceAsStream(
				"/WEB-INF/classes/META-INF/steps.properties");

		steps = new HashMap<Integer, Step>();
		Properties stepProperties = new Properties();
		
		try 
		{
			stepProperties.load(propertiesStream);
			prepareSteps(stepProperties);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			throw new ServletException("Steps properties file can not be read");
		}
		
//		dataSource = properties.getProperty("data_source");
//		DBUtils.setDataSource(dataSource);
//		
		sgeFactory = SessionFactory.getFactory();
		sgeSession = sgeFactory.getSession();
		try 
		{
			sgeSession.init("");
		} 
		catch (DrmaaException e) 
		{
			e.printStackTrace();
			throw new ServletException("Can not initialize sge session");
		}
		
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
		
//		EntityManager entityManager = null;
//		
//		try
//		{
////			RunParametersItem item = new RunParametersItem();
////			item.setUid(2);
////			item.setHomologsCutoff(125);
//			
////			ObjectInputStream in = new ObjectInputStream(new FileInputStream("c:/test1.crk"));
//			ObjectInputStream in = new ObjectInputStream(new FileInputStream("c:/files1/res2/1smt.webui.dat"));
////			ObjectInputStream in = new ObjectInputStream(new FileInputStream("c:/1ton.webui.dat"));
//			PDBScoreItemDB readitem = (PDBScoreItemDB)in.readObject();
////			System.out.println(readitem.getInterfaceItems().get(0).getInterfaceResidues().size());
//			
//			InterfaceScoreItemDB iitem = readitem.getInterfaceItems().get(0).getInterfaceScores().get(0);
//			
//			entityManager = EntityManagerHandler.getEntityManager();
//			entityManager.getTransaction().begin();
//			entityManager.persist(readitem);
//			entityManager.getTransaction().commit();
//			entityManager.close();
//		}
//		catch(Throwable e)
//		{
//			e.printStackTrace();
//		}
		
		
//		EntityManager entityManager = null;
//		PDBScoreItem result = null;
//		
//		try
//		{
//			entityManager = EntityManagerHandler.getEntityManager();
//			PDBScoreDAO pdbScoreDAO = new PDBScoreDAOImpl();
//			PDBScoreItem item = pdbScoreDAO.getPDBScore("test1");
//			System.out.println("NAME: " + item.getPdbName());
//			
////			List<NumHomologsStringItem> hom = pdbScoreDAO.get(item.getUid());
////			System.out.println("ASAC1: " + item.getInterfaceItems().get(0).getAsaC1());
////			System.out.println("NUMHOMOL: " + hom.get(1).getText());
//		}
//		catch(Throwable e)
//		{
//			e.printStackTrace();
//		}
//		finally
//		{
//			try
//			{
//				entityManager.close();
//			}
//			catch(Throwable t)
//			{
//				
//			}
//		}
	}

	private void prepareSteps(Properties stepProperties) 
	{
		int nrOfSteps=1;
		boolean nextStepFound = true;
		
		while((nrOfSteps < stepProperties.size() + 1) && (nextStepFound))
		{
			if(!stepProperties.containsKey("step" + nrOfSteps))
			{
				nextStepFound = false;
			}
			else
			{
				nrOfSteps++;
			}
		}
		
		for(int i=1; i<nrOfSteps; i++)
		{
			Step step = new Step();
			
			if(stepProperties.containsKey("step" + i + "_filename_suffix"))
			{
				step.setSuffix(stepProperties.getProperty("step" + i + "_filename_suffix"));
			}
			
			if(stepProperties.containsKey("step" + i + "_filename_prefix"))
			{
				step.setPrefix(stepProperties.getProperty("step" + i + "_filename_prefix"));
			}
			
			if(stepProperties.containsKey("step" + i + "_filename"))
			{
				step.setFileName(stepProperties.getProperty("step" + i + "_filename"));
			}
			
			step.setDescription(stepProperties.getProperty("step" + i));
			
			steps.put(i, step);
		}
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
					wasFileUploaded,
					downloadFileZipExcludeSufixes);

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

	public ProcessingData getResultsOfProcessing(String jobId, boolean debug) throws CrkWebException 
	{
		String status = null;
		
		JobDAO jobDAO = new JobDAOImpl();
		status = jobDAO.getStatusForJob(jobId);
		
		if(status != null)
		{
			UserSessionDAO sessionDAO = new UserSessionDAOImpl();
			sessionDAO.insertSessionForJob(getThreadLocalRequest().getSession().getId(), jobId);
		
//		String status = DBUtils.getStatusForJob(jobId, getThreadLocalRequest().getSession().getId());

			if(status.equals(StatusOfJob.FINISHED)) 
			{
				return getResultData(jobId);
			}
			else 
			{
				return getStatusData(jobId, status, debug);
			}
		}
		else
		{
			return null;
		}
	}

	private ProcessingInProgressData getStatusData(final String jobId, String status, boolean debug) throws CrkWebException 
	{
		JobDAO jobDAO = new JobDAOImpl();
			
		ProcessingInProgressData statusData = null;

		if((jobId != null) && (!jobId.equals("")))
		{
			String dataDirectory = generalDestinationDirectoryName + "/" + jobId;
	
			if (checkIfDirectoryExist(dataDirectory)) 
			{
				statusData = new ProcessingInProgressData();
	
				statusData.setJobId(jobId);
				statusData.setStatus(status);
				statusData.setInput(jobDAO.getInputForJob(jobId));
				statusData.setStep(new StepStatus());
	
				try 
				{
					List<File> filesToRead = new ArrayList<File>();
					
					if (checkIfFileExist(dataDirectory + "/crklog")) 
					{
						filesToRead.add(new File(dataDirectory + "/crklog"));
					}
					
					if(debug)
					{
						File directory = new File(dataDirectory);
						
						File[] directoryContent = null;
						
						directoryContent = directory.listFiles(new FilenameFilter() {
							
							@Override
							public boolean accept(File dir, String name) 
							{
								if(name.startsWith(jobId + ".e"))
								{
									return true;
								}
								else
								{
									return false;
								}
							}
						});
						
						if(directoryContent != null)
						{
							for(File fileToInclude : directoryContent)
							{
								filesToRead.add(fileToInclude);
							}
						}
					}
					

					StringBuffer log = new StringBuffer();
					
					for(File logFile : filesToRead)
					{
						FileReader inputStream = new FileReader(logFile);
				        BufferedReader bufferedInputStream = new BufferedReader(inputStream);
				        
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
				        catch(Throwable t)
				        {
				        	throw t;
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
			        
					statusData.setLog(log.toString());
					
					if(status.equals(StatusOfJob.RUNNING))
					{
						statusData.setStep(retrieveCurrentStep(jobId));
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
	
	private StepStatus retrieveCurrentStep(final String jobId)
	{
		StepStatus stepStatus = new StepStatus();
		stepStatus.setTotalNumberOfSteps(steps.size());
		stepStatus.setCurrentStepNumber(0);
		stepStatus.setCurrentStep("Waiting");
		
		boolean stepFound = false;
		int i = steps.size();
		
		String dataDirectory = generalDestinationDirectoryName + "/" + jobId;
		
		if (checkIfDirectoryExist(dataDirectory)) 
		{
			while((!stepFound) && (i > 0))
			{
				File directory = new File(dataDirectory);
				
				File[] directoryContent = null;
				
				final int stepNumber = i;
				
				directoryContent = directory.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) 
					{
						if((steps.get(stepNumber).getFileName() != null) &&
						   (!steps.get(stepNumber).getFileName().equals("")) &&
						   (name.toLowerCase().equals(steps.get(stepNumber).getFileName())))
					    {
							return true;
					    }
						else if((steps.get(stepNumber).getPrefix() != null) &&
						   (!steps.get(stepNumber).getPrefix().equals("")) &&
						   (steps.get(stepNumber).getSuffix() != null) &&
						   (!steps.get(stepNumber).getSuffix().equals("")) &&
						   (name.toLowerCase().startsWith(steps.get(stepNumber).getPrefix())) &&
						   (name.toLowerCase().endsWith(steps.get(stepNumber).getSuffix())))
						{
							return true;
						}
						else if((steps.get(stepNumber).getPrefix() != null) &&
							    (!steps.get(stepNumber).getPrefix().equals("")) &&
							    (name.toLowerCase().startsWith(steps.get(stepNumber).getPrefix())))
						{
							return true;
						}
						else if((steps.get(stepNumber).getSuffix() != null) &&
							    (!steps.get(stepNumber).getSuffix().equals("")) &&
							    (name.toLowerCase().endsWith(steps.get(stepNumber).getSuffix())))
						{
							return true;
						}
						else
						{
							return false;
						}
					}
				});
				
				if((directoryContent != null) && (directoryContent.length > 0))
				{
					stepFound = true;
					
					if(i < steps.size())
					{
						i++;
					}
					
					stepStatus.setCurrentStepNumber(i);
					stepStatus.setCurrentStep(steps.get(i).getDescription());
				}
				else
				{
					i--;
				}
			}
		}
		
		return stepStatus;
	}
	
//	private PDBScoreItem getResultData(String jobId) throws CrkWebException 
//	{
//		PDBScoreItem resultsData = null;
//
//		if ((jobId != null) && (jobId.length() != 0)) 
//		{
//			File resultFileDirectory = new File(
//					generalDestinationDirectoryName + "/" + jobId);
//
//			if (resultFileDirectory.exists()
//					&& resultFileDirectory.isDirectory())
//			{
//				String[] directoryContent = resultFileDirectory
//						.list(new FilenameFilter() {
//
//							public boolean accept(File dir, String name) {
//								if (name.endsWith(".webui.dat")) {
//									return true;
//								} else {
//									return false;
//								}
//							}
//						});
//
//				if ((directoryContent != null) && (directoryContent.length > 0)) 
//				{
//					File resultFile = new File(resultFileDirectory + "/" + directoryContent[0]);
//					
//					if (resultFile.exists()) 
//					{
//						FileInputStream fileInputStream = null;
//						ObjectInputStream inputStream = null;
//						
//						try 
//						{
//							fileInputStream = new FileInputStream(resultFile);
//							inputStream = new ObjectInputStream(fileInputStream);
//							resultsData = (PDBScoreItem)inputStream.readObject();
//							resultsData.setJobId(jobId);
//						} 
//						catch (Throwable e)
//						{
//							throw new CrkWebException(e);
//						}
//						finally
//						{
//							if(inputStream != null)
//							{
//								try
//								{
//									inputStream.close();
//								}
//								catch(Throwable t)
//								{
//									t.printStackTrace();
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		return resultsData;
//	}
	
	private PDBScoreItem getResultData(String jobId) throws CrkWebException 
	{
		PDBScoreDAO pdbScoreDAO = new PDBScoreDAOImpl();
		PDBScoreItem pdbScoreItem = pdbScoreDAO.getPDBScore(jobId);
		
		InterfaceItemDAO interfaceItemDAO = new InterfaceItemDAOImpl();
		List<InterfaceItem> interfaceItems = interfaceItemDAO.getInterfacesWithScores(pdbScoreItem.getUid());
		pdbScoreItem.setInterfaceItems(interfaceItems);
		
		NumHomologsStringsDAO numHomologsStringsDAO = new NumHomologsStringsDAOImpl();
		List<NumHomologsStringItem> numHomologsStringItems = numHomologsStringsDAO.getNumHomologsStrings(pdbScoreItem.getUid());
		pdbScoreItem.setNumHomologsStrings(numHomologsStringItems);
//		
		return pdbScoreItem;
	}
	
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(int interfaceUid) throws CrkWebException
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
	public List<ProcessingInProgressData> getJobsForCurrentSession() throws CrkWebException 
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		JobDAO jobDAO = new JobDAOImpl();
		return jobDAO.getJobsForSession(sessionId);
//		return DBUtils.getJobsForCurrentSession(sessionId);
	}
	
	@Override
	public String stopJob(String jobToStop) throws CrkWebException 
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
				if ((activeInstances[i] != null) && (activeInstances[i].getName().equals(jobToStop))) 
				{
					if(!activeInstances[i].isInterrupted())
					{
						((CrkThread)activeInstances[i]).interrupt();
					}
					
					wasFound = true;
					
					File killFile = new File(
							generalDestinationDirectoryName + "/" + jobToStop
									+ "/crkkilled");
					try 
					{
						killFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}

					JobDAO jobDAO = new JobDAOImpl();
					jobDAO.updateStatusOfJob(jobToStop, StatusOfJob.STOPPED);
	//					DBUtils.updateStatusOfJob(jobId, StatusOfJob.STOPPED);
					
					result = "Job: " + jobToStop + " was stopped";
				}

				i++;
			}

			if (!wasFound) 
			{
				result = "Job: " + jobToStop + " was not stopped";
			}
		}
		
		return result;
	}
	
	public String deleteJob(String jobToDelete) throws CrkWebException
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		JobDAO jobDAO = new JobDAOImpl();
		jobDAO.untieSelectedJobFromSession(sessionId, jobToDelete);
		
		String result = "Job: " + jobToDelete + " was removed";
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
				catch(CrkWebException e) 
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

	public InterfaceResiduesItemsList getAllResidues(int pdbScoreId) throws CrkWebException 
	{
		InterfaceResidueItemDAO interfaceResidueItemDAO = new InterfaceResidueItemDAOImpl();
		InterfaceResiduesItemsList interfaceResiduesItemsList = interfaceResidueItemDAO.getResiduesForAllInterfaces(pdbScoreId);
		return interfaceResiduesItemsList;
	}
}
