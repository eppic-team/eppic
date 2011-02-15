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

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

//TODO will be changed to use datasource
public class DBUtils 
{
	private static String dataSource;

	public static void setDataSource(String selectedDataSource)
	{
		dataSource = selectedDataSource;
	}

	// /**
	// * Get connection from the pool
	// * @return connection to the database
	// * @throws NamingException
	// * @throws SQLException
	// */
	// public static Connection getConnection(String source) throws
	// NamingException, SQLException
	// {
	// Connection connection = null;
	//
	// InitialContext initialContext = new InitialContext();
	//
	// if(initialContext != null)
	// {
	// DataSource dataSoure = (DataSource)initialContext.lookup(source);
	// connection = dataSoure.getConnection();
	// }
	//
	// return connection;
	// }

	public static Connection getConnection() throws NamingException,
			SQLException 
	{
		Connection connection = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		connection = DriverManager.getConnection(dataSource);

		return connection;
	}

	public static void insertNewJob(String jobId, String sessionId,
			String email, String input)  throws CrkWebException 
	{
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = getConnection();

			if (connection != null) {
				String query = "INSERT INTO Jobs(jobId, sessionId, status, email, ip, input) VALUES(?,?,?,?,?,?)";

				statement = connection.prepareStatement(query);

				if (statement != null) {
					statement.setString(1, jobId);
					statement.setString(2, sessionId);
					statement.setString(3, "Running");
					statement.setString(4, email);
					statement.setString(5, "");
					statement.setString(6, input);

					statement.executeUpdate();
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static List<ProcessingInProgressData> getJobsForCurrentSession(String sessionId) throws CrkWebException
	{
		List<ProcessingInProgressData> statusData = null;

		Connection connection = null;
		Statement statement = null;

		try {
			connection = getConnection();

			if (connection != null) {
				String query = String
						.format("SELECT jobId, status, input FROM Jobs WHERE sessionId=\"%s\"",
								sessionId);

				statement = connection.createStatement();

				ResultSet results = null;

				if (statement != null) {
					results = statement.executeQuery(query);
				}

				if (results != null) {
					statusData = new ArrayList<ProcessingInProgressData>();

					while (results.next()) {
						ProcessingInProgressData statusDataItem = new ProcessingInProgressData();
						statusDataItem.setJobId(results.getString(1));
						statusDataItem.setStatus(results.getString(2));
						statusDataItem.setInput(results.getString(3));

						statusData.add(statusDataItem);
					}
				}

			}
		} catch (NamingException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return statusData;
	}

	public static void untieJobsFromSession(String sessionId) throws CrkWebException 
	{
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = getConnection();

			if (connection != null) {
				String query = "UPDATE Jobs SET sessionId=NULL WHERE sessionId=?";

				statement = connection.prepareStatement(query);

				if (statement != null) {
					statement.setString(1, sessionId);

					statement.executeUpdate();
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void updateStatusOfJob(String jobId, String status) throws CrkWebException 
	{
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = getConnection();

			if (connection != null) {
				String query = "UPDATE Jobs SET status=? WHERE jobId=?";

				statement = connection.prepareStatement(query);

				if (statement != null) {
					statement.setString(1, status);
					statement.setString(2, jobId);

					statement.executeUpdate();
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getStatusForJob(String jobId) throws CrkWebException 
	{
		String status = "nonexisting";

		Connection connection = null;
		Statement statement = null;

		try {
			connection = getConnection();

			if (connection != null) {
				String query = String.format(
						"SELECT status FROM Jobs WHERE jobId=\"%s\"", jobId);

				statement = connection.createStatement();

				ResultSet results = null;

				if (statement != null) {
					results = statement.executeQuery(query);
				}

				if (results != null) 
				{

					while (results.next()) {
						status = results.getString(1);
					}
				}

			}
		} catch (NamingException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CrkWebException(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
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
	
	public static int getNrOfJobsForSessionId(String sessionId) throws CrkWebException
	{
		int nrOfJobsForSessionId = 0;
		
		Connection connection = null;
		Statement statement = null;

		try 
		{
			connection = getConnection();

			if (connection != null) {
				String query = String.format(
						"SELECT count(jobId) FROM Jobs WHERE sessionId=\"%s\"", sessionId);

				statement = connection.createStatement();

				ResultSet results = null;

				if (statement != null) {
					results = statement.executeQuery(query);
				}

				if (results != null) 
				{
					while (results.next())
					{
						nrOfJobsForSessionId = results.getInt(1);
					}
				}

			}
		}
		catch (NamingException e) 
		{
			e.printStackTrace();
			throw new CrkWebException(e);
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			throw new CrkWebException(e);
		} 
		finally 
		{
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return nrOfJobsForSessionId;
	}

}
