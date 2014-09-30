package ch.systemsx.sybit.crkwebui.server.files.downloader.validators;

import java.io.File;
import java.io.IOException;

public class FileToDownloadLocationValidator 
{
	/**
	 * Validates correctness of the location of file to download.
	 * @param location directory where file to download is located
	 * @throws IOException when validation fails
	 */
	public static void validateLocation(File location) throws IOException
	{
		if ((!location.exists()) ||
			(!location.isDirectory())) 
		{
			throw new IOException("Location does not exist.");
		}
	}
}
