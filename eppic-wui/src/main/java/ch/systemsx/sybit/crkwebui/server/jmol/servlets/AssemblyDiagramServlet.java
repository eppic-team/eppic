package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.nbio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.AssemblyDiagramPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.AssemblyDiagramServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

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

	public static final String PARAM_INTERFACES = "interfaces";
	public static final String PARAM_CLUSTERS = "clusters";
	//public static final String NGL_URL = "https://rawgit.com/sbliven/ngl/master/js/build/ngl.embedded.js";
	//public static final String VIS_JS_URL = "https://cdnjs.cloudflare.com/ajax/libs/vis/4.9.0/vis.min.js";

	private static final Logger logger = LoggerFactory.getLogger(AssemblyDiagramServlet.class);

	private String resultsLocation;
	private String destination_path;
	
	private String atomCachePath;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		resultsLocation = properties.getProperty("results_location");
		destination_path = properties.getProperty("destination_path");
		atomCachePath = propertiesCli.getProperty("ATOM_CACHE_PATH");
		
		if (atomCachePath == null) 
			logger.warn("ATOM_CACHE_PATH is not set in config file, will not be able to reuse cache for PDB cif.gz files!");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		//TODO add type=interface/assembly as parameter, so that assemblies can also be supported


		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String requestedIfacesStr = request.getParameter(PARAM_INTERFACES);
		String requestedClusterStr = request.getParameter(PARAM_CLUSTERS);
		String size = request.getParameter(JmolViewerServlet.PARAM_SIZE);

		String url3dmoljs = properties.getProperty("url3dmoljs");
		if (url3dmoljs == null || url3dmoljs.equals("")) {
			logger.info("The URL for 3Dmol js is not set in config file. Will use the js file from eppic");
			url3dmoljs = "3Dmol-min.js"; //we set it to the js file within eppic
		}

		logger.info("Requested assemblyDiagram page for jobId={},interfaces={},clusters={}",jobId,requestedIfacesStr,requestedClusterStr);

		PrintWriter outputStream = null;

		try
		{
			AssemblyDiagramServletInputValidator.validateLatticeGraphInput(jobId,requestedIfacesStr,requestedClusterStr);

			PdbInfo pdbInfo = LatticeGraphServlet.getPdbInfo(jobId);
			String input = pdbInfo.getInputName();

			// job directory on local filesystem
			File dir = DirLocatorUtil.getJobDir(new File(destination_path), jobId);

			List<Interface> ifaceList = LatticeGraphServlet.getInterfaceList(pdbInfo);

			//TODO better to filter interfaces here before construction, or afterwards?
			Collection<Integer> requestedIfaces = LatticeGraphServlet.parseInterfaceListWithClusters(requestedIfacesStr,requestedClusterStr,ifaceList);;

			String title = jobId + " - Assembly Diagram";
			if(requestedIfaces != null && !requestedIfaces.isEmpty()) {
				title += " for interfaces "+requestedIfacesStr;
			}


			outputStream = new PrintWriter(response.getOutputStream());

			AssemblyDiagramPageGenerator.generateHTMLPage(dir,input, atomCachePath, title, size, ifaceList, requestedIfaces,outputStream);

			// TODO start generating JSON and unit cell already

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
		} catch (StructureException e) {
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
