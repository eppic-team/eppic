package ch.systemsx.sybit.crkwebui.server.commons.util.io;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Directory content utils.
 * @author AS
 *
 */
public class DirectoryContentReader
{
	/**
	 * Retrieves list of files starting with specified name in the directory.
	 * @param directory directory to list
	 * @param prefix prefix of the filename
	 * @return array of files with specified prefix name in selected directory
	 */
	public static File[] getFilesNamesWithPrefix(File directory,
												 final String prefix)
	{
		File[] directoryContent = directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name)
			{
				if(name.startsWith(prefix))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		});

		return directoryContent;
	}
	
	/**
	 * Retrieves first file in the directory matching specified suffix.
	 * @param directory directory from which file is to be retrieved
	 * @param suffix suffix to match
	 * @return file with specified suffix
	 */
	public static File getFileFromDirectoryWithSpecifiedSuffix(File resultFileDirectory,
															   final String suffix)
	{
		File retrievedFile = null;
		
		String[] directoryContent = resultFileDirectory.list(new FilenameFilter() {

			public boolean accept(File dir, String name) 
			{
				if (name.endsWith(suffix)) 
				{
					return true;
				}
				else 
				{
					return false;
				}
			}
		});
		
		if ((directoryContent != null) && (directoryContent.length > 0)) 
		{
			retrievedFile = new File(resultFileDirectory + File.separator + directoryContent[0]);
		}
		
		return retrievedFile;
	}
}
