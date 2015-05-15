package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBInfoDAOJpa;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.JmolPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.JmolViewerServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.PymolRunner;

/**
 * Servlet used to open jmol.
 * @author AS
 */
public class JmolViewerServlet extends BaseServlet
{

	private static final long serialVersionUID = 1L;

	//private static final Logger logger = LoggerFactory.getLogger(JmolViewerServlet.class);
	
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
		String jobId = request.getParameter("id");
		String interfaceId = request.getParameter("interface");
		String input = request.getParameter("input");
		String size = request.getParameter("size");
		String version = request.getParameter("version");

		String serverName = request.getServerName();
		int serverPort = request.getServerPort();

		String serverUrl = protocol + "://" + serverName + ":" + serverPort + "/" + servletContPath;

		ServletOutputStream outputStream = null;

		try
		{
			JmolViewerServletInputValidator.validateJmolViewerInput(jobId, interfaceId, input, size);

			PymolRunner pr = new PymolRunner(null);
			//pr.readColorMappingsFromResourceFile(EppicParams.PYMOL_COLOR_MAPPINGS_IS); 

			Interface interfData = getInterfaceData(jobId, Integer.parseInt(interfaceId));
			

			String jmolPage = JmolPageGenerator.generatePage(jobId + " - " + interfaceId + "\n", size, serverUrl,
					resultsLocation + jobId, input + "." + interfaceId + ".pdb", pr, version, 
					interfData);


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
		catch(DaoException e)
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
