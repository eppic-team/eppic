package ch.systemsx.sybit.crkwebui.server.sitemap.servlets;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.JmolViewerServlet;
import eppic.commons.util.MySQLConnection;

public class SiteMapMainServlet extends BaseServlet
{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "siteMapMain";
	
	private static final Logger logger = LoggerFactory.getLogger(JmolViewerServlet.class);
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}
	
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		response.setContentType("text/xml;charset=UTF-8");
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		sb.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
		
		sb.append("<sitemap>\n");
		sb.append("\t<loc>http://www.eppic-web.org/ewui/sitemap/sitemap.xml</loc>\n");
		sb.append("</sitemap>\n");
		
		//write the links to the dynamic pages
		int limit = 50000;
		try {
			//get the total
			MySQLConnection conn = new MySQLConnection("eppic_3_0_2017_01");
			java.sql.Statement st = conn.createStatement();
			String sql = "select count(*) from PdbInfo";
			ResultSet rs = st.executeQuery(sql);
			
			//this loop executes once
			while (rs.next()) {
				String count = rs.getString(1);
				int total = Integer.parseInt(count);				
				for(int start=0; start<total; start=start+limit){
					if(start + limit > total){
						limit = total - start;
					}					
					sb.append("\t<sitemap>\n");
					sb.append("\t\t<loc>http://www.eppic-web.org/ewui/sitemap/sitemap.xml?start=" + start + "&#038;limit=" + limit + ";</loc>\n");
					writeDetails(sb);
					sb.append("\t</sitemap>\n");
				}
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb.append("</sitemapindex>\n");	
		response.getWriter().write(sb.toString());
	}
	
	public void writeDetails(StringBuilder sb){
		//example date format: should be like this 2017-03-27T23:55:42+01:00 
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = dateFormat.format(new Date());
		dateString = dateString.substring(0,9) + "T" + dateString.substring(11,dateString.length()) + "+00:00";
		sb.append("\t\t<lastmod>"+ dateString +"</lastmod>\n");
	}
	
	

	


}
