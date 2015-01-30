package eppic.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;

public class MySQLConnection {

	/*--------------------- constants -----------------------*/
	
    // defaults for database connection 
    private static final String    PASSWORD       =	"nieve";
    private static final String    DEFAULT_PORT   = "3306";
	
	/*------------------- member variables --------------------*/
	
	private Connection conn; 
	private String host;
	private String user;
	private String password;
	private String port;
	private String dbname;
	
	/*-------------------- constructors -----------------------*/

	/** 
	 * Connect to database using the given server, user and password
	 * @param dbServer
	 * @param dbUserName
	 * @param dbPassword
	 * @throws SQLException 
	 */
	public MySQLConnection(String dbServer, String dbUserName, String dbPassword) throws SQLException {
		loadMySQLDriver();
		host=dbServer;
		user=dbUserName;
		password=dbPassword;
		port=DEFAULT_PORT;
		dbname="";		
		String connStr="jdbc:mysql://"+host+":"+port+"/"+dbname;

		conn = DriverManager.getConnection(connStr, user, password);
	}

	/**
	 * Connect to database using the given server, user, password and dbname.
	 * Please always use this constructor in preference rather than constructing without specifying a database
	 * @param dbServer
	 * @param dbUserName
	 * @param dbPassword
	 * @param dbName 
	 * @throws SQLException 
	 */
	public MySQLConnection(String dbServer, String dbUserName, String dbPassword, String dbName) throws SQLException {
		loadMySQLDriver(); 
		host=dbServer;
		user=dbUserName;
		password=dbPassword;
		port=DEFAULT_PORT;
		dbname=dbName;		
		String connStr="jdbc:mysql://"+host+":"+port+"/"+dbname;

		conn = DriverManager.getConnection(connStr, user, password);
	}

	/**
	 * Connect to database using the given server and dbName
	 * Password taken from default, user name from unix user name
	 * @param dbServer
	 * @param dbName
	 * @throws SQLException 
	 */
	public MySQLConnection(String dbServer, String dbName) throws SQLException {
		loadMySQLDriver(); 
		host=dbServer;
		user=getUserName();
		password=PASSWORD;
		port=DEFAULT_PORT;
		dbname=dbName;
		String connStr="jdbc:mysql://"+host+":"+port+"/"+dbname;

		conn = DriverManager.getConnection(connStr, user, password);
	}

	/**
	 * Connect to database using the given server, user, password, dbname and port
	 * Only needed if mysql server uses a port different from the standard 3306
	 * @param dbServer
	 * @param dbUserName
	 * @param dbPassword
	 * @param dbName
	 * @param portNum
	 * @throws SQLException 
	 */
	public MySQLConnection(String dbServer, String dbUserName, String dbPassword, String dbName, int portNum) throws SQLException {
		loadMySQLDriver();
		host=dbServer;
		user=dbUserName;
		password=dbPassword;
		port=String.valueOf(portNum);
		dbname=dbName;		
		String connStr="jdbc:mysql://"+host+":"+port+"/"+dbname;

		conn = DriverManager.getConnection(connStr, user, password);
	}
		
	/**
	 * Connect to database giving a connection file
	 * @param connFile the connection file, if null then default ~/.my.cnf will be read
	 * @throws SQLException 
	 */
	public MySQLConnection(File connFile) throws SQLException {
		loadMySQLDriver();
		readConnectionFile(connFile);
		String connStr="jdbc:mysql://"+host+":"+port+"/"+dbname;

		conn = DriverManager.getConnection(connStr, user, password);
	}
	
	/**
	 * Connect to database reading connection parameters from default file ~/.my.cnf
	 * Equivalent to calling MySQLConnection(null)
	 * @throws SQLException
	 */
	public MySQLConnection() throws SQLException {
		loadMySQLDriver();
		readConnectionFile(null);
		String connStr="jdbc:mysql://"+host+":"+port+"/"+dbname;

		conn = DriverManager.getConnection(connStr, user, password);
	}
	
	/*---------------------------- private methods --------------------------*/
	
	/** 
	 * Get user name from operating system (for use as database username) 
	 */
	private static String getUserName() {
		String user = null;
		user = System.getProperty("user.name");
		if(user == null) {
			System.err.println("Could not get user name from operating system. Exiting");
			System.exit(1);
		}
		return user;
	}
	
