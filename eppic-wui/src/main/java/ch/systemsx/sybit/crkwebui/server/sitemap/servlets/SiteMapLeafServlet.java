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

public class SiteMapLeafServlet extends BaseServlet
{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "siteMapMain";
	
	//public static final String PARAM_INPUT = "input";
	//public static final String PARAM_SIZE = "size";
	
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
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		response.setContentType("text/xml;charset=UTF-8");
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

		if(start == null && limit == null){
			writeStaticSitemap(sb);
		}
		else{
			try {
				//get the total
				MySQLConnection conn = new MySQLConnection("eppic_3_0_2017_01");
				java.sql.Statement  st = conn.createStatement();
				String sql = "select PdbCode from PdbInfo order by PdbCode asc limit " + limit + " offset " + start;
				ResultSet rs = st.executeQuery(sql);
				while (rs.next()) {
					String pdbCode = rs.getString(1);
					if(pdbCode!= null){
						sb.append("\t<url>\n");
						sb.append("\t\t<loc>http://www.eppic-web.org/ewui/#id/"+ pdbCode +"</loc>\n");
						writeDetails(sb);
						sb.append("\t</url>\n");
					}
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sb.append("</urlset>");		
		response.getWriter().write(sb.toString());
	}
	
	public void writeDetails(StringBuilder sb){
		//example date format: should be like this 2017-03-27T23:55:42+01:00 
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = dateFormat.format(new Date());
		dateString = dateString.substring(0,9) + "T" + dateString.substring(11,dateString.length()) + "+00:00";
		sb.append("\t\t<lastmod>"+ dateString +"</lastmod>\n");
		sb.append("\t\t<changefreq>monthly</changefreq>\n");
		sb.append("\t\t<priority>0.5</priority>\n");
	}
	
	
	public void writeStaticSitemap(StringBuilder sb){
		
		//write the static pages
		
		sb.append("\t<url>\n");
		sb.append("\t\t<loc>http://www.eppic-web.org/ewui/</loc>\n");
		writeDetails(sb);
		sb.append("\t</url>\n");
		
		sb.append("\t<url>\n");
		sb.append("\t\t<loc>http://www.eppic-web.org/ewui/#downloads</loc>\n");
		writeDetails(sb);
		sb.append("\t</url>\n");
		
		sb.append("\t<url>\n");
		sb.append("\t\t<loc>http://www.eppic-web.org/ewui/#help</loc>\n");
		writeDetails(sb);
		sb.append("\t</url>\n");
		
		sb.append("\t<url>\n");
		sb.append("\t\t<loc>http://www.eppic-web.org/ewui/#faq</loc>\n");
		writeDetails(sb);
		sb.append("\t</url>\n");
		
		sb.append("\t<url>\n");
		sb.append("\t\t<loc>http://www.eppic-web.org/ewui/#releases</loc>\n");
		writeDetails(sb);
		sb.append("\t</url>\n");
		
		sb.append("\t<url>\n");
		sb.append("\t\t<loc>http://www.eppic-web.org/ewui/#statistics</loc>\n");
		writeDetails(sb);
		sb.append("\t</url>\n");
		
		sb.append("\t<url>\n");
		sb.append("\t\t<loc>http://www.eppic-web.org/ewui/#publications</loc>\n");
		writeDetails(sb);			
		sb.append("\t</url>\n");
	}
	


}
