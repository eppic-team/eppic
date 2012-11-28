package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.server.util.FileContentReader;
import ch.systemsx.sybit.crkwebui.server.validators.JmolViewerServletInputValidator;
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
		
		try
		{
			JmolViewerServletInputValidator.validateJmolViewerInput(jobId, interfaceId, input, size);
			
			String jmolScript = FileContentReader.readContentOfFile(jmolScriptFile, true);
			
			ServletOutputStream output = response.getOutputStream();
			output.println(generateOutput(jobId, interfaceId, input, size, serverUrl, jmolScript));
		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error during preparation of jmol page.");
		}
	}
	
	private String generateOutput(String jobId,
								  String interfaceId,
								  String input,
								  String size,
								  String serverUrl,
								  String jmolScript)
	{
		jmolScript = jmolScript.replaceAll("\n", "");
		
		StringBuffer jmolPage = new StringBuffer();
		
		jmolPage.append("<html>" + "\n");
		jmolPage.append("<head>" + "\n");
		jmolPage.append("<title>" + "\n");
		jmolPage.append(jobId + " - " + interfaceId + "\n");
		jmolPage.append("</title>" + "\n");
		jmolPage.append("<script type=\"text/javascript\" language=\"javascript\" src=\"" + 
						serverUrl + "/resources/jmol/Jmol.js\"></script>" + "\n");
		jmolPage.append("</head>" + "\n");
		jmolPage.append("<body>" + "\n");
		jmolPage.append("<script>" + "\n");
		jmolPage.append("jmolInitialize(\"" + serverUrl + "/resources/jmol\");" + "\n");
		jmolPage.append("jmolSetCallback(\"language\", \"en\");" + "\n");
		jmolPage.append("jmolApplet(" + size + ", 'load " + serverUrl + "/" + resultsLocation + jobId + "/" + input + "." + interfaceId + ".pdb.gz; "+ jmolScript + "');" + "\n");
		jmolPage.append("</script>" + "\n");
		jmolPage.append("</body>" + "\n");
		jmolPage.append("</html>" + "\n");
		
		return jmolPage.toString();
	}
}
