package ch.systemsx.sybit.crkwebui.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * File content reader.
 * @author AS
 */
public class FileContentReader {

	/**
	 * Reads content of the file.
	 * @param file file to read
	 * @return content of the file
	 * @throws IOException when can not retrieve content of the file
	 */
	public static String readContentOfFile(File file) throws IOException
	{
		FileReader inputStream = null;
        BufferedReader bufferedInputStream = null;

        try
        {
        	inputStream = new FileReader(file);
	        bufferedInputStream = new BufferedReader(inputStream);
	        
	        StringBuffer content = new StringBuffer();
	        
	        String line = null;

	        while ((line = bufferedInputStream.readLine()) != null)
	        {
	        	content.append(line);
	        }
	        
	        return content.toString();
        }
        finally
        {
        	if(bufferedInputStream != null)
			{
				try
				{
					bufferedInputStream.close();
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
        }
	}
}
