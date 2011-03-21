package ch.systemsx.sybit.crkwebui.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.NamingException;

/**
 * Database connector
 * @author srebniak_a
 *
 */
public class DatabaseConnector 
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
	
	public static Connection getConnection() throws NamingException, SQLException, ClassNotFoundException
	{
		Connection connection = null;
		
		Class.forName("com.mysql.jdbc.Driver");
		
		connection = DriverManager.getConnection(dataSource);
		
		return connection;
	}
	
}
