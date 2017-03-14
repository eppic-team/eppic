package ch.systemsx.sybit.sitemap;

import java.sql.ResultSet;
import java.sql.SQLException;

import eppic.commons.util.MySQLConnection;

public class SiteMapGenerator {

	public static void main(String args[])
	{
		System.out.println("Initializing the site map.....");
		String prefixUrl = "ewui";
		int limit = 50000;
		int offset = 0;
		int total = 0;
		
		try {
			//get the total
			MySQLConnection conn = new MySQLConnection("eppic_3_0_2017_01");
			java.sql.Statement st = conn.createStatement();
			String sql = "select count(*) from PdbInfo";
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				String count = rs.getString(1);
				total = Integer.parseInt(count);
				System.out.println("total: " + total);
			}			
			System.out.println("Connecting to the database.....");			
			System.out.println("Generating an xml sitemap....");

			while(offset < total){
				System.out.println("Creating a new xml file...");
				if (offset + limit > total){
					limit = total-offset;
				}
				st = conn.createStatement();
				sql = "select PdbCode from PdbInfo order by PdbCode asc limit " + limit + " offset " + offset;
				//System.out.println(sql);
				rs = st.executeQuery(sql);
				while (rs.next()) {
					String pdbCode = rs.getString(1);
					//System.out.println("Writing xml entry for ....." + pdbCode);
				}
				offset = offset+limit;
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
