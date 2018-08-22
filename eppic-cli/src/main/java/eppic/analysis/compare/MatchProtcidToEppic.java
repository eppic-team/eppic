package eppic.analysis.compare;

import eppic.commons.util.Goodies;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.xtal.SpaceGroup;



public class MatchProtcidToEppic {

	private static final String PROGRAM_NAME = "MatchProtcidToEppic";
	
	// for interfaces below this area, the warnings for no-match will have a different wording
	private static final double MIN_AREA = 100;
	
	public static void main(String[] args) throws Exception{
		
		
		File serializedFilesDir = null;
		String table = null;
		
		String help = "Usage: \n" +
				PROGRAM_NAME+"\n" +
				"   -w         :  dir containing webui.dat files for each PDB (in the usual divided layout).\n"+
				"   -t         :  table to read ProtCID interfaces from\n";
		
		Getopt g = new Getopt(PROGRAM_NAME, args, "w:t:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'w':
				serializedFilesDir = new File(g.getOptarg());
				break;
			case 't':
				table = g.getOptarg();
				break;
			case 'h':
			case '?':
				System.out.println(help);
				System.exit(0);
				break; // getopt() already printed an error
			}
		}
		
		if (serializedFilesDir==null){
			System.err.println("Must specify -w");
			System.err.println(help);
			System.exit(1);
		}
		if (table==null){
			System.err.println("Must specify -t");
			System.err.println(help);
			System.exit(1);
		}

		// TODO if we ever need to use this script again, we need to pass the db connection params somehow
		Connection conn = initMySQLConnection(null, null, null, null, null);

		java.sql.Statement st = conn.createStatement();
		String sql = "select pdbCode, interfaceId, chain1, chain2, dn_op1, dn_op2, dn_area from "+table;
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			
			String pdbCode = rs.getString(1);
			int interfaceId = rs.getInt(2);
			String chain1 = rs.getString(3);
			String chain2 = rs.getString(4);
			String op1 = rs.getString(5);
			String op2 = rs.getString(6);
			double area = rs.getDouble(7);
			
			SimpleInterface si = new SimpleInterface();
			si.setId(interfaceId);
			si.setChain1(chain1);
			si.setChain2(chain2);
			si.setArea(area);
			si.setOperator1(SpaceGroup.getMatrixFromAlgebraic(op1));
			si.setOperator2(SpaceGroup.getMatrixFromAlgebraic(op2));

			List<SimpleInterface> list = new ArrayList<SimpleInterface>();
			list.add(si);
			
			PdbInfoDB pdbInfo = null;
			
			try {
				pdbInfo = getPdbInfoFromFile(serializedFilesDir, pdbCode);
			} catch (IOException e) {
				System.err.println("Missing dat file for "+pdbCode+", error: "+e.getMessage());
				continue;
			}
			
			InterfaceMatcher im = new InterfaceMatcher(pdbInfo.getInterfaceClusters(), list);
			
			InterfaceDB ourI = im.getOurs(interfaceId);

			if (ourI==null) {
				String msgPrefix = "Failed: ";
				if (area<MIN_AREA) msgPrefix = "Failed (small area interface): ";
				System.err.println(msgPrefix+pdbCode+"\t"+interfaceId+"\t"+chain1+"\t"+chain2+"\t"+op1+"\t"+op2+"\t"+String.format("%7.2f",area));				
				continue;
			} 
			
			//TODO do we need to check for not 1:1 mapping??
			//if (!im.checkOneToOneMapping()) {
			//	// do something
			//	continue;
			//}
			
			System.out.print(pdbCode+"\t"+interfaceId+"\t"+chain1+"\t"+chain2+"\t"+op1+"\t"+op2+"\t"+String.format("%7.2f",area));
			
			System.out.println("\t"+ourI.getInterfaceId()+"\t"+ourI.getChain1()+"\t"+ourI.getChain2()+"\t"+ourI.getOperator()+"\t"+String.format("%7.2f",ourI.getArea()));
		}
	}
	
	private static PdbInfoDB getPdbInfoFromFile(File serializedFilesDir, String pdbCode) throws ClassNotFoundException, IOException {
		String midIndex = pdbCode.substring(1,3);
		File subdir = new File(serializedFilesDir,"divided"+File.separator+midIndex+File.separator+pdbCode);
		File webuidatFile = new File(subdir,pdbCode+".webui.dat");
		return (PdbInfoDB)Goodies.readFromFile(webuidatFile);
	}

	private static Connection initMySQLConnection(String dbHost, String dbPort, String dbUser, String dbPwd, String dbName) throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException e) {
			// this would indicate some problem with packaging, we can't really continue if this happens
			String msg = "Could not instantiate MySQL driver: " + e.getMessage() + ". Problem with app packaging?";
			throw new RuntimeException(msg);
		}

		String connStr="jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;

		return DriverManager.getConnection(connStr, dbUser, dbPwd);
	}
}
