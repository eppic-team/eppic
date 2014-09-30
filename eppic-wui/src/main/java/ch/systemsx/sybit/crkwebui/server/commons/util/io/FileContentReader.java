package ch.systemsx.sybit.crkwebui.server.commons.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * File content reader.
 * @author AS
 */
public class FileContentReader {

	/**
	 * Reads content of the file.
	 * @param file file to read
	 * @param addNewLine flag pointing whether new line char should be added to the end of the read line
	 * @return content of the file
	 * @throws IOException when can not retrieve content of the file
	 */
	public static String readContentOfFile(File file, boolean addNewLine) throws IOException
	{
		FileReader inputStream = null;
        BufferedReader bufferedReader = null;

        try
        {
        	inputStream = new FileReader(file);
        	bufferedReader = new BufferedReader(inputStream);
	        
	        return readContentOfFile(bufferedReader, addNewLine);
        }
        finally
        {
        	if(bufferedReader != null)
			{
				try
				{
					bufferedReader.close();
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
        	
        	if(inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
        }
	}
	
	/**
	 * Reads content of the input stream.
	 * @param inputStream input stream
	 * @param addNewLine flag pointing whether new line char should be added to the end of the read line
	 * @return content of the stream
	 * @throws IOException when can not read content of the stream
	 */
	public static String readContentOfFile(InputStream inputStream, boolean addNewLine) throws IOException
	{
		InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try
        {
        	inputStreamReader = new InputStreamReader(inputStream);
        	bufferedReader = new BufferedReader(inputStreamReader);
	        
	        return readContentOfFile(bufferedReader, addNewLine);
        }
        finally
        {
        	if(bufferedReader != null)
			{
				try
				{
					bufferedReader.close();
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
        	
        	if(inputStreamReader != null)
			{
				try
				{
					inputStreamReader.close();
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
        }
	}
	
	/**
	 * Reads content of the buffered reader.
	 * @param bufferedReader input buffered reader
	 * @param addNewLine flag pointing whether new line char should be added to the end of the read line
	 * @return content of the stream
	 * @throws IOException when can not read content of the stream
	 */
	private static String readContentOfFile(BufferedReader bufferedReader,
			   								boolean addNewLine) throws IOException
	{
		StringBuffer content = new StringBuffer();

		String line = null;

		while ((line = bufferedReader.readLine()) != null)
		{
			content.append(line);

			if(addNewLine)
			{
				content.append("\n");
			}
		}

		return content.toString();
	}
}
