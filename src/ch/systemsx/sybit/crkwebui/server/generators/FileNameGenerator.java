package ch.systemsx.sybit.crkwebui.server.generators;

/**
 * This class is used to create file names and patterns based on input parameters.
 */
public class FileNameGenerator 
{

	/**
	 * Creates file suffix for specified input parameters.
	 * @param type type of the file
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param alignment alignment identifier
	 * @return suffix of the file
	 */
	public static String generateFileSuffixNameToDownload(String type,
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
		
		return pattern;
	}
	
	/**
	 * Creates name of the file to download.
	 * @param originalName original file name
	 * @param jobId identifier of the job used to add to the name of the file
	 * @return name of the file to download
	 */
	public static String generateNameOfTheFileToDownload(String originalName,
														 String jobId)
	{
		String processedFileName = originalName;
		
		if((originalName != null) && (originalName.contains(".")))
		{
			processedFileName = originalName.substring(0, originalName.indexOf("."));
			processedFileName = processedFileName + "-" + jobId + ".";
			
			if(originalName.indexOf(".") + 1 != originalName.length())
			{
				processedFileName = processedFileName + originalName.substring(originalName.indexOf(".") + 1);
			}
		}
		
		return processedFileName;
	}
}
