package ch.systemsx.sybit.crkwebui.server.files.downloader.generators;


/**
 * This class is used to create suffix of the file to download.
 */
public class FileToDownloadNameSuffixGenerator 
{

	/**
	 * Creates file suffix for specified input parameters (with compression).
	 * @param type type of the file
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param alignment alignment identifier
	 * @return suffix of the file
	 */
	public static String generateFileNameSuffix(String type,
												String jobId,
												String interfaceId,
												String alignment)
	{
		String suffix = generateFileNameSuffixWithoutCompression(type, jobId, interfaceId, alignment);
		
		if(suffix != null)
		{
			if((suffix.endsWith(".pdb")) ||
			   (suffix.endsWith(".pse")))
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
	 * @param alignment alignment identifier
	 * @return suffix of the file
	 */
	private static String generateFileNameSuffixWithoutCompression(String type,
																   String jobId,
																   String interfaceId,
																   String alignment)
	{
		String pattern = null;
		
		if(type.equals("zip"))
		{
			pattern = ".zip";
		}
		else if(type.equals("interface"))
		{
			pattern = "." + interfaceId + ".pdb";
		}
		else if(type.equals("pse"))
		{
			pattern = "." + interfaceId + ".pse";
		}
		else if(type.equals("fasta"))
		{
			pattern = "." + alignment + ".aln";
		}
		else if(type.equals("entropiespse")) {
			pattern = "." + alignment + ".entropies.pse";
		}
		
		return pattern;
	}
}
