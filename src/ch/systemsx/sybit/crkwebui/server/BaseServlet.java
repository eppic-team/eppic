package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

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
	protected Properties messages;

	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);

		InputStream propertiesStream = getServletContext()
				.getResourceAsStream(
						File.separator + "WEB-INF" + 
						File.separator + "classes" + 
						File.separator + "META-INF" + 
						File.separator + "server.properties");

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

		InputStream messagesStream = getServletContext()
				.getResourceAsStream(
						File.separator + "WEB-INF" + 
						File.separator + "classes" + 
						File.separator + "META-INF" + 
						File.separator + "constants.properties");

		messages = new Properties();

		try {
			messages.load(messagesStream);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException(
					"Properties with messages can not be read");
		}
	}

}
