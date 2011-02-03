package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import owl.core.util.RegexFileFilter;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.client.data.InterfaceScore;
import ch.systemsx.sybit.crkwebui.client.data.ResultsData;
import ch.systemsx.sybit.crkwebui.client.data.StatusData;
import ch.systemsx.sybit.crkwebui.shared.FieldVerifier;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import crk.InterfaceEvolContextList;
import model.PdbScore;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class CrkWebServiceImpl extends RemoteServiceServlet implements
		CrkWebService {

	private Properties properties;
	
	private String generalTmpDirectoryName;
	private String generalDestinationDirectoryName;
	
	private String dataSource;
	
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		
		InputStream propertiesStream = 
			getServletContext().getResourceAsStream("/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/server.properties");
		
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
		
		if(!tmpDir.isDirectory()) 
		{
			throw new ServletException(generalTmpDirectoryName + " is not a directory");
		}
		
//		String realPath = getServletContext().getRealPath(properties.getProperty("destination_path"));
		generalDestinationDirectoryName = properties.getProperty("destination_path");
		File destinationDir = new File(generalDestinationDirectoryName);
		if(!destinationDir.isDirectory()) 
		{
			throw new ServletException(generalDestinationDirectoryName + " is not a directory");
		}
		
		dataSource = properties.getProperty("data_source");
		DBUtils.setDataSource(dataSource);
	}
	
	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

