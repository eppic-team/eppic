package ch.systemsx.sybit.crkwebui.server.files.downloader.generators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import eppic.EppicParams;


/**
 * This class is used to create suffix of the file to download.
 */
public class FileToDownloadNameSuffixGenerator 
{

	private static final Logger logger = LoggerFactory.getLogger(FileToDownloadNameSuffixGenerator.class);
	
	/**
	 * Creates file suffix for specified input parameters (with compression).
	 * @param type type of the file
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param assemblyId assembly identifier
	 * @param repChainId alignment identifier
	 * @param format
	 * @return suffix of the file
	 */
	public static String generateFileNameSuffix(String type,
												String jobId,
												String interfaceId,
												String assemblyId,
												String repChainId,
												String format)
	{
		String suffix = generateFileNameSuffixWithoutCompression(type, jobId, interfaceId, assemblyId, repChainId, format);
		
		if(suffix != null)
		{
			if((suffix.endsWith(".pdb")) ||
			   (suffix.endsWith(".cif")) ||
			   (suffix.endsWith(".pse")) )
			{
				suffix = suffix + ".gz";
			}
		}
		
		return suffix;
	}
	
	/**
	 * Creates file suffix for specified input parameters (without compression).
	 * @param type type of the file
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param assemblyId
	 * @param repChainId alignment identifier
	 * @param format the format of the coordinates file
	 * @return suffix of the file
	 */
	private static String generateFileNameSuffixWithoutCompression(String type,
																   String jobId,
																   String interfaceId,
																   String assemblyId,
																   String repChainId,
																   String format)
	{
		String pattern = null;
		
		if(type.equals(FileDownloadServlet.TYPE_VALUE_INTERFACE)) {
			
			if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_PDB)) {
				pattern = EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interfaceId + ".pdb";
			} else if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_CIF)) {
				pattern = EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interfaceId + ".cif";
			} else {
				// default pdb to be backwards compatible 
				logger.info("No format specified for type=interface, using pdb as default format");
				pattern = EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interfaceId + ".pdb"; 
			}
		}
		else if (type.equals(FileDownloadServlet.TYPE_VALUE_ASSEMBLY)) {
			
			if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_PDB)) {
				pattern = EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX + "." + assemblyId + ".pdb";
			} else if (format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_CIF)) {
				pattern = EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX + "." + assemblyId + ".cif";
			} else {
				// default pdb to be backwards compatible 
				logger.info("No format specified for type=assembly, using pdb as default format");
				pattern = EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX + "." + assemblyId + ".pdb";
			}
		}
		else if(type.equals(FileDownloadServlet.TYPE_VALUE_MSA)) {
			pattern = "." + repChainId + ".aln";
		}
		
		return pattern;
	}
}
