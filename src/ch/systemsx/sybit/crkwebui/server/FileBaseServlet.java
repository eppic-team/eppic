package ch.systemsx.sybit.crkwebui.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Base class for file upload and download servlets
 * @author srebniak_a
 *
 */
public class FileBaseServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Properties properties;
	protected Properties messages;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		InputStream propertiesStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/server.properties");

		properties = new Properties();

		try {
			properties.load(propertiesStream);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException("Properties file can not be read");
		}

		InputStream messagesStream = getServletContext()
				.getResourceAsStream(
						"/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/constants.properties");

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
