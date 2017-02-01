package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
import ch.systemsx.sybit.crkwebui.server.db.dao.AssemblyDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.AssemblyDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBInfoDAOJpa;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.JmolPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.JmolViewerServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.EppicParams;

/**
 * Servlet used to open jmol.
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
	
	private static final Logger logger = LoggerFactory.getLogger(JmolViewerServlet.class);
	
	private String resultsLocation;
	private String protocol;
	//private String generalDestinationDirectoryName;
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

		//generalDestinationDirectoryName = properties.getProperty("destination_path");

		servletContPath = getServletContext().getInitParameter("servletContPath");
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

		String serverName = request.getServerName();
		int serverPort = request.getServerPort();

		String serverUrl = protocol + "://" + serverName + ":" + serverPort + "/" + servletContPath;

		String nglJsUrl = properties.getProperty("urlNglJs");
		if (nglJsUrl == null || nglJsUrl.equals("")) {
			logger.warn("The URL for NGL js is not set in property 'urlNglJs' in config file! NGL won't work.");
		}
		
		logger.info("Requested 3D viewer page for jobId={}, input={}, interfaceId={}, size={}",jobId,input,interfaceId,size);

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
			
			Interface interfData;
			
			Assembly assemblyData;
			
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
					size, serverUrl,
					DirLocatorUtil.getJobUrlPath(resultsLocation, jobId), 
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
	
	private Interface getInterfaceData(String jobId, int interfaceId) throws DaoException {
		
		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
				
		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
		Interface interf = interfaceDAO.getInterfaceWithResidues(pdbInfo.getUid(), interfaceId);
		

		return interf;
	}

	private Assembly getAssemblyData(String jobId, int assemblyId) throws DaoException {

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

		AssemblyDAO assemblyDAO = new AssemblyDAOJpa();
		
		List<Assembly> assemblies = assemblyDAO.getAssemblies(pdbInfo.getUid());
		
		Assembly assembly = null;
		for (Assembly a:assemblies) {
			if (a.getId() == assemblyId) assembly = a;
		}
		
		return assembly;
		
	}
}
