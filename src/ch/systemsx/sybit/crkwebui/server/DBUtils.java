package ch.systemsx.sybit.crkwebui.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import ch.systemsx.sybit.crkwebui.client.data.StatusData;

//TODO will be changed to use datasource
public class DBUtils 
{
	private static String dataSource;
	
	public static void setDataSource(String selectedDataSource)
	{
		dataSource = selectedDataSource;
	}
//	/**
//	 * Get connection from the pool
//	 * @return connection to the database
//	 * @throws NamingException 
//	 * @throws SQLException 
//	 */
//	public static Connection getConnection(String source) throws NamingException, SQLException
//	{
//		Connection connection = null;
//		
//		InitialContext initialContext = new InitialContext();
//		
//		if(initialContext != null)
//		{
//			DataSource dataSoure = (DataSource)initialContext.lookup(source);
//			connection = dataSoure.getConnection();
//		}
//		
//		return connection;
//	}
	
	public static Connection getConnection() throws NamingException, SQLException
	{
		Connection connection = null;
		
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
		} 
		catch (ClassNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		connection = DriverManager.getConnection(dataSource);
		
		return connection;
	}
	
	public static String insertNewJob(String jobId,
									  String sessionId,
									  String email,
									  String input)
	{
		String errorMessage = null;
		
		Connection connection = null;
		PreparedStatement statement = null;
	
		try 
		{
			connection = getConnection();
	
			if(connection != null)
			{
				String query = "INSERT INTO Jobs(jobId, sessionId, status, email, ip, input) VALUES(?,?,?,?,?,?)";
				
				statement  = connection.prepareStatement(query);
	
				if(statement != null)
				{
					statement.setString(1, jobId);
					statement.setString(2, sessionId);
					statement.setString(3, "Running");
					statement.setString(4, email);
					statement.setString(5, "");
					statement.setString(6, input);
					
					statement.executeUpdate();
				}
			}
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
			errorMessage = e.getMessage();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			errorMessage = e.getMessage();
		}
		finally
		{
			if(statement != null)
			{
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			if(connection != null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return errorMessage;
	}
	
	public static List<StatusData> getJobsForCurrentSession(String sessionId)
	{
		List<StatusData> statusData = null;
		
		Connection connection = null;
		Statement statement = null;
		
		try 
		{
			connection = getConnection();
			
			if(connection != null)
			{
				String query = String.format("SELECT jobId, status, input FROM Jobs WHERE sessionId=\"%s\"",
											 sessionId);
				
				statement  = connection.createStatement();
				
				ResultSet results = null;
				
				if(statement != null)
				{
					results = statement.executeQuery(query);
				}
				
				if(results != null)
				{
					statusData = new ArrayList<StatusData>();
					
					while(results.next())
					{
						StatusData statusDataItem = new StatusData();
						statusDataItem.setJobId(results.getString(1));
						statusDataItem.setStatus(results.getString(2));
						statusDataItem.setInput(results.getString(3));
						
						statusData.add(statusDataItem);
					}
				}
				
			}
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(statement != null)
			{
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(connection != null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return statusData;
	}

	public static String untieJobsFromSession(String sessionId)
	{
		String errorMessage = null;
		
		Connection connection = null;
		PreparedStatement statement = null;
		
		try 
		{
			connection = getConnection();
			
			if(connection != null)
			{
				String query = "UPDATE Jobs SET sessionId=NULL WHERE sessionId=?";
				
				statement  = connection.prepareStatement(query);
				
				if(statement != null)
				{
					statement.setString(1, sessionId);
					
					statement.executeUpdate();
				}
			}
		} 
		catch (NamingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorMessage = e.getMessage();
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorMessage = e.getMessage();
		}
		finally
		{
			if(statement != null)
			{
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(connection != null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return errorMessage;
	}
	
	public static String updateStatusOfJob(String jobId,
									 	   String status) 
	{
		String errorMessage = null;
		
		Connection connection = null;
		PreparedStatement statement = null;
		
		try 
		{
			connection = getConnection();
			
			if(connection != null)
			{
				String query = "UPDATE Jobs SET status=? WHERE jobId=?";
				
				statement  = connection.prepareStatement(query);
				
				if(statement != null)
				{
					statement.setString(1, status);
					statement.setString(2, jobId);
					
					statement.executeUpdate();
				}
			}
		} 
		catch (NamingException e) 
		{
			errorMessage = e.getMessage();
			e.printStackTrace();
		}
		catch (SQLException e) 
		{
			errorMessage = e.getMessage();
			e.printStackTrace();
		}
		finally
		{
			if(statement != null)
			{
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		
			if(connection != null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return errorMessage;
	}	
	
	public static String getStatusForJob(String jobId)
	{
		String status = "nonexisting";
		
		Connection connection = null;
		Statement statement = null;
		
		try 
		{
			connection = getConnection();
			
			if(connection != null)
			{
				String query = String.format("SELECT status FROM Jobs WHERE jobId=\"%s\"",
											 jobId);
				
				statement  = connection.createStatement();
				
				ResultSet results = null;
				
				if(statement != null)
				{
					results = statement.executeQuery(query);
				}
				
				if(results != null)
				{
					int i = 0;
					
					while(results.next())
					{
						status = results.getString(1);
						i++;
					}
				}
				
			}
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(statement != null)
			{
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(connection != null)
			{
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return status;
	}
	
}
