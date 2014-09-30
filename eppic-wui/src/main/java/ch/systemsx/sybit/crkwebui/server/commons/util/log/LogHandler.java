package ch.systemsx.sybit.crkwebui.server.commons.util.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Handler used to add text to log file.
 */
public class LogHandler
{
	/**
	 * Adds text to specified log file.
	 * @param logFile file location
	 * @param message text to add
	 */
	public static void writeToLogFile(File logFile, String message)
	{
		FileOutputStream outputStream = null;
		BufferedOutputStream bufferedOutputStream = null;

		try
		{
			outputStream = new FileOutputStream(logFile, true);
			bufferedOutputStream = new BufferedOutputStream(outputStream);
			bufferedOutputStream.write(message.getBytes());
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(bufferedOutputStream != null)
			{
				try
				{
					bufferedOutputStream.close();
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
		}
	}
}
