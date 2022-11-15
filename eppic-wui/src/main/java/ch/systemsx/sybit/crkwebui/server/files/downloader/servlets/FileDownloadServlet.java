package ch.systemsx.sybit.crkwebui.server.files.downloader.servlets;

import java.io.File;
import java.io.IOException;

import javax.persistence.NoResultException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.servlets.util.RequestUtil;
import ch.systemsx.sybit.crkwebui.server.commons.servlets.util.ResponseUtil;
import ch.systemsx.sybit.crkwebui.server.commons.util.http.ContentTypeGenerator;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirLocatorUtil;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirectoryContentReader;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.FileToDownloadNameGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.FileToDownloadNameSuffixGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileDownloadServletInputValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileToDownloadLocationValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileToDownloadNameSuffixValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileToDownloadValidator;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.LatticeGraphServlet;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import eppic.CoordFilesAdaptor;
import eppic.db.dao.*;
import eppic.db.dao.mongo.JobDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.mongoutils.MongoDbStore;
import eppic.model.db.PdbInfoDB;
import eppic.model.dto.InputWithType;

/**
 * Servlet used to download files stored in the server.
 * 
 * The following are the valid values for the parameters:
 * <pre>
 * 
 * Parameter name 					Parameter value
 * --------------					---------------
 * type								"interface", "assembly", "msa" (was "fasta"), "entropiespse" ("pse","zip" now removed)
 * id								int (the jobId)
 * assemblyId						int (with type="assembly")
 * interfaceId (was interface)		int (with type="interface")
 * repChainId (was alignment)		String (the representative chain id) (with type="msa")
 * coordsFormat						"pdb", "cif", "pdb.gz", "cif.gz", "pse" (with type="interface","assembly")
 * 
 * </pre>
 * 
 * @author srebniak_a 
 */
public class FileDownloadServlet extends BaseServlet 
{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "fileDownload";

	/**
	 * The type of file requested.
	 * Valid values are interface, assembly, msa (was fasta), entropiespse.
	 * "pse" used to be valid value, now in {@link #PARAM_COORDS_FORMAT}
	 */
	public static final String PARAM_TYPE = "type";
	
	/**
	 * The job identifier (PDB code or long alphanumerical String)
	 */
	public static final String PARAM_ID = "id";
	
	/**
	 * The interface identifier (int)
	 */
	public static final String PARAM_INTERFACE_ID = "interfaceId"; // was "interface"
	
	/**
	 * The assembly identifier (int), to be used with {@value #PARAM_TYPE}="msa"
	 */
	public static final String PARAM_ASSEMBLY_ID = "assemblyId";
	
	/**
	 * The representative chain identifier (a String)
	 */
	public static final String PARAM_REP_CHAIN_ID = "repChainId"; // was "alignment"
	
	/**
	 * The file format for the coordinate files requested with {@value #PARAM_TYPE}="interface","assembly"
	 * Valid values are {@value #COORDS_FORMAT_VALUE_CIF}, {@value #COORDS_FORMAT_VALUE_PDB}
	 */
	public static final String PARAM_COORDS_FORMAT = "coordsFormat";
	
	
	// the values for PARAM_TYPE
	public static final String TYPE_VALUE_INTERFACE = "interface";
	public static final String TYPE_VALUE_ASSEMBLY = "assembly";
	public static final String TYPE_VALUE_MSA = "msa"; // was "fasta"

