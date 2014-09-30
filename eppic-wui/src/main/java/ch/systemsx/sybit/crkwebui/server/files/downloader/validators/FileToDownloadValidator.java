package ch.systemsx.sybit.crkwebui.server.files.downloader.validators;

import java.io.File;
import java.io.IOException;

public class FileToDownloadValidator 
{
	/**
	 * Validates correctness of file to download.
	 * @param fileToDownload file to download
	 * @throws IOException when validation fails
	 */
	public static void validateFile(File fileToDownload) throws IOException
	{
		if (fileToDownload == null) 
		{
			throw new IOException("File to download does not exist.");
		}
	}
}