// version for files
//	@Override
//	public boolean checkIfDataProcessed(String id)
//	{
//		if((checkIfFileExist(generalDestinationDirectoryName + "/" + id + "/crkerr")) || 
//		   (!checkIfFileExist(generalDestinationDirectoryName + "/" + id + "/crkrun")))
//		{
//			return true;
//		}	
//		else
//		{
//			return false;
//		}
//	}
	
	@Override
	public boolean checkIfDataProcessed(String id)
	{
		String status = DBUtils.getStatusForJob(id);
		
		if(status.equals("Finished"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public StatusData getStatusData(String id)
	{
		StatusData statusData = null;
		
		String dataDirectory = generalDestinationDirectoryName + "/" + id;
		
		if(checkIfDirectoryExist(dataDirectory))
		{
			statusData = new StatusData();
			
			statusData.setJobId(id);
			
//			if(checkIfFileExist(dataDirectory + "/crkerr"))
//			{
//				statusData.setStatus("Error");
//			}
//			else if(checkIfFileExist(dataDirectory + "/crkkilled"))
//			{
//				statusData.setStatus("Stopped");
//			}
//			else if(checkIfFileExist(dataDirectory + "/crkrun"))
//			{
//				statusData.setStatus("InProgress");
//			}
			
			statusData.setStatus(DBUtils.getStatusForJob(id));
			
			if(checkIfFileExist(dataDirectory + "/crklog"))
			{
				try
				{
					File logFile = new File(dataDirectory + "/crklog");
					
					FileInputStream inputStream = new FileInputStream(logFile);
					BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
					
					byte[] buffer = new byte[inputStream.available()];
					
		            int length;
		            
		            StringBuffer log = new StringBuffer();
		            
		            while ((bufferedInputStream != null) && ((length = bufferedInputStream.read(buffer)) != -1)) 
		            {
		            	log.append(new String(buffer));
		            }
		
		            bufferedInputStream.close();
		            inputStream.close();
	
					statusData.setLog(log.toString());
				}
				catch(Exception e)
				{
					
				}
			}
		}
		
		return statusData;
	}
	
	// version for files
//	@Override
//	public StatusData getStatusData(String id)
//	{
//		StatusData statusData = null;
//		
//		String dataDirectory = generalDestinationDirectoryName + "/" + id;
//		
//		if(checkIfDirectoryExist(dataDirectory))
//		{
//			statusData = new StatusData();
//			
//			statusData.setJobId(id);
//			
//			if(checkIfFileExist(dataDirectory + "/crkerr"))
//			{
//				statusData.setStatus("Error");
//			}
//			else if(checkIfFileExist(dataDirectory + "/crkkilled"))
//			{
//				statusData.setStatus("Stopped");
//			}
//			else if(checkIfFileExist(dataDirectory + "/crkrun"))
//			{
//				statusData.setStatus("InProgress");
//			}
//			
//			if(checkIfFileExist(dataDirectory + "/crklog"))
//			{
//				try
//				{
//					File logFile = new File(dataDirectory + "/crklog");
//					
//					FileInputStream inputStream = new FileInputStream(logFile);
//					BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//					
//					byte[] buffer = new byte[inputStream.available()];
//					
//		            int length;
//		            
//		            StringBuffer log = new StringBuffer();
//		            
//		            while ((bufferedInputStream != null) && ((length = bufferedInputStream.read(buffer)) != -1)) 
//		            {
//		            	log.append(new String(buffer));
//		            }
//		
//		            bufferedInputStream.close();
//		            inputStream.close();
//	
//					statusData.setLog(log.toString());
//				}
//				catch(Exception e)
//				{
//					
//				}
//			}
//		}
//		
//		return statusData;
//	}
//	
	@Override
	public String killJob(String id)
	{
		String result = null;
		
		Object runInstancesAttribute =  getServletContext().getAttribute("instances");
		
		if(runInstancesAttribute != null)
		{
			CrkThreadGroup runningInstances = (CrkThreadGroup)runInstancesAttribute;
			
			Thread[] activeInstances = new Thread[runningInstances.activeCount()]; 
			
			runningInstances.enumerate(activeInstances);
			
			if(activeInstances != null)
			{
				int i=0;
				boolean wasFound = false;
				
				while((i < activeInstances.length) && (!wasFound))
				{
					if(activeInstances[i].getName().equals(id))
					{
						activeInstances[i].interrupt();
						wasFound = true;
						result = "Job " + id + " stopped";
						
						File killFile = new File(generalDestinationDirectoryName + "/" + id + "/crkkilled");
						try 
						{
							killFile.createNewFile();
						}
						catch (IOException e) 
						{
							e.printStackTrace();
						}
						
						String errorDuringUpdaingInDB = DBUtils.updateStatusOfJob(id, "Stopped");
					}
					
					i++;
				}
				
				if(!wasFound)
				{
					result = "No job " + id + " or can not be stopped";
				}
			}
		}
		
		return result;
	}

	@Override
	public ResultsData getResultData(String id) 
	{
		ResultsData resultsData = null;
		
		if((id != null) && (id.length() != 0))
		{
			File resultFileDirectory = new File(properties.getProperty("destination_path") + "/" + id);
			
			if(resultFileDirectory.exists() &&
				resultFileDirectory.isDirectory())
			{
				String[] directoryContent = resultFileDirectory.list(new FilenameFilter() {
					
					public boolean accept(File dir, String name) 
					{
						if(name.endsWith(".scores"))
						{
							return true;
						}
						else
						{
							return false;
						}
					}
				});
				
				if(directoryContent != null &&
					directoryContent.length > 0)
				{
					File resultFile = new File(resultFileDirectory + "/" + directoryContent[0]);
					
					if(resultFile.exists())
					{
						try
						{
							model.PdbScore[] pdbScores = InterfaceEvolContextList.parseScoresFile(resultFile);
							
							if((pdbScores != null) && (pdbScores.length > 1))
							{
								resultsData = new ResultsData();
								resultsData.setPdbName(pdbScores[0].getPdbName());
								resultsData.setHomologsCutoff(pdbScores[0].getHomologsCutoff());
								resultsData.setQueryCovCutoff(pdbScores[0].getQueryCovCutoff());
								resultsData.setBioCutoff(pdbScores[0].getBioCutoff());
								resultsData.setXtalCutoff(pdbScores[0].getXtalCutoff());
								
								int nrOfInterface = pdbScores[0].getInterfaceScoreMap().size();
								
								int i = 1;
								
//								resultsData.setMinCoreSize(nrOfInterface);
								
								while(i <= nrOfInterface)
								{
									InterfaceScore interfaceScore = new InterfaceScore();
									interfaceScore.setId(i);
									interfaceScore.setInterfArea(pdbScores[0].getInterfaceScoreMap().get(i).getInterfArea());
									interfaceScore.setFirstChainId(pdbScores[0].getInterfaceScoreMap().get(i).getFirstChainId());
									interfaceScore.setSecondChainId(pdbScores[0].getInterfaceScoreMap().get(i).getSecondChainId());
									interfaceScore.setCoreSize1(pdbScores[0].getInterfaceScoreMap().get(i).getCoreSize1()[0]);
									interfaceScore.setCoreSize2(pdbScores[0].getInterfaceScoreMap().get(i).getCoreSize2()[0]);
									interfaceScore.setNumHomologs1(pdbScores[0].getInterfaceScoreMap().get(i).getNumHomologs1());
									interfaceScore.setNumHomologs2(pdbScores[0].getInterfaceScoreMap().get(i).getNumHomologs2());
									
									double[] rim1SCores =  new double[2];
									rim1SCores[0] = pdbScores[0].getInterfaceScoreMap().get(i).getRim1Scores()[0];
									rim1SCores[1] = pdbScores[1].getInterfaceScoreMap().get(i).getRim1Scores()[0];
									interfaceScore.setRim1Scores(rim1SCores);
									
									double[] core1SCores =  new double[2];
									core1SCores[0] = pdbScores[0].getInterfaceScoreMap().get(i).getCore1Scores()[0];
									core1SCores[1] = pdbScores[1].getInterfaceScoreMap().get(i).getCore1Scores()[0];
									interfaceScore.setCore1Scores(core1SCores);
									
									double[] rim2SCores =  new double[2];
									rim2SCores[0] = pdbScores[0].getInterfaceScoreMap().get(i).getRim2Scores()[0];
									rim2SCores[1] = pdbScores[1].getInterfaceScoreMap().get(i).getRim2Scores()[0];
									interfaceScore.setRim2Scores(rim2SCores);
									
									double[] core2SCores =  new double[2];
									core2SCores[0] = pdbScores[0].getInterfaceScoreMap().get(i).getCore2Scores()[0];
									core2SCores[1] = pdbScores[1].getInterfaceScoreMap().get(i).getCore2Scores()[0];
									interfaceScore.setCore2Scores(core2SCores);
									
									double[] finalScores =  new double[2];
									finalScores[0] = pdbScores[0].getInterfaceScoreMap().get(i).getFinalScores()[0];
									finalScores[1] = pdbScores[1].getInterfaceScoreMap().get(i).getFinalScores()[0];
									interfaceScore.setFinalScores(finalScores);
									
									resultsData.addInterfScore(interfaceScore);
									
									i++;
								}
							}
						}
						catch(Exception e)
						{
							
						}
					}
				}
			}
		}
		
		return resultsData;
	}
	
	public String test(String test)
	{
//		StatusData[] statusDataArray = null;
//		
//		Connection connection;
//		
//		try 
//		{
//			connection = DatabaseConnector.getConnection(dataSource);
//
//			if(connection != null)
//			{
//				String sessionId = getThreadLocalRequest().getSession().getId();
//				
//				String query = String.format("SELECT jobId, status FROM Jobs WHERE sessionId=\"%s\"",
//											 sessionId);
//				
//				Statement statement  = connection.createStatement();
//				
//				ResultSet results = null;
//				
//				if(statement != null)
//				{
//					results = statement.executeQuery(query);
//				}
//				
//				if(results != null)
//				{
//					int nrOfElements = results.getFetchSize();
//					
//					statusDataArray = new StatusData[nrOfElements];
//					
//					int i = 0;
//					
//					while(results.next())
//					{
//						StatusData statusDataItem = new StatusData();
//						statusDataItem.setJobId(results.getString(0));
//						statusDataItem.setStatus(results.getString(1));
//						
//						statusDataArray[i] = statusDataItem;
//						i++;
//					}
//				}
//				
//			}
//		} 
//		catch (NamingException e) 
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		catch (SQLException e) 
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return "";
		
		return getThreadLocalRequest().getSession().getId();
	}
	
	private boolean checkIfDirectoryExist(String directoryName)
	{
		File directory = new File(directoryName);
		
		if(directory.exists() && directory.isDirectory())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean checkIfFileExist(String fileName)
	{
		File file = new File(fileName);
		
		if(file.exists())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public List<StatusData> getJobsForCurrentSession()
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
