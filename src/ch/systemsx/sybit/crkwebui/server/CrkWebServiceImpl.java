package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import model.InterfaceResidueItem;
import model.InterfaceResidueMethodItem;
import model.PDBScoreItem;
import model.PdbScore;
import model.ProcessingData;
import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import crk.InterfaceEvolContextList;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class CrkWebServiceImpl extends RemoteServiceServlet implements
		CrkWebService {

	// general server settings
	private Properties properties;

	private EmailData emailData;

	private String generalTmpDirectoryName;
	private String generalDestinationDirectoryName;

	private String dataSource;

	// list of running  threads
	private CrkThreadGroup runInstances;

	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);

		InputStream propertiesStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/server.properties");

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

		emailData = new EmailData();
		emailData.setEmailSender(properties.getProperty("email_username", ""));
		emailData.setEmailSenderPassword(properties.getProperty("email_password", ""));
		emailData.setHost(properties.getProperty("email_host"));
		emailData.setPort(properties.getProperty("email_port"));

		runInstances = new CrkThreadGroup("instances");
		getServletContext().setAttribute("instances", runInstances);

		dataSource = properties.getProperty("data_source");
		DBUtils.setDataSource(dataSource);
	}

	public String greetServer(String input) throws IllegalArgumentException {
		// // Verify that the input is valid.
		// if (!FieldVerifier.isValidName(input)) {
		// // If the input is not valid, throw an IllegalArgumentException back
		// to
		// // the client.
		// throw new IllegalArgumentException(
		// "Name must be at least 4 characters long");
		// }
		//
		// String serverInfo = getServletContext().getServerInfo();
		// String userAgent = getThreadLocalRequest().getHeader("User-Agent");
		//
		// // Escape data from the client to avoid cross-site script
		// vulnerabilities.
		// input = escapeHtml(input);
		// userAgent = escapeHtml(userAgent);
		//
		// return "Hello, " + input + "!<br><br>I am running " + serverInfo
		// + ".<br><br>It looks like you are using:<br>" + userAgent;
		return "";
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
	public ApplicationSettings getSettings() throws Exception 
	{
		ApplicationSettings settings = new ApplicationSettings();

		InputStream propertiesStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/grid.properties");

		Properties gridProperties = new Properties();
		
		try
		{
			gridProperties.load(propertiesStream);
		}
		catch(IOException e)
		{
			throw new Exception("Error during loading grid settings: " + e.getMessage());
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
			throw new Exception("Scoring methods not set");
		}

		// default input parameters values
		InputStream defaultInputParametersStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/input_default_parameters.properties");

		Properties defaultInputParametersProperties = new Properties();

		try 
		{
			defaultInputParametersProperties.load(defaultInputParametersStream);
		}
		catch (IOException e) 
		{
			throw new Exception("Error during reading default values of input parameters");
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

		return settings;
	}

	@Override
	public ProcessingData getResultsOfProcessing(String id) throws Exception 
	{
		String status = DBUtils.getStatusForJob(id);

		if(status.equals("Finished")) 
		{
			return getResultData(id);
		}
		else 
		{
			return getStatusData(id);
		}
	}

	private ProcessingInProgressData getStatusData(String id) throws Exception 
	{
		ProcessingInProgressData statusData = null;

		if((id != null) && (!id.equals("")))
		{
			String dataDirectory = generalDestinationDirectoryName + "/" + id;
	
			if (checkIfDirectoryExist(dataDirectory)) 
			{
				statusData = new ProcessingInProgressData();
	
				statusData.setJobId(id);
	
				statusData.setStatus(DBUtils.getStatusForJob(id));
	
				if (checkIfFileExist(dataDirectory + "/crklog")) 
				{
					try 
					{
						File logFile = new File(dataDirectory + "/crklog");
	
						FileInputStream inputStream = new FileInputStream(logFile);
						BufferedInputStream bufferedInputStream = new BufferedInputStream(
								inputStream);
	
						byte[] buffer = new byte[inputStream.available()];
	
						int length;
	
						StringBuffer log = new StringBuffer();
	
						while ((bufferedInputStream != null)
								&& ((length = bufferedInputStream.read(buffer)) != -1)) 
						{
							log.append(new String(buffer));
						}
	
						bufferedInputStream.close();
						inputStream.close();
	
						statusData.setLog(log.toString());
					} 
					catch (Exception e) 
					{
						throw new Exception("Error during connecting to log file: " + e.getMessage());
					}
				}
			}
		}

		return statusData;
	}
	
	private PDBScoreItem getResultData(String id) 
	{
		PDBScoreItem resultsData = null;

		if ((id != null) && (id.length() != 0)) 
		{
			File resultFileDirectory = new File(
					properties.getProperty("destination_path") + "/" + id);

			if (resultFileDirectory.exists()
					&& resultFileDirectory.isDirectory())
			{
				String[] directoryContent = resultFileDirectory
						.list(new FilenameFilter() {

							public boolean accept(File dir, String name) {
								if (name.endsWith(".scores")) {
									return true;
								} else {
									return false;
								}
							}
						});

				if (directoryContent != null && directoryContent.length > 0) 
				{
					PdbScore[] allPdbScores = null;

					List<PdbScore[]> pdbScores = new ArrayList<PdbScore[]>();

					for (int i = 0; i < directoryContent.length; i++)
					{
						File resultFile = new File(resultFileDirectory + "/"
								+ directoryContent[0]);

						if (resultFile.exists()) 
						{
							try {
								PdbScore[] pdbScoresForMethod = InterfaceEvolContextList
										.parseScoresFile(resultFile);
								pdbScores.add(pdbScoresForMethod);
							} catch (Exception e) {

							}
						}
					}

					if (pdbScores.size() > 0) 
					{
						allPdbScores = pdbScores.get(0);

						int totalLength = 0;

						for (PdbScore[] array : pdbScores) 
						{
							totalLength += array.length;
						}

						int offset = pdbScores.get(0).length;

						for (int i = 1; i < pdbScores.size(); i++) 
						{
							System.arraycopy(pdbScores.get(i), 0, allPdbScores,
									offset, pdbScores.get(i).length);
							offset += pdbScores.get(i).length;
						}
					}

					resultsData = PDBScoreItem.createPDBScoreItem(allPdbScores);

				}
			}
		}

		return resultsData;
	}
	
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(
			String jobId, int interfaceId) 
	{
		HashMap<Integer, List<InterfaceResidueItem>> structures = new HashMap<Integer, List<InterfaceResidueItem>>();

		for (int j = 1; j < 3; j++)
		{
			List<InterfaceResidueItem> residueItems = new ArrayList<InterfaceResidueItem>();

			for (int i = 0; i < 2; i++) 
			{
				InterfaceResidueItem residueItem = new InterfaceResidueItem();
				residueItem.setAsa(20);
				residueItem.setResidueType("ABC");

				Map<String, InterfaceResidueMethodItem> residueMethodItems = new HashMap<String, InterfaceResidueMethodItem>();

				InterfaceResidueMethodItem residueMethodItem = new InterfaceResidueMethodItem();
				residueMethodItem.setScore(30);

				residueMethodItems.put("Entropy", residueMethodItem);

				residueItem.setInterfaceResidueMethodItems(residueMethodItems);

				residueItems.add(residueItem);
			}

			structures.put(j, residueItems);
		}

		return structures;
	}
	
	@Override
	public void runJob(RunJobData runJobData) 
	{
		if (runJobData != null) 
		{
			emailData.setEmailRecipient(runJobData.getEmailAddress());

			String localDestinationDirName = generalDestinationDirectoryName + "/" + runJobData.getJobId();

			EmailSender emailSender = new EmailSender(emailData);

			DBUtils.insertNewJob(runJobData.getJobId(),
					getThreadLocalRequest().getSession().getId(),
					emailData.getEmailRecipient(), runJobData.getFileName());

			CrkRunner crkRunner = new CrkRunner(emailSender,
					runJobData.getFileName(), 
					"resultPath",
					localDestinationDirName, 
					runJobData.getJobId());

			Thread crkRunnerThread = new Thread(runInstances, 
					crkRunner,
					runJobData.getJobId());

			crkRunnerThread.start();
		}
	}

	@Override
	public String killJob(String id)
	{
		String result = null;

		Object runInstancesAttribute = getServletContext().getAttribute(
				"instances");

		if (runInstancesAttribute != null) {
			CrkThreadGroup runningInstances = (CrkThreadGroup) runInstancesAttribute;

			Thread[] activeInstances = new Thread[runningInstances
					.activeCount()];

			runningInstances.enumerate(activeInstances);

			if (activeInstances != null) {
				int i = 0;
				boolean wasFound = false;

				while ((i < activeInstances.length) && (!wasFound)) {
					if (activeInstances[i].getName().equals(id)) {
						activeInstances[i].interrupt();
						wasFound = true;
						result = "Job " + id + " stopped";

						File killFile = new File(
								generalDestinationDirectoryName + "/" + id
										+ "/crkkilled");
						try {
							killFile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}

						String errorDuringUpdaingInDB = DBUtils
								.updateStatusOfJob(id, "Stopped");
					}

					i++;
				}

				if (!wasFound) {
					result = "No job " + id + " or can not be stopped";
				}
			}
		}

		return result;
	}

	

	public String test(String test) {
		// StatusData[] statusDataArray = null;
		//
		// Connection connection;
		//
		// try
		// {
		// connection = DatabaseConnector.getConnection(dataSource);
		//
		// if(connection != null)
		// {
		// String sessionId = getThreadLocalRequest().getSession().getId();
		//
		// String query =
		// String.format("SELECT jobId, status FROM Jobs WHERE sessionId=\"%s\"",
		// sessionId);
		//
		// Statement statement = connection.createStatement();
		//
		// ResultSet results = null;
		//
		// if(statement != null)
		// {
		// results = statement.executeQuery(query);
		// }
		//
		// if(results != null)
		// {
		// int nrOfElements = results.getFetchSize();
		//
		// statusDataArray = new StatusData[nrOfElements];
		//
		// int i = 0;
		//
		// while(results.next())
		// {
		// StatusData statusDataItem = new StatusData();
		// statusDataItem.setJobId(results.getString(0));
		// statusDataItem.setStatus(results.getString(1));
		//
		// statusDataArray[i] = statusDataItem;
		// i++;
		// }
		// }
		//
		// }
		// }
		// catch (NamingException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// catch (SQLException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// return "";

		return getThreadLocalRequest().getSession().getId();
	}

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
	public List<ProcessingInProgressData> getJobsForCurrentSession() 
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		return DBUtils.getJobsForCurrentSession(sessionId);
	}

	@Override
	public String untieJobsFromSession() 
	{
		String sessionId = getThreadLocalRequest().getSession().getId();
		return DBUtils.untieJobsFromSession(sessionId);
	}
}
