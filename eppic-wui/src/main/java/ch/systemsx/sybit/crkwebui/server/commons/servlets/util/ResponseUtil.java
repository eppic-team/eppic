package ch.systemsx.sybit.crkwebui.server.commons.servlets.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

public class ResponseUtil {

	/**
	 * Prints content of the retrieved file to the output.
	 * @param resultFile retrieved file
	 * @param response servlet response
	 * @throws IOException when printing fails
	 */
	public static void printFileContentToOutput(File resultFile,
										  		HttpServletResponse response) throws IOException
	{
		byte[] buffer = new byte[1024];

		try (
				DataInputStream input = new DataInputStream(new FileInputStream(resultFile));
				ServletOutputStream output = response.getOutputStream()) {

			int length;

			while ((length = input.read(buffer)) != -1) {
				output.write(buffer, 0, length);
			}
		}
	}
	
	/**
	 * Checks whether compress response.
	 * @param acceptGzipEncoding flag pointing whether browser supports encoding of the response
	 * @param suffix suffix of the file to download
	 * @return information whether compressed response should be sent
	 * @throws ValidationException when there is no support for encoding
	 */
	public static boolean checkIfDoGzipEncoding(boolean acceptGzipEncoding,
												String suffix) throws ValidationException
	{
		boolean isContentGzipped = false;
		
		if(suffix.endsWith(".gz"))
		{
			isContentGzipped = true;
		}
		
		if((isContentGzipped) && (!acceptGzipEncoding))
		{
			throw new ValidationException("No support for gzip encoding - please use a browser supporting Content-Encoding:gzip");
		}
		
		return isContentGzipped;
	}
	
}
