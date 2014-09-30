package ch.systemsx.sybit.crkwebui.server.commons.util.io;

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
	 * @param directory directory to check
	 * @return true if directory exists, false otherwise
	 */
	public static boolean checkIfDirectoryExist(File directory)
	{
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
	 * @param file file to check
	 * @return true if file exists, false otherwise
	 */
	public static boolean checkIfFileExist(File file) 
	{
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
