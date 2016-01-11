package ch.systemsx.sybit.crkwebui.server.commons.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;
import eppic.EppicParams;

/**
 * Base class for other servlets.
 * @author srebniak_a
 *
 */
public class BaseServlet extends HttpServlet {

	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);

	protected Properties properties;
	protected Properties propertiesCli;

	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);

		properties = new Properties();
		propertiesCli = new Properties();

		try {
			
			logger.info("Reading server property file "+CrkWebServiceImpl.SERVER_PROPERTIES_FILE);
			InputStream propertiesStream = 
					new FileInputStream(new File(CrkWebServiceImpl.SERVER_PROPERTIES_FILE));

			properties.load(propertiesStream);			
			
		}
		catch (IOException e) 
		{
			
			throw new ServletException("Properties file '"+CrkWebServiceImpl.SERVER_PROPERTIES_FILE+"' can not be read. Error: "+e.getMessage());
		}
		
		File eppicCliConfFile = new File(System.getProperty("user.home"), EppicParams.CONFIG_FILE_NAME);
		
		try {
			logger.info("Reading CLI properties file {} ", eppicCliConfFile.toString());
			
			propertiesCli.load(new FileInputStream(eppicCliConfFile));

		} catch (IOException e) {
			throw new ServletException("Properties file '"+eppicCliConfFile+"' can not be read. Error: "+e.getMessage());
		}
	}

}
