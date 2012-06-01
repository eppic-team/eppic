package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.server.util.FileContentReader;

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

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
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
		
		String protocol = "http";
		if(properties.getProperty("protocol") != null)
		{
			protocol = properties.getProperty("protocol");
		}
		
		String serverUrl = protocol + "://" + serverName + ":" + serverPort + "/crkwebui";
		
		String resultsLocation = properties.getProperty("results_location");
		
		String generalDestinationDirectoryName = properties.getProperty("destination_path");
		File jmolScriptFile = new File(generalDestinationDirectoryName + File.separator + jobId, 
									   input + "." + interfaceId + ".jmol");
		String jmolScript = FileContentReader.readContentOfFile(jmolScriptFile);
        
		
		ServletOutputStream output = response.getOutputStream();
		output.println("<html>");
		output.println("<head>");
		output.println("<title>");
		output.println(jobId + " - " + interfaceId);
		output.println("</title>");
		output.println("<script type=\"text/javascript\" language=\"javascript\" src=\"" + 
						serverUrl + "/resources/jmol/Jmol.js\"></script>");
		output.println("</head>");
		output.println("<body>");
		output.println("<script>");
		output.println("jmolInitialize(\"" + serverUrl + "/resources/jmol\");");
		output.println("jmolSetCallback(\"language\", \"en\");");
		output.println("jmolApplet(" + size + ", 'load " + serverUrl + "/" + resultsLocation + jobId + "/" + input + "." + interfaceId + ".pdb.gz; "+ jmolScript + "');");
		output.println("</script>");
		output.println("</body>");
		output.println("</html>");
	}
}
