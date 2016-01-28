package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.nbio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
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
 * interfaces						String (comma-separated list of interface ids)
 * clusters							String (comma-separated list of interface cluster ids). Superseded by interfaces.
 *
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
	public static final String PARAM_CLUSTERS = "clusters";

	private static final Logger logger = LoggerFactory.getLogger(LatticeGraphServlet.class);

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

		logger.info("Requested Lattice Graph page for jobId={},interfaces={},clusters={}",jobId,requestedIfacesStr,requestedClusterStr);

		PrintWriter outputStream = null;

		try
		{
			LatticeGraphServletInputValidator.validateLatticeGraphInput(jobId,requestedIfacesStr,requestedClusterStr);

			PdbInfo pdbInfo = getPdbInfo(jobId);
			String input = pdbInfo.getInputName();
			String inputPrefix = pdbInfo.getTruncatedInputName();

			// job directory on local filesystem
			File dir = DirLocatorUtil.getJobDir(new File(destination_path), jobId);

			// Construct UC filename
			File ucFile = new File(dir,inputPrefix + EppicParams.UNIT_CELL_COORD_FILES_SUFFIX + ".cif.gz");
			String ucURI = DirLocatorUtil.getJobUrlPath(resultsLocation, jobId) + "/" + inputPrefix + EppicParams.UNIT_CELL_COORD_FILES_SUFFIX + ".cif";

			List<Interface> ifaceList = getInterfaceList(pdbInfo);

			//TODO better to filter interfaces here before construction, or afterwards?
			Collection<Integer> requestedIfaces = parseInterfaceListWithClusters(requestedIfacesStr,requestedClusterStr,ifaceList);;

			String title = jobId + " - Lattice Graph";
			if(requestedIfaces != null && !requestedIfaces.isEmpty()) {
				title += " for interfaces "+requestedIfacesStr;
			}


			outputStream = new PrintWriter(response.getOutputStream());

			LatticeGraphPageGenerator.generatePage(dir,input, atomCachePath, ucFile, ucURI, title, size, ifaceList, requestedIfaces, url3dmoljs,outputStream);

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
	
	/**
	 * Combines a list of clusters and a list of interfaces, taking the union.
	 * @param requestedIfacesStr
	 * @param requestedClusterStr
	 * @param ifaceList
	 * @return
	 */
	private static Collection<Integer> parseInterfaceListWithClusters(
			String ifaceStr, String clusterStr,
			List<Interface> ifaceList) {
		// If one of interfaces and clusters is specified, return it
		// If either are '*', return null (all)
		// If both are specified, return their union
		if( ifaceStr == null || ifaceStr.isEmpty() ) {
			if(clusterStr == null || clusterStr.isEmpty()) {
				// If neither are specified, return null (all)
				return null;
			}
			// Only clusters specified
			if(clusterStr.equals('*'))
				return null;
			List<Integer> clusterList = LatticeGUI3Dmol.parseInterfaceList(clusterStr);
			return mapClusters(new HashSet<Integer>(clusterList),ifaceList);
		} else {
			if(clusterStr == null || clusterStr.isEmpty()) {
				// Only interfaces specified
				return LatticeGUI3Dmol.parseInterfaceList(ifaceStr);
			}
			// Both specified
			if(ifaceStr.equals('*') || clusterStr.equals('*') )
				return null;
			Set<Integer> interfaces = mapClusters(new HashSet<Integer>(LatticeGUI3Dmol.parseInterfaceList(ifaceStr)),ifaceList);
			interfaces.addAll(LatticeGUI3Dmol.parseInterfaceList(ifaceStr));
			return interfaces;
		}
	}
	/**
	 * Expand a list of interface cluster numbers to a full list of interfaces
	 * @param clusters
	 * @param ifaceList
	 * @return
	 */
	private static Set<Integer> mapClusters(Set<Integer> clusters,
			Collection<Interface> ifaceList) {
		Set<Integer> interfaces = new HashSet<Integer>();
		for(Interface iface : ifaceList) {
			if(clusters.contains(iface.getClusterId())) {
				// Only interfaces specified
				interfaces.add(iface.getInterfaceId());
			}
		}
		return interfaces;
	}
}
