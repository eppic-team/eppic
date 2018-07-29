package ch.systemsx.sybit.crkwebui.server.jmol.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.nbio.structure.align.util.AtomCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.LatticeGraphPageGenerator;
import ch.systemsx.sybit.crkwebui.server.jmol.validators.LatticeGraphServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import eppic.model.dto.PdbInfo;
import eppic.assembly.gui.LatticeGUI;
import eppic.db.dao.DaoException;
import eppic.db.dao.JobDAO;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.jpa.JobDAOJpa;
import eppic.db.dao.jpa.PDBInfoDAOJpa;
import eppic.model.db.JobDB;

/**
 * Servlet used to display a LatticeGraph3Dmol page.
 * 
 * The following are the valid values for the parameters:
 * <pre>
 * 
 * Parameter name 					Parameter value
 * --------------					---------------
 * id								String (the jobId hash)
 * assembly							String (the eppic assembly id)
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
	public static final String PARAM_ASSEMBLY = "assembly";
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

		String jobId = request.getParameter(FileDownloadServlet.PARAM_ID);
		String requestedIfacesStr = request.getParameter(PARAM_INTERFACES);
		String requestedClusterStr = request.getParameter(PARAM_CLUSTERS);
		String requestedAssemblyStr = request.getParameter(LatticeGraphServlet.PARAM_ASSEMBLY);
		String size = request.getParameter(JmolViewerServlet.PARAM_SIZE);

		// setting a default size if not specified, #191
		if (size == null || size.trim().isEmpty()) 
			size = JmolViewerServlet.DEFAULT_SIZE;


		logger.info("Requested Lattice Graph page for jobId={}, interfaces={}, clusters={}, assembly={}",
				jobId,requestedIfacesStr,requestedClusterStr, requestedAssemblyStr);

		PrintWriter outputStream = null;

		try
		{
			LatticeGraphServletInputValidator.validateLatticeGraphInput(jobId,requestedIfacesStr,requestedClusterStr, requestedAssemblyStr);

			PdbInfo pdbInfo = getPdbInfo(jobId);
			String input = pdbInfo.getInputName();

			// job directory on local filesystem
			File dir = DirLocatorUtil.getJobDir(new File(destination_path), jobId);

			// Construct filename for AU cif file
			File auFile = getAuFileName(dir, input, atomCachePath);
			
			String inputFileNameNoGz = auFile.getName();
			if (auFile.getName().endsWith(".gz")) {
				inputFileNameNoGz = auFile.getName().substring(0, auFile.getName().lastIndexOf(".gz"));
			} else if (auFile.getName().endsWith(".GZ")) {
				inputFileNameNoGz = auFile.getName().substring(0, auFile.getName().lastIndexOf(".GZ"));
					
			}
			// the URL has no gz at the end because it's served as plain text via content-encoding: gzip
			String auURI = DirLocatorUtil.getJobUrlPath(resultsLocation, jobId) + "/" + inputFileNameNoGz;

			outputStream = new PrintWriter(response.getOutputStream());
			//String molviewerurl = properties.getProperty("urlNglJs");

			String nglJsUrl = properties.getProperty("urlNglJs");
			if (nglJsUrl == null || nglJsUrl.equals("")) {
				logger.warn("The URL for NGL js is not set in property 'urlNglJs' in config file! NGL won't work.");
			}

			// the json data URL from REST API
			// TODO construct from constant or property for api URL
            String REST_PREFIX = "/rest/api/v3/job";

			String jsonURL = null;
            String title = jobId + " - Lattice Graph";

            if (requestedAssemblyStr != null) {
                // should be no risk because validator checked for number
                int assemblyId = Integer.parseInt(requestedAssemblyStr);
                jsonURL = REST_PREFIX + "/latticeGraph/" + jobId + "/" + assemblyId;
                title += " for assembly " + requestedAssemblyStr;
            }

			if (requestedIfacesStr != null) {
			    jsonURL = REST_PREFIX + "/latticeGraphByInterfaceIds/" + jobId + "/" + requestedIfacesStr;
			    title += " for interfaces " + requestedIfacesStr;
            }

            if (requestedClusterStr != null) {
                jsonURL = REST_PREFIX + "/latticeGraphByInterfaceClusterIds/" + jobId + "/" + requestedClusterStr;
                title += " for interface clusters " + requestedClusterStr;
            }

			String webappRoot = request.getContextPath();
			LatticeGraphPageGenerator.generateHTMLPage(auURI, title, size, jsonURL, outputStream, nglJsUrl, webappRoot);

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
	
	static PdbInfo getPdbInfo(String jobId) throws DaoException {
		PDBInfoDAO pdbDao = new PDBInfoDAOJpa();
		PdbInfo pdbinfo = pdbDao.getPDBInfo(jobId);
		// Set additional job properties
		JobDAO jobdao = new JobDAOJpa();
		JobDB job = jobdao.getJob(jobId);
		pdbinfo.setInputName(job.getInputName());
		pdbinfo.setInputType(job.getInputType());
		return pdbinfo;
	}

	/**
	 * Returns the file name of the input structure: if user job it returns 
	 * the path to the file in the job dir, if precomputed it finds the file path
	 * in the local atom cache and creates a symbolic link to it in the job dir
	 * @param directory Directory to search for the file
	 * @param inputName either the input file name, or a PDB code
	 * @param atomCachePath Path for downloaded CIF files
	 * @return the path to the AU structure file in the job dir
	 * @throws IOException if file is a user job file and can't be found, 
	 * or if the input is a pdb id and its file can't be found in atom cache
	 */
	public static File getAuFileName(File directory, String inputName, String atomCachePath) throws IOException {
		
		// inputName will be the full file name in user jobs and the pdbId for precomputed jobs
		File structFile = new File(directory,inputName);
		logger.info("Trying to find the structure file for input name '{}'. Searching file in {}", inputName, structFile.toString());

		if(!structFile.exists()){
			if  (!inputName.matches("^\\d\\w\\w\\w$")) {
				throw new IOException(String.format(
						"Could not find file %s and the inputName '%s' does not look "
								+ "like a PDB id. Can't produce the lattice graph page!",
								structFile, inputName));
			} else {
				
				// it is like a PDB id, leave it to AtomCache
				AtomCache atomCache = null;
				if (atomCachePath ==null) {
					atomCache = new AtomCache();
					logger.warn("Defaulting to downloading structures to {}. Please set the ATOM_CACHE_PATH property.",atomCache.getCachePath());
				}
				else {
					atomCache = new AtomCache(atomCachePath);
				}

				atomCache.setUseMmCif(true);

				structFile = LatticeGUI.getFile(atomCache, inputName);
				
				if (structFile == null || !structFile.exists()) {
					logger.error("The structure file {} does not exist in atom cache! Will not be able to display lattice graph", structFile);
					throw new IOException("Structure file " + structFile+" does not exist in atom cache");
				}

				File sLink = new File(directory, structFile.getName());
				if (!sLink.exists()) {
					
					// TODO downloading symlinks don't work with current server configuration, 
					// TODO for now resorting to copy: must fix server config and do it with symlinks!
					
					logger.info("Copying to {} from file {}", sLink.toString(), structFile.toString());
					Files.copy(structFile.toPath(), sLink.toPath() );
					
					// we create a symbolic link to the file in the atomcache dir
					//logger.info("Creating symbolic link {} to file {}", sLink.toString(), structFile.toString());
					//Files.createSymbolicLink(sLink.toPath(), structFile.toPath());
				}
				// and finally if no exception is thrown we return the symbolic link
				structFile = sLink;
			}
		}
		
		return structFile;
	}
}
