package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.FileContentReader;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.JmolPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.JmolViewerServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Servlet used to open jmol.
 * @author AS
 */
public class JmolViewerServlet extends BaseServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String resultsLocation;
	private String protocol;
	private String generalDestinationDirectoryName;
	private String servletContPath;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		
		resultsLocation = properties.getProperty("results_location");
		
		protocol = "http";
		if(properties.getProperty("protocol") != null)
		{
			protocol = properties.getProperty("protocol");
		}
		
		generalDestinationDirectoryName = properties.getProperty("destination_path");
		
		servletContPath = getServletContext().getInitParameter("servletContPath");
	}
	
	@Override
	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response) throws ServletException, IOException
	{
		String jobId = request.getParameter("id");
		String interfaceId = request.getParameter("interface");
		String input = request.getParameter("input");
		String size = request.getParameter("size");
		
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();

		String serverUrl = protocol + "://" + serverName + ":" + serverPort + "/" + servletContPath;
		
		File jmolScriptFile = new File(generalDestinationDirectoryName + File.separator + jobId, 
									   input + "." + interfaceId + ".jmol");
		
		ServletOutputStream outputStream = null;
		
		try
		{
			JmolViewerServletInputValidator.validateJmolViewerInput(jobId, interfaceId, input, size);
			
			String jmolScript = FileContentReader.readContentOfFile(jmolScriptFile, true);
			
			String jmolPage = JmolPageGenerator.generatePage(jobId + " - " + interfaceId + "\n", 
															 size, 
															 serverUrl, 
															 resultsLocation + jobId, 
															 input + "." + interfaceId + ".pdb.gz", 
															 jmolScript);

			outputStream = response.getOutputStream();
			outputStream.println(jmolPage);
		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error during preparation of jmol page.");
		}
		finally
		{
			if(outputStream != null)
			{
				try
				{
					outputStream.close();
				}
				catch(Throwable t) {}
			}
		}
	}
}
