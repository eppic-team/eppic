package ch.systemsx.sybit.crkwebui.server.files.uploader.generators;

import org.apache.commons.fileupload.FileItem;

public class FileToUploadNameGenerator 
{
	/**
	 * Creates name of the file to upload.
	 * @param fileToUpload file to upload
	 * @return name of the file
	 */
	public static String generateNameOfFileToUpload(FileItem fileToUpload)
	{
		String fileName = fileToUpload.getName();
		
		if(fileName.contains("\\"))
		{
			fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
		}
		
		return fileName;
	}
}
