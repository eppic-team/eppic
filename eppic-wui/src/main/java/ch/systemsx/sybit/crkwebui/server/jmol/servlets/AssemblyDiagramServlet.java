package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

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
import eppic.model.dto.Interface;
import eppic.model.dto.PdbInfo;
import eppic.commons.util.IntervalSet;
import eppic.db.dao.DaoException;

/**
 * Servlet used to display an AssemblyDiagram page.
 * 
 * The following are the valid values for the parameters:
 * <pre>
 * 
 * Parameter name 					Parameter value
 * --------------					---------------
 * id								String (the jobId hash)
 * interfaces						String (comma-separated list of interface ids)
 * clusters							String (comma-separated list of interface cluster ids). Superseded by interfaces.
 *
 * @author Spencer Bliven
 */
public class AssemblyDiagramServlet extends BaseServlet
{

	private static final long serialVersionUID = 1L;

	/**
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "assemblyDiagram";

	private static final Logger logger = LoggerFactory.getLogger(AssemblyDiagramServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String requestedIfacesStr = request.getParameter(LatticeGraphServlet.PARAM_INTERFACES);
		String requestedAssemblyStr = request.getParameter(LatticeGraphServlet.PARAM_ASSEMBLY);
		String requestedClusterStr = request.getParameter(LatticeGraphServlet.PARAM_CLUSTERS);
		String size = request.getParameter(JmolViewerServlet.PARAM_SIZE);

		// setting a default size if not specified, #191
		if (size == null || size.trim().isEmpty()) 
			size = JmolViewerServlet.DEFAULT_SIZE;

		logger.info("Requested assemblyDiagram page for jobId={}, interfaces={}, clusters={}, assembly={}",
				jobId,requestedIfacesStr,requestedClusterStr, requestedAssemblyStr);

		PrintWriter outputStream = null;

		try
		{
			AssemblyDiagramServletInputValidator.validateLatticeGraphInput(jobId, requestedIfacesStr, requestedClusterStr, requestedAssemblyStr);

			// should be no risk because validator checked for number
			int assemblyId = Integer.parseInt(requestedAssemblyStr);

			PdbInfo pdbInfo = LatticeGraphServlet.getPdbInfo(jobId);

			List<Interface> ifaceList = LatticeGraphServlet.getInterfaceList(pdbInfo);

			// TODO no interfaces data should be needed here, data should come from rest api
			// TODO at the moment input from interfaces does not work, implement
			IntervalSet requestedIntervals = LatticeGraphServlet.parseInterfaceListWithClusters(requestedIfacesStr,requestedClusterStr,ifaceList);
			Collection<Integer> requestedIfaces = requestedIntervals.getIntegerSet();

			String title = jobId + " - Assembly Diagram";
			if(requestedIfaces != null && !requestedIfaces.isEmpty()) {
				title += " for interfaces "+requestedIfacesStr;
			}

			outputStream = new PrintWriter(response.getOutputStream());

			// the json data URL from REST API
			// TODO construct from constant or property for api URL
			String jsonURL = "/rest/api/v3/job/assemblyDiagram/" + jobId + "/" + assemblyId;

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
		} catch(DaoException e) {
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
