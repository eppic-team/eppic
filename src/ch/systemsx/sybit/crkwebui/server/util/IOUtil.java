package ch.systemsx.sybit.crkwebui.server.util;

import java.io.File;

/**
 * IO utils.
 * @author AS
 *
 */
public class IOUtil 
{
	/**
	 * Checks whether directory with specified name exists.
	 * @param directoryName name of the directory to check
	 * @return true if directory exists, false otherwise
	 */
	public static boolean checkIfDirectoryExist(String directoryName)
	{
		File directory = new File(directoryName);

		if (directory.exists() && directory.isDirectory()) 
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Checks whether file with specified name exists.
	 * @param fileName name of the file to check
	 * @return true if file exists, false otherwise
	 */
	public static boolean checkIfFileExist(String fileName) 
	{
		File file = new File(fileName);

		if (file.exists()) 
		{
			return true;
		}
		else 
		{
			return false;
		}
	}
	
}
