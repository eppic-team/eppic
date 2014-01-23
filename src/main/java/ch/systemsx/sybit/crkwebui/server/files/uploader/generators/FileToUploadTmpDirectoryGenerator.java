package ch.systemsx.sybit.crkwebui.server.files.uploader.generators;

import java.io.File;
import java.io.IOException;

public class FileToUploadTmpDirectoryGenerator 
{
	/**
	 * Generates directory where uploaded file is temporarily stored.
	 * @param tmpDirectoryLocationName location of tmp directory
	 * @param tmpDirectoryName tmp directory name
	 * @return created tmp directory handler
	 * @throws IOException when generation of tmp directory fails
	 */
	public static File generateTmpDirectory(String tmpDirectoryLocationName,
											String tmpDirectoryName) throws IOException
	{
		File localTmpDir = new File(tmpDirectoryLocationName, tmpDirectoryName);
		if(!localTmpDir.mkdir())
		{
			throw new IOException("Can not create temporary directory to upload file.");
		}
		
		return localTmpDir;
	}
}
