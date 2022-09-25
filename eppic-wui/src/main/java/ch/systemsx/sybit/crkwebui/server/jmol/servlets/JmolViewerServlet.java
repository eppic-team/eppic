package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.model.db.AssemblyDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.JmolPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.JmolViewerServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import eppic.EppicParams;
import eppic.db.dao.DaoException;
import eppic.db.dao.PDBInfoDAO;

/**
 * Servlet used to open the 3D viewer for interfaces and assemblies.
 * @author AS
 */
public class JmolViewerServlet extends BaseServlet
{

	private static final long serialVersionUID = 1L;
	
	/**
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "jmolViewer";
	
	public static final String PARAM_INPUT = "input";
	public static final String PARAM_SIZE = "size";
	
	public static final String DEFAULT_SIZE = "700";
	
	private static final Logger logger = LoggerFactory.getLogger(JmolViewerServlet.class);
	
	private String protocol;
	

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		protocol = "http";
		if(properties.getProperty("protocol") != null)
		{
			protocol = properties.getProperty("protocol");
		}

		//generalDestinationDirectoryName = properties.getProperty("destination_path");

	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		
		//TODO add type=interface/assembly as parameter, so that assemblies can also be supported
		
		
		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String type = request.getParameter(FileDownloadServlet.PARAM_TYPE);
		String interfaceId = request.getParameter(FileDownloadServlet.PARAM_INTERFACE_ID);
		String assemblyId = request.getParameter(FileDownloadServlet.PARAM_ASSEMBLY_ID);
		String format = request.getParameter(FileDownloadServlet.PARAM_COORDS_FORMAT);
		String input = request.getParameter(PARAM_INPUT);
		String size = request.getParameter(PARAM_SIZE);
		
		// setting a default size if not specified, #191
		if (size == null || size.trim().isEmpty()) 
			size = JmolViewerServlet.DEFAULT_SIZE;


		String serverName = request.getServerName();

		// it looks like this gives 80 even when in https... so I've commented out
		// Anyway it would only be useful if serving on a non-standard port
		//int serverPort = request.getServerPort();

		String serverUrl = protocol + "://" + serverName;// + ":" + serverPort;

		String nglJsUrl = properties.getProperty("urlNglJs");
		if (nglJsUrl == null || nglJsUrl.equals("")) {
			logger.warn("The URL for NGL js is not set in property 'urlNglJs' in config file! NGL won't work.");
		}
		
		logger.info("Requested 3D viewer page for jobId={}, input={}, interfaceId={}, assemblyId={}, size={}",jobId,input,interfaceId,assemblyId,size);

		try( PrintWriter outputStream = new PrintWriter(response.getOutputStream());)
		{
			JmolViewerServletInputValidator.validateJmolViewerInput(jobId, type, interfaceId, assemblyId, format, input, size);


			String extension;
			if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_PDB)) {
				extension = ".pdb";
			} else if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_CIF)) {
				extension = ".cif";
			} else {
				// the validator shouldn't allow this case, but anyway we set a default
				extension = ".cif"; 
			}
			
			InterfaceDB interfData;
			
			AssemblyDB assemblyData;
			
			String fileName;
			
			String title;
			
			if (type.equals(FileDownloadServlet.TYPE_VALUE_INTERFACE)) {
				fileName = input + EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interfaceId + extension;
				interfData = getInterfaceData(jobId, Integer.parseInt(interfaceId));
				assemblyData = null;
				title = jobId + " - Interface " + interfaceId;
				
			} else if (type.equals(FileDownloadServlet.TYPE_VALUE_ASSEMBLY)) {
				fileName = input + EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX+ "." + assemblyId + extension;
				title = jobId + " - Assembly " + assemblyId;
				// interfdata would have to be null in this case
				interfData = null;
				assemblyData = getAssemblyData(jobId, Integer.parseInt(assemblyId));
				
				
			} else {
				// the validator shouldn't allow this case, but anyway let's set a default 
				fileName = input + EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interfaceId + extension;
				interfData = getInterfaceData(jobId, Integer.parseInt(interfaceId));
				assemblyData = null;
				title = jobId + " - Interface " + interfaceId;
			}

			String webappRoot = request.getContextPath();
			
			JmolPageGenerator.generatePage(title, 
					size, jobId, serverUrl,
					fileName,   
					interfData,
					assemblyData,
					nglJsUrl,outputStream, webappRoot);
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
		}
	}
	
	private InterfaceDB getInterfaceData(String jobId, int interfaceId) throws DaoException {
		
		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
		PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
				
		InterfaceDB interf = pdbInfo.getInterface(interfaceId);

		return interf;
	}

	private AssemblyDB getAssemblyData(String jobId, int assemblyId) throws DaoException {

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOMongo();
		PdbInfoDB pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
		
		return pdbInfo.getAssemblyById(assemblyId);
	}
}
