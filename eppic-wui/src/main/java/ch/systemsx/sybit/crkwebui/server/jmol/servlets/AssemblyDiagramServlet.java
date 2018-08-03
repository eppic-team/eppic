package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.IOException;
import java.io.PrintWriter;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.AssemblyDiagramPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.AssemblyDiagramServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Servlet used to display an AssemblyDiagram page.
 * 
 * The following are the valid values for the parameters:
 * <pre>
 * 
 * Parameter name 					Parameter value
 * --------------					---------------
 * id								String (the jobId hash)
 * assembly							String (the eppic assembly id)
 *
 * @author Spencer Bliven
 * @author Jose Duarte
 */
public class AssemblyDiagramServlet extends BaseServlet
{

	private static final long serialVersionUID = 1L;

	/**
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "assemblyDiagram";

	private static final Logger logger = LoggerFactory.getLogger(AssemblyDiagramServlet.class);

	private String restPrefix;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		restPrefix = properties.getProperty("rest_prefix");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String requestedAssemblyStr = request.getParameter(LatticeGraphServlet.PARAM_ASSEMBLY);
		String size = request.getParameter(JmolViewerServlet.PARAM_SIZE);

		// setting a default size if not specified, #191
		if (size == null || size.trim().isEmpty()) 
			size = JmolViewerServlet.DEFAULT_SIZE;

		logger.info("Requested assemblyDiagram page for jobId={}, assembly={}",
				jobId, requestedAssemblyStr);

		PrintWriter outputStream = null;

		try
		{
			AssemblyDiagramServletInputValidator.validateLatticeGraphInput(jobId, null, null, requestedAssemblyStr);

			outputStream = new PrintWriter(response.getOutputStream());

			String title = jobId + " - Assembly Diagram";

			// should be no risk because validator checked for number and null
			int assemblyId = Integer.parseInt(requestedAssemblyStr);
			// the json data URL from REST API
			String jsonURL = restPrefix + "/assemblyDiagram/" + jobId + "/" + assemblyId;
			title += " for assembly " + requestedAssemblyStr;

			// TODO should we support interfaceId list and interfaceClusterId list too? Problem is we can't do that from db data only. Projection needs to be calculated for each case

			String webappRoot = request.getContextPath();
			logger.debug("Context path: {}", webappRoot);
			AssemblyDiagramPageGenerator.generateHTMLPage(title, size, jsonURL, outputStream, webappRoot);

		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during preparation of Assembly Diagram page.");
			logger.error("Error during preparation of Assembly Diagram page.",e);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during preparation of Assembly Diagram page.");
			logger.error("Error during preparation of Assembly Diagram page.",e);
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