	protected static void loadMySQLDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch(IllegalAccessException e) {
			e.printStackTrace();
			System.err.println("An exception occurred while loading the mysql jdbc driver, exiting.");
			System.exit(1);
		}
		catch(InstantiationException e) {
			e.printStackTrace();
			System.err.println("An exception occurred while loading the mysql jdbc driver, exiting.");
			System.exit(1);			
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println("An exception occurred while loading the mysql jdbc driver, exiting.");
			System.exit(1);			
		}
			
	}

	/**
	 * Reads mysql client connection parameters from file in standard mysql .cnf format.
	 * Only the [mysql] section of the file is read, the rest is ignored. The fields read
	 * are host, port, user, password and database
	 * If file doesn't exist a warning is printed and default blank values set.
	 * @param connFile if null default ~/.my.cnf is read
	 */
	private void readConnectionFile(File connFile) { 
		String homedir = System.getProperty("user.home"); 
		if (connFile==null) { // no file was specified
			connFile=new File(homedir,".my.cnf"); // assume default configuration file 
		}		
		// else the location of the connection file was given 		

		// setting default blank values, they will stay like that unless specified in file
		this.host = "";
		this.port = DEFAULT_PORT;
		this.user = "";
		this.password = "";
		this.dbname="";
		// checking if file is there, if not we print a warning and go ahead with connection
		if (!connFile.exists()) {
			System.err.println("Warning: MySQL connection file "+connFile+" doesn't exist. Will use default parameters for connection");
			return;
		}
		
		// get parameters from file 
		try {
			boolean inClientSection = false;
			String oneLine;
			BufferedReader fileIn = new BufferedReader(new FileReader(connFile));  
			while ((oneLine = fileIn.readLine()) != null ) {
				if (oneLine.startsWith("#")) continue;
				if (oneLine.startsWith("[mysql]")) {
					inClientSection = true;
					continue;
				} else if (oneLine.startsWith("[")) { // any other section is not a client section
					inClientSection = false;
					continue;
				}

				if (inClientSection) { 
					if( oneLine.startsWith("host=")) {
						host=oneLine.substring(oneLine.indexOf('=')+1, oneLine.length()).trim();
						continue; 
					} 
					if( oneLine.startsWith("port=")) {
						port=oneLine.substring(oneLine.indexOf('=')+1, oneLine.length()).trim();
						continue; 
					}
					if( oneLine.startsWith("user=")) {
						user=oneLine.substring(oneLine.indexOf('=')+1, oneLine.length()).trim();
						continue; 
					} 
					if( oneLine.startsWith("password=")) {
						password=oneLine.substring(oneLine.indexOf('=')+1, oneLine.length()).trim();
						continue; 
					}
					if( oneLine.startsWith("database=")) {
						dbname=oneLine.substring(oneLine.indexOf('=')+1, oneLine.length()).trim();
						continue; 
					}
				}
			} 

			fileIn.close();
		} 
		catch (IOException e) {
			System.err.println("Couldn't read MySQL connection file "+connFile+". Error: "+e.getMessage()+". Exiting");			
			System.exit(1);
		}  		
	}

	/*-------------------------- getters and setters ------------------------*/
	
	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * Returns a statement object created from the underlying connection.
	 */
	public Statement createStatement() throws SQLException {
		return this.conn.createStatement();
	}
	
	/**
	 * Execute an SQL operation, e.g. update or delete, ignoring any output.
	 * @param query
	 * @throws SQLException
	 */
	public void executeSql(String query) throws SQLException{
		Statement stmt;		
		stmt = conn.createStatement();	
		stmt.execute(query);
		stmt.close();		    
	}
	
	/**
	 * Closes the current connection. Errors are written to stdout.
	 */
	public void close() {		
		try {
			conn.close();
	    } catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState:     " + e.getSQLState());
			System.err.println("VendorError:  " + e.getErrorCode());
			e.printStackTrace();
	    } // end try/catch connection 
	}
	
	/**
	 * Returns the actual Connection object within this MySQLConnection. This is useful in case that an external
	 * library needs a default Connection object.
	 * @return the underling connection object of this connection
	 */
	public Connection getConnectionObject() {
		return this.conn;
	}

    /**
     * Prints the database size info for the given database.
     * @param dbName
     */
	public void printDbSizeInfo (String dbName) {
		double data = 0, index = 0, table_data = 0, table_index = 0, GB = Math.pow(2, 30);
		String Query = null, table = null;
		Statement Stmt = null;
		ResultSet RS = null;		
		try {
		    Query = "SHOW TABLE STATUS FROM "+dbName;
		    Stmt = this.conn.createStatement();
		    RS = Stmt.executeQuery(Query);
		    while (RS.next()) {
				table = RS.getString("Name");
				table_data = RS.getDouble("Data_length");
				table_index = RS.getDouble("Index_length");
				data += RS.getDouble("Data_length");
				index += RS.getDouble("Index_length");
				System.out.println("Table "+table+"##data:"+table_data+", index:"+table_index);
		    }
		    RS.close();
		    Stmt.close();	
		    System.out.println("Database "+dbName+" needs "+((data+index)/GB)+ " GB (data:"+(data/GB)+", index:"+(index/GB)+").");
		} 
		catch (SQLException e) {
		    System.err.println("SQLException: " + e.getMessage());
		    System.err.println("SQLState:     " + e.getSQLState());
		    System.err.println("VendorError:  " + e.getErrorCode());
		    e.printStackTrace();
		} // end try/catch connection
    }

    /**
     * Returns the minimum and the maximum value of the given double column in the given database.
     * Note that if the connection was created without pointing to a particular database then the
     * argument table must be specified as dbname.tablename.
     * @param table
     * @param column
     * @return a double[2] array where arr[0] = min and arr[1] = max
     */
    public double[] getRange(String table, String column) {
    	String query = "";
    	Statement S;
    	ResultSet R;
    	double[] range = new double[2];
    	try { 
    	    query = "SELECT MIN("+column+"), MAX("+column+") FROM "+table+";";
    	    S = this.conn.createStatement();
    	    R = S.executeQuery(query);    	
    	    if (R.next()) {
	    		range[0] = R.getDouble(1);
	    		range[1] = R.getDouble(2);
    	    } 
    	    R.close();
    	    S.close();
    	} // end try
    	catch (SQLException e) {
    	    System.err.println("SQLException: " + e.getMessage());
    	    System.err.println("SQLState:     " + e.getSQLState());
    	    System.err.println("VendorError:  " + e.getErrorCode());
    	    e.printStackTrace();
    	} // end catch    	
    	return range;    	
    }

    /**
     * Returns the minimum and the maximum value of the given double column in the given database
     * subject to a given WHERE condition. Note that if the connection was created without pointing
     * to a particular database then the argument table must be specified as dbname.tablename.
     * @param table
     * @param column
     * @param whereStr
     * @return
     */
    public double[] getRange(String table, String column, String whereStr) {
    	String query = "";
    	Statement S;
    	ResultSet R;
    	double[] range = new double[2];
    	try { 
    	    query = "SELECT MIN("+column+"), MAX("+column+") FROM "+table+" WHERE ("+whereStr+");";
    	    S = this.conn.createStatement();
    	    R = S.executeQuery(query);    	
    	    if (R.next()) {
	    		range[0] = R.getDouble(1);
	    		range[1] = R.getDouble(2);
    	    } 
    	    R.close();
    	    S.close();
    	} // end try
    	catch (SQLException e) {
    	    System.err.println("SQLException: " + e.getMessage());
    	    System.err.println("SQLState:     " + e.getSQLState());
    	    System.err.println("VendorError:  " + e.getErrorCode());
    	    e.printStackTrace();
    	} // end catch    	
    	return range;    	
    }
	
    /**
     * To get all index names for a certain table. Note the MySQLConnection object must be created with a non-blank database.
     * Using INFORMATION_SCHEMA db, only works for mysql 5.0 and above.
     * @param table
     * @return the names of the indexes
     */
    public String[] getAllIndexes4Table(String table) {
    	ArrayList<String> indexesAL=new ArrayList<String>();
    	String query;
    	Statement S;
    	ResultSet R;
    	try { 
    	    query = "SELECT DISTINCT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA='"+dbname+"' AND TABLE_NAME='"+table+"';";
    	    S = this.conn.createStatement();
    	    R = S.executeQuery(query);    	
    	    while (R.next()) {
	    		indexesAL.add(R.getString(1));	    		
    	    } 
    	    R.close();
    	    S.close();
    	} // end try     		
    	catch (SQLException e) {
    	    System.err.println("SQLException: " + e.getMessage());
    	    System.err.println("SQLState:     " + e.getSQLState());
    	    System.err.println("VendorError:  " + e.getErrorCode());
    	    e.printStackTrace();
    	} // end catch
    	String[] indexes=new String[indexesAL.size()];
    	int i=0;
    	for (String index:indexesAL) {
    		indexes[i]=index;
    		i++;
    	}
    	return indexes;
    }
    
    /**
     * Gets an array of Strings with all queries necessary to create all the indexes for a certain table 
     * @param table
     * @return
     */
    public String[] getCreateIndex4Table(String table){
    	String[] indexes=this.getAllIndexes4Table(table);
    	String[] createIndexQueries=new String[indexes.length];     	
    	for (int i=0;i<indexes.length;i++){
    		String index=indexes[i];
        	try { 
        		Statement S;
        		ResultSet R;
        	    String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
        	    				"WHERE TABLE_SCHEMA='"+dbname+"' AND TABLE_NAME='"+table+"' AND INDEX_NAME='"+index+"' " +
        	    						"ORDER BY SEQ_IN_INDEX;";
        	    S = this.conn.createStatement();
        	    R = S.executeQuery(query);
        	    String createIndexStr="CREATE INDEX "+index+" ON "+table+" (";
        	    while (R.next()) {
        	    	String colName = R.getString(1);
        	    	createIndexStr+=colName+",";
        	    }
        	    createIndexStr=createIndexStr.substring(0,createIndexStr.lastIndexOf(","));
        	    createIndexStr+=");";
        	    createIndexQueries[i]=createIndexStr;
        	    R.close();
        	    S.close();
        	} // end try     		
        	catch (SQLException e) {
        	    System.err.println("SQLException: " + e.getMessage());
        	    System.err.println("SQLState:     " + e.getSQLState());
        	    System.err.println("VendorError:  " + e.getErrorCode());
        	    e.printStackTrace();
        	} // end catch    		
    	}
    	return createIndexQueries;
    }
    
    /**
     * To get the column type for a certain column and table
     * @param table
     * @param column
     * @return
     */
    public String getColumnType(String table,String column){
    	String query = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
    					"WHERE TABLE_SCHEMA='"+this.dbname+"' AND TABLE_NAME='"+table+"' AND COLUMN_NAME='"+column+"';";
    	String colType = this.getStringFromDb(query);
    	return colType;
    }
    
    /**
     * To findout whether a key (i.e. column) is numeric-based or text-based
     * @param table
     * @param key
     * @return true if is numeric-based, false if is text-based
     */
    public boolean isKeyNumeric(String table, String key){
    	boolean isNumeric = false;
    	String colType = getColumnType(table,key);
    	if (colType.contains("int") || colType.contains("INT")){
    		isNumeric = true;
    	}
    	else if (colType.contains("char") || colType.contains("CHAR")){
    		isNumeric = false;
    	}
    	else {
    		System.err.println("The key '"+key+"' from table '"+table+"' is neither numeric-based (int) nor text-based (char/varchar). Check what's wrong!");
    	}
    	return isNumeric;
    }
    
    /**
     * To get all tables for this MySQLConnection's database.
     * @return an array of String with all table names
     */
	public String[] getTables4Db(){
		String[] tables=null;
		ArrayList<String> tablesAL=new ArrayList<String>();
		String query="SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='"+dbname+"' ORDER BY TABLE_NAME DESC;";
		try {			
			Statement S = this.conn.createStatement();
			ResultSet R=S.executeQuery(query);
			while (R.next()){
				tablesAL.add(R.getString(1));
			}
			S.close();
			R.close();
			tables=new String[tablesAL.size()];
			for (int i=0;i<tablesAL.size();i++) {
				tables[i]=tablesAL.get(i);
			}
		}
		catch(SQLException e){			
			System.err.println("Couldn't get table names from "+host+" for db="+dbname);
    	    System.err.println("SQLException: " + e.getMessage());
    	    System.err.println("SQLState:     " + e.getSQLState());
    	    System.err.println("VendorError:  " + e.getErrorCode());
			e.printStackTrace();
		}
		return tables;
	}
	
	/** 
	 * To get all distinct ordered ids from a certain key and table from this MySQLConnection
	 * @param key the key name
	 * @param table the table name
	 * @return int array containing all ids 
	 */
	public Integer[] getAllNumIds4KeyAndTable(String key, String table){
		Integer[] allIds=null;		
		try {
			Statement S=conn.createStatement();
			String query="SELECT DISTINCT "+key+" FROM "+table+" ORDER BY "+key+";";
			ResultSet R=S.executeQuery(query);
			ArrayList<Integer> idsAL=new ArrayList<Integer>();
			while (R.next()){
				idsAL.add(R.getInt(1));
			}
			allIds=new Integer[idsAL.size()];
			for (int i=0;i<idsAL.size();i++) {
				allIds[i]=idsAL.get(i);
			}
			R.close();
			S.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		return allIds;
	}

	/** 
	 * To get all distinct ordered text (i.e. char/varchar column) ids from a certain key and table from this MySQLConnection
	 * @param key the key name
	 * @param table the table name
	 * @return int array containing all ids 
	 */
	public String[] getAllTxtIds4KeyAndTable(String key, String table){
		String[] allIds=null;		
		try {
			Statement S=conn.createStatement();
			String query="SELECT DISTINCT "+key+" FROM "+table+" ORDER BY "+key+";";
			ResultSet R=S.executeQuery(query);
			ArrayList<String> idsAL=new ArrayList<String>();
			while (R.next()){
				idsAL.add(R.getString(1));
			}
			allIds=new String[idsAL.size()];
			for (int i=0;i<idsAL.size();i++) {
				allIds[i]=idsAL.get(i);
			}
			R.close();
			S.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		return allIds;
	}

	/** 
	 * To get all distinct ordered ids from a certain key and table from this MySQLConnection
	 * @param key the key name
	 * @param table the table name
	 * @return int array containing all ids 
	 */
	public Object[] getAllIds4KeyAndTable(String key, String table){
		Object[] allIds=null;
		try {
			Statement S=conn.createStatement();
			String query="SELECT DISTINCT "+key+" FROM "+table+" ORDER BY "+key+";";
			ResultSet R=S.executeQuery(query);
			ArrayList<String> idsAL=new ArrayList<String>();
			while (R.next()){
				idsAL.add(R.getString(1));
			}
			if (isKeyNumeric(table,key)){
				allIds=new Integer[idsAL.size()];
				for (int i=0;i<idsAL.size();i++) {
					allIds[i]=Integer.parseInt(idsAL.get(i));
				}
			} else {
				allIds=new String[idsAL.size()];
				for (int i=0;i<idsAL.size();i++){
					allIds[i]=idsAL.get(i);
				}
			}
			R.close();
			S.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		return allIds;
	}

    /**
     * To set the sql_mode of this connection. 
     * @param sqlmode either NO_UNSIGNED_SUBTRACTION or blank
     */
    public void setSqlMode(String sqlmode) {
    	String query="SET SESSION sql_mode='"+sqlmode+"';";
    	try {
    		this.executeSql(query);
    	}
    	catch (SQLException e){
    		System.err.println("Couldn't change the sql mode to "+sqlmode);
    	    System.err.println("SQLException: " + e.getMessage());
    	    System.err.println("SQLState:     " + e.getSQLState());
    	    System.err.println("VendorError:  " + e.getErrorCode());
    	    e.printStackTrace();    		
    	}
    }
    
    // Convenience methods for retrieving data from the database without having to iterate
    // over result sets
    
	/** 
	 * Returns the first column of the first row of the result of the given query as a string
	 * or null if no results were found. SQL errors are printed to stderr.
	 * @param query the sql query string
	 * @return the first column of the first row of the result of the given query as a string
	 * or null if no results were found
	 */	
	public String getStringFromDb(String query) {
		Statement    stmt;
		ResultSet    rs;
		String       result = null;
		
		try { 			
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery(query);
		    if(rs.next()) {
		    	result = rs.getString(1);
		    }
		    rs.close();
		    stmt.close(); 		    
		} // end try
		catch (SQLException e) {
		    System.err.println("SQLException: " + e.getMessage());
		    System.err.println("SQLState:     " + e.getSQLState());
		    System.err.println("VendorError:  " + e.getErrorCode());
		    e.printStackTrace();
		} // end catch				
		
		return result;
	}
	
	/** 
	 * Returns the first column of the first row of the result of the given query as an integer
	 * or -1 if no results were found. SQL errors are printed to stderr.
	 * @param query the sql query string
	 * @return the first column of the first row of the result of the given query as a string
	 * or -1 if no such results were found
	 */
	public int getIntFromDb(String query) {
		Statement    stmt;
		ResultSet    rs;
		int          result = -1;

		try { 			
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery(query);
		    if(rs.next()) {
		    	result = rs.getInt(1);
		    }
		    rs.close();
		    stmt.close();		    
		} // end try
		catch (SQLException e) {
		    System.err.println("SQLException: " + e.getMessage());
		    System.err.println("SQLState:     " + e.getSQLState());
		    System.err.println("VendorError:  " + e.getErrorCode());
		    e.printStackTrace();
		} // end catch			
		
		return result;
	}
	
	/** 
	 * Returns the first column of the first row of the result of the given query as a double
	 * or NaN if no results were found. SQL errors are printed to stderr.
	 * @param query the sql query string
	 * @return the first column of the first row of the result of the given query as a string
	 * or NaN if no such results were returned
	 */
	public double getDoubleFromDb(String query) {
		Statement    stmt;
		ResultSet    rs;
		double       result = Double.NaN;

		try {		
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery(query);
		    if(rs.next()) {
		    	result = rs.getDouble(1);
		    }
		    rs.close();
		    stmt.close(); 
		    
		} // end try
		catch (SQLException e) {
		    System.err.println("SQLException: " + e.getMessage());
		    System.err.println("SQLState:     " + e.getSQLState());
		    System.err.println("VendorError:  " + e.getErrorCode());
		    e.printStackTrace();
		} // end catch			
		return result;
	}
	
	/**
	 * Returns the first column of the results of the given query as an array of strings.
	 * or null if no results were found. SQL errors are printed to stderr.
	 * @param query the sql query string
	 * @return the results of the given query as an array of strings.
	 * or null if no such results were found.
	 */
	public String[] getStringsFromDb(String query) {

		Statement    stmt;
		ResultSet    rs;
		String[]     result = null;
		LinkedList<String> resultList = new LinkedList<String>();
		
		try { 			
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery(query);
		    while(rs.next()) {
		    	resultList.add(rs.getString(1));
		    }
		    rs.close();
		    stmt.close();
		    if(resultList.size() > 0) {
		    	result = new String[resultList.size()];
		    	result = resultList.toArray(result);
		    }
		}
		catch (SQLException e) {
		    System.err.println("SQLException: " + e.getMessage());
		    System.err.println("SQLState:     " + e.getSQLState());
		    System.err.println("VendorError:  " + e.getErrorCode());
		    e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Returns the first column of the results of the given query as an array of integers.
	 * or null if no results were found. SQL errors are printed to stderr.
	 * @param query the sql query string
	 * @return the results of the given query as an array of integers.
	 * or null if no such results were found.
	 */
	public Integer[] getIntsFromDb(String query) {

		Statement    stmt;
		ResultSet    rs;
		Integer[]     result = null;
		LinkedList<Integer> resultList = new LinkedList<Integer>();
		
		try { 			
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery(query);
		    while(rs.next()) {
		    	resultList.add(rs.getInt(1));
		    }
		    rs.close();
		    stmt.close();
		    if(resultList.size() > 0) {
		    	result = new Integer[resultList.size()];
		    	result = resultList.toArray(result);
		    }
		}
		catch (SQLException e) {
		    System.err.println("SQLException: " + e.getMessage());
		    System.err.println("SQLState:     " + e.getSQLState());
		    System.err.println("VendorError:  " + e.getErrorCode());
		    e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Returns the first column of the results of the given query as an array of doubles.
	 * or null if no results were found. SQL errors are printed to stderr.
	 * @param query the sql query string
	 * @return the results of the given query as an array of doubles.
	 * or null if no such results were found.
	 */
	public Double[] getDoublesFromDb(String query) {

		Statement    stmt;
		ResultSet    rs;
		Double[]     result = null;
		LinkedList<Double> resultList = new LinkedList<Double>();
		
		try { 			
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery(query);
		    while(rs.next()) {
		    	resultList.add(rs.getDouble(1));
		    }
		    rs.close();
		    stmt.close();
		    if(resultList.size() > 0) {
		    	result = new Double[resultList.size()];
		    	result = resultList.toArray(result);
		    }
		}
		catch (SQLException e) {
		    System.err.println("SQLException: " + e.getMessage());
		    System.err.println("SQLState:     " + e.getSQLState());
		    System.err.println("VendorError:  " + e.getErrorCode());
		    e.printStackTrace();
		}
		
		return result;
	}
    
}
