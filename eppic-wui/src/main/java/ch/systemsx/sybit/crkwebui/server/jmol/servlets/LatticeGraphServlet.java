package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.File;
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
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
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
import eppic.model.JobDB;

/**
 * Servlet used to display a LatticeGraph3Dmol page.
 * 
 * The following are the valid values for the parameters:
 * <pre>
 * 
 * Parameter name 					Parameter value
 * --------------					---------------
 * id								String (the jobId hash)
 * input							The job's inputName (Name of input file within the jobId directory),
 * 									or else the PDB ID for precalculated jobs
 * 									(Note this differs from JmolViewer, which uses the truncated inputName)
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
	private String destination_path;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		resultsLocation = properties.getProperty("results_location");
		destination_path = properties.getProperty("destination_path");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		//TODO add type=interface/assembly as parameter, so that assemblies can also be supported


		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String requestedIfacesStr = request.getParameter(PARAM_INTERFACES);

		String url3dmoljs = properties.getProperty("url3dmoljs");
		if (url3dmoljs == null || url3dmoljs.equals("")) {
			logger.error("The URL for 3Dmol js is not set in config file!");
			return;
		}

		logger.info("Requested Lattice Graph page for jobId={},interfaces={}",jobId,requestedIfacesStr);

		PrintWriter outputStream = null;

		try
		{
			LatticeGraphServletInputValidator.validateLatticeGraphInput(jobId,requestedIfacesStr);

			PdbInfo pdbInfo = getPdbInfo(jobId);
			String input = pdbInfo.getInputName();
			String inputPrefix = pdbInfo.getTruncatedInputName();
			
			// job directory on local filesystem
			File dir = new File(destination_path + jobId);
			
			// Construct UC filename
			File ucFile = new File(dir,inputPrefix + EppicParams.UNIT_CELL_COORD_FILES_SUFFIX + ".cif.gz");
			String ucURI = resultsLocation + jobId + "/" + inputPrefix + EppicParams.UNIT_CELL_COORD_FILES_SUFFIX + ".cif";
			
			//TODO better to filter interfaces here before construction, or afterwards?
			List<Integer> requestedIfaces = LatticeGUI3Dmol.parseInterfaceList(requestedIfacesStr);
			//requestedIfaces = null; //Filter afterwards

			List<Interface> ifaceList = getInterfaceList(pdbInfo);
			String title = jobId + " - Lattice Graph";
			if(requestedIfaces != null && !requestedIfaces.isEmpty()) {
				title += " for interfaces "+requestedIfacesStr;
			}

			outputStream = new PrintWriter(response.getOutputStream());
			
			LatticeGraphPageGenerator.generatePage(dir,input, ucFile, ucURI, title, ifaceList, requestedIfaces, url3dmoljs,outputStream);

		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during preparation of Lattice Graph page.");
			logger.error("Error during preparation of Lattice Graph page.",e);
		} catch(DaoException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during preparation of Lattice Graph page.");
			logger.error("Error during preparation of Lattice Graph page.",e);
		} catch (StructureException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during preparation of Lattice Graph page.");
			logger.error("Error during preparation of Lattice Graph page.",e);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during preparation of Lattice Graph page.");
			logger.error("Error during preparation of Lattice Graph page.",e);
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
	
	private PdbInfo getPdbInfo(String jobId) throws DaoException {
		PDBInfoDAO pdbDao = new PDBInfoDAOJpa();
		PdbInfo pdbinfo = pdbDao.getPDBInfo(jobId);
		// Set additional job properties
		JobDAO jobdao = new JobDAOJpa();
		JobDB job = jobdao.getJob(jobId);
		pdbinfo.setInputName(job.getInputName());
		pdbinfo.setInputType(job.getInputType());
		return pdbinfo;
	}

	private List<Interface> getInterfaceList(PdbInfo pdbInfo) throws DaoException {
		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
			return interfaceDAO.getAllInterfaces(pdbInfo.getUid());
	}
}
