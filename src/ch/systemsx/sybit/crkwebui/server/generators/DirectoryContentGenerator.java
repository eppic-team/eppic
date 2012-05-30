package ch.systemsx.sybit.crkwebui.server.generators;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Directory content utils.
 * @author AS
 *
 */
public class DirectoryContentGenerator
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
}
