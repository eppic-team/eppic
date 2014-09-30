package ch.systemsx.sybit.crkwebui.server.files.downloader.generators;

/**
 * This class is used to create file name of the file to download.
 */
public class FileToDownloadNameGenerator 
{
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
