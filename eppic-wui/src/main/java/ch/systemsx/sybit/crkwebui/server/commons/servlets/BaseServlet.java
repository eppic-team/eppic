package ch.systemsx.sybit.crkwebui.server.commons.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;

/**
 * Base class for other servlets.
 * @author srebniak_a
 *
 */
public class BaseServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Properties properties;

	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);

		InputStream propertiesStream = getServletContext()
				.getResourceAsStream(CrkWebServiceImpl.SERVER_PROPERTIES_FILE);

		properties = new Properties();

		try 
		{
			properties.load(propertiesStream);
		}
		catch (IOException e) 
		{
			//e.printStackTrace();
			throw new ServletException("Properties file '"+CrkWebServiceImpl.SERVER_PROPERTIES_FILE+"' can not be read. Error: "+e.getMessage());
		}
	}

}
