package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.nbio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBInfoDAOJpa;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.LatticeGraphPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.LatticeGraphServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.EppicParams;
import eppic.assembly.gui.LatticeGUI3Dmol;

/**
 * Servlet used to display a LatticeGraph3Dmol page.
 * 
 * The following are the valid values for the parameters:
 * <pre>
 * 
 * Parameter name 					Parameter value
 * --------------					---------------
 * id								String (the jobId hash)
 * input							Name of input file within the jobId directory
 * 									(Note this differs from JmolViewer, which strips the extension)
 * interfaces						String (comma-separated list of interface ids)

 * @author Spencer Bliven
 */
public class LatticeGraphServlet extends BaseServlet
{

	private static final long serialVersionUID = 1L;

	/**
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "latticeGraph";

	public static final String PARAM_INTERFACES = "interfaces";

	private static final Logger logger = LoggerFactory.getLogger(LatticeGraphServlet.class);

	private String resultsLocation;
	private String protocol;
	//private String generalDestinationDirectoryName;
	private String servletContPath;
	private String destination_path;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		resultsLocation = properties.getProperty("results_location");
		destination_path = properties.getProperty("destination_path");

		protocol = "http";
		if(properties.getProperty("protocol") != null)
		{
			protocol = properties.getProperty("protocol");
		}

		//generalDestinationDirectoryName = properties.getProperty("destination_path");

		servletContPath = getServletContext().getInitParameter("servletContPath");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		//TODO add type=interface/assembly as parameter, so that assemblies can also be supported


		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String input = request.getParameter(JmolViewerServlet.PARAM_INPUT);
		String requestedIfacesStr = request.getParameter(PARAM_INTERFACES);

		String serverName = request.getServerName();
		int serverPort = request.getServerPort();

		String serverUrl = protocol + "://" + serverName + ":" + serverPort + "/" + servletContPath;

		String url3dmoljs = properties.getProperty("url3dmoljs");
		if (url3dmoljs == null || url3dmoljs.equals("")) {
			logger.error("The URL for 3Dmol js is not set in config file!");
			return;
		}

		logger.info("Requested 3D viewer page for jobId={}, input={},interfaces={}, size={}",jobId,input,requestedIfacesStr);

		PrintWriter outputStream = null;

		try
		{
			LatticeGraphServletInputValidator.validateLatticeGraphInput(jobId, input,requestedIfacesStr);

			String dir = destination_path + jobId + "/";
			String inputFilename = dir + input;
			
			// Construct UC filename
			String inputPrefix = input.replaceAll("\\.(cif|pdb)(\\.gz)?$", "");
			String ucFilename = dir+inputPrefix + EppicParams.UNIT_CELL_COORD_FILES_SUFFIX + ".cif";
			String ucURI = resultsLocation + jobId + "/" + inputPrefix + EppicParams.UNIT_CELL_COORD_FILES_SUFFIX + ".cif";
			
			//TODO better to filter interfaces here before construction, or afterwards?
			List<Integer> requestedIfaces = LatticeGUI3Dmol.parseInterfaceList(requestedIfacesStr);
			//requestedIfaces = null; //Filter afterwards

			List<Interface> ifaceList = getInterfaceList(jobId);
			String title = jobId + " - Lattice Graph";
			if(requestedIfaces != null && !requestedIfaces.isEmpty()) {
				title += " for interfaces "+requestedIfacesStr;
			}

			outputStream = new PrintWriter(response.getOutputStream());
			
			LatticeGraphPageGenerator.generatePage(inputFilename, ucFilename, ucURI, title, ifaceList, requestedIfaces, url3dmoljs,outputStream);

		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error during preparation of 3D viewer page.");
		}
		catch(DaoException e)
		{
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error during preparation of 3D viewer page.");
		} catch (StructureException e) {
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error during preparation of 3D viewer page.");
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

	private List<Interface> getInterfaceList(String jobId) throws DaoException {
		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
			return interfaceDAO.getAllInterfaces(pdbInfo.getUid());
	}
}
