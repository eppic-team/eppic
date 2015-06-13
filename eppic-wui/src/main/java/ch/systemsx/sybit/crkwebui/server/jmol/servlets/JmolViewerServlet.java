package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBInfoDAOJpa;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.JmolPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.JmolViewerServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
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
		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String interfaceId = request.getParameter(FileDownloadServlet.PARAM_INTERFACE_ID);
		String assemblyId = request.getParameter(FileDownloadServlet.PARAM_ASSEMBLY_ID);
		String format = request.getParameter(FileDownloadServlet.PARAM_COORDS_FORMAT);
		String input = request.getParameter(PARAM_INPUT);
		String size = request.getParameter(PARAM_SIZE);

		String serverName = request.getServerName();
		int serverPort = request.getServerPort();

		String serverUrl = protocol + "://" + serverName + ":" + serverPort + "/" + servletContPath;

		String url3dmoljs = properties.getProperty("url3dmoljs");
		if (url3dmoljs == null || url3dmoljs.equals("")) {
			logger.error("The URL for 3Dmol js is not set in config file!");
			return;
		}
		
		logger.info("Requested 3D viewer page for jobId={}, input={}, interfaceId={}, size={}",jobId,input,interfaceId,size);
		
		ServletOutputStream outputStream = null;

		try
		{
			JmolViewerServletInputValidator.validateJmolViewerInput(jobId, interfaceId, assemblyId, format, input, size);


			Interface interfData = getInterfaceData(jobId, Integer.parseInt(interfaceId));
			
			// TODO implement assembly data retrieval once assemblies are implemented (what kind of data would we need for assemblies?
			
			String extension;
			if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_PDB)) {
				extension = ".pdb";
			} else if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_CIF)) {
				extension = ".cif";
			} else {
				extension = ".cif"; // we set the default to cif just in case format is not set
			}

			String jmolPage = JmolPageGenerator.generatePage(jobId + " - " + interfaceId + "\n", 
					size, serverUrl,
					resultsLocation + jobId, 
					input + EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interfaceId + extension,   
					interfData,
					url3dmoljs);


			outputStream = response.getOutputStream();
			outputStream.println(jmolPage);
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
	
	private Interface getInterfaceData(String jobId, int interfaceId) throws DaoException {
		
		List<Integer> interfaceIdList = new ArrayList<Integer>();
		interfaceIdList.add(interfaceId);
		

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
				
		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
		Interface interf = interfaceDAO.getInterfaceWithResidues(pdbInfo.getUid(), interfaceId);
		

		return interf;
	}
}