	// the values for PARAM_COORDS_FORMAT
	public static final String COORDS_FORMAT_VALUE_PDB = "pdb";
	public static final String COORDS_FORMAT_VALUE_CIF = "cif";

	
	
	
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * Returns file specified by the parameters.
	 */
	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response) throws ServletException, IOException
	{
		boolean acceptGzipEncoding = RequestUtil.checkIfAcceptGzipEncoding(request);
		
		String type = request.getParameter(PARAM_TYPE);
		String jobId = request.getParameter(PARAM_ID);
		String interfaceId = request.getParameter(PARAM_INTERFACE_ID);
		String assemblyId = request.getParameter(PARAM_ASSEMBLY_ID);
		String repChainId = request.getParameter(PARAM_REP_CHAIN_ID);
		String format = request.getParameter(PARAM_COORDS_FORMAT);

		try
		{
			FileDownloadServletInputValidator.validateFileDownloadInput(type, jobId, interfaceId, assemblyId, repChainId, format);
			
			File fileToDownloadLocation = DirLocatorUtil.getJobDir(new File(properties.getProperty("destination_path")), jobId);
			FileToDownloadLocationValidator.validateLocation(fileToDownloadLocation);
			
			String suffix = FileToDownloadNameSuffixGenerator.generateFileNameSuffix(type, jobId, interfaceId, assemblyId, repChainId, format);
			FileToDownloadNameSuffixValidator.validateSuffix(suffix);
			
			boolean isContentGzipped = ResponseUtil.checkIfDoGzipEncoding(acceptGzipEncoding, suffix);

			if (type.equals(TYPE_VALUE_ASSEMBLY) || type.equals(TYPE_VALUE_INTERFACE)) {
			    // TODO at the moment PDB format is not supported, we could try a best-effort implementation at some point (https://github.com/eppic-team/eppic/issues/185)
				produceCoordsFileResponse(response, type, jobId, interfaceId, assemblyId, fileToDownloadLocation);
				return;
			}

			File fileToDownload = DirectoryContentReader.getFileFromDirectoryWithSpecifiedSuffix(fileToDownloadLocation, suffix);
			FileToDownloadValidator.validateFile(fileToDownload);

			prepareResponse(response, fileToDownload.getName(), jobId, isContentGzipped);
			ResponseUtil.printFileContentToOutput(fileToDownload, response);
		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error while trying to download the file: " + e.getMessage());
		}
		catch(DaoException e) {
			if (e.getCause() instanceof NoResultException) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Error while trying to download the file: " + e.getMessage());
			} else {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while trying to download the file: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Sets header and content type of response.
	 * @param response response to update
	 * @param resultFileName name of the file to donwload
	 * @param jobId identiifer of the job
	 * @param isContentGzipped flag pointing whether compressed content is provided
	 */
	private void prepareResponse(HttpServletResponse response,
								 String resultFileName,
								 String jobId,
								 boolean isContentGzipped)
	{
		String contentType = ContentTypeGenerator.generateContentTypeByFileExtension(resultFileName);
		response.setContentType(contentType);
		
		String processedFileName = FileToDownloadNameGenerator.generateNameOfTheFileToDownload(resultFileName, jobId);
		
		//remove gz
		if(isContentGzipped)
		{
			processedFileName = processedFileName.substring(0, processedFileName.length() - 3);
			response.setHeader("Content-Encoding", "gzip");
		}
		
		response.setHeader("Content-Disposition", "attachment; filename=\"" + processedFileName + "\"");
	}

	private void produceCoordsFileResponse(HttpServletResponse response, String type, String jobId, String interfaceId, String assemblyId, File jobDir) throws DaoException, ValidationException, IOException {

		// prepare response
		response.setContentType("chemical/x-cif");
		// Important: we must NOT set gzip content-encoding here, jetty configuration takes care of that
		//response.setHeader("Content-Encoding", "gzip");

		// Otherwise with explicit gzip content-encoding here, tools like 'curl --compressed' complain
		// At the same time, wget (wget --header="accept-encoding: gzip") behaves weirdly without the explicit gzip
		// content-encoding here, downloading file but returning a compressed file
		// The wget problem seems to be a weird implementation by wget. Firefox, chrome and curl all behave well
		// downloading to an uncompressed file (with transmission compressed).

		String outputFileName = jobId + "." + type + "." + ((assemblyId==null)?interfaceId:assemblyId) + ".cif";
		response.setHeader("Content-Disposition", "attachment; filename=\"" + outputFileName + "\"");


		// get data and produce file
		PDBInfoDAO dao = new PDBInfoDAOMongo(MongoDbStore.getMongoDb());
		PdbInfoDB pdbInfo = dao.getPDBInfo(jobId);

		JobDAO jobDAO = new JobDAOMongo();
		InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

		File auFile = LatticeGraphServlet.getAuFileName(jobDir, input.getInputName());

		CoordFilesAdaptor adaptor = new CoordFilesAdaptor();

		if (type.equals(TYPE_VALUE_ASSEMBLY)) {
			adaptor.getAssemblyCoordsMmcif(jobId, auFile, response.getOutputStream(), pdbInfo, Integer.parseInt(assemblyId), true);
		} else if (type.equals(TYPE_VALUE_INTERFACE)) {
			adaptor.getInterfaceCoordsMmcif(jobId, auFile, response.getOutputStream(), pdbInfo, Integer.parseInt(interfaceId), true);
		} else {
			// should not happen, the validation took care of this
			throw new ValidationException("Unsupported file type " + type);
		}

	}
}
