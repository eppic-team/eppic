package ch.systemsx.sybit.crkwebui.server.files.downloader.servlets;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirectoryContentReader;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.ContentTypeGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.FileNameGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileDownloadServletInputValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Servlet used to download stored on the server side files.
 * 
 * @author srebniak_a
 * 
 */
public class FileDownloadServlet extends BaseServlet 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * Returns file specified by the parameters.
	 */
	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response) throws ServletException, IOException
	{
		boolean acceptGzipEncoding = checkIfAcceptGzipEncoding(request);
		
		String type = request.getParameter("type");
		String jobId = request.getParameter("id");
		String interfaceId = request.getParameter("interface");
		String alignment = request.getParameter("alignment");

		try
		{
			FileDownloadServletInputValidator.validateFileDownloadInput(type, jobId, interfaceId, alignment);
			
			File resultFileDirectory = new File(properties.getProperty("destination_path"), jobId);

			if (resultFileDirectory.exists()
				&& resultFileDirectory.isDirectory()) 
			{
				String suffixType = FileNameGenerator.generateFileSuffixNameToDownload(type, jobId, interfaceId, alignment);
				
				if(suffixType == null)
				{
					throw new ValidationException("No support for specified type.");
				}
				else
				{
					boolean isContentGzipped = false;
					
					if(suffixType.endsWith(".pdb") ||
					  (suffixType.endsWith(".pse")))
					{
						isContentGzipped = true;
						suffixType = suffixType + ".gz";
					}
					
					if(isContentGzipped && !acceptGzipEncoding)
					{
						throw new ValidationException("No support for gzip encoding - please use the browser supporting Content-Encoding:gzip");
					}
					else
					{
						File resultFile = DirectoryContentReader.getFileFromDirectoryWithSpecifiedSuffix(resultFileDirectory, suffixType);
		
						if (resultFile != null) 
						{
							String contentType = ContentTypeGenerator.generateContentTypeByFileExtension(resultFile.getName());
							response.setContentType(contentType);
							
							String processedFileName = FileNameGenerator.generateNameOfTheFileToDownload(resultFile.getName(), jobId);
							
							//remove gz
							if(isContentGzipped)
							{
								processedFileName = processedFileName.substring(0, processedFileName.length() - 3);
								response.setHeader("Content-Encoding", "gzip");
							}
							
//									response.setContentLength((int) resultFile.length());
							response.setHeader("Content-Disposition", "attachment; filename=\"" + processedFileName + "\"");
	
							printFileContentToOutput(resultFile, response);
						} 
						else 
						{
							throw new IOException("Results file does not exist.");
						}
					}
				}
			}
			else
			{
				throw new IOException("Results directory does not exist.");
			}
		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error during trying to download the file: " + e.getMessage());
		}
	}
	
	/**
	 * Checks whether compress response.
	 * @param request servlet request
	 * @return information whether compressed response should be sent
	 */
	private boolean checkIfAcceptGzipEncoding(HttpServletRequest request)
	{
		boolean acceptGzipEncoding = false;
		
		String acceptEncodingHeader = request.getHeader("Accept-Encoding");
		if((acceptEncodingHeader != null) &&
		   (acceptEncodingHeader.contains("gzip")))
		{
			acceptGzipEncoding = true;
		}
		
		return acceptGzipEncoding;
	}
	
	/**
	 * Prints content of the retrieved file to the output.
	 * @param resultFile retrieved file
	 * @param response servlet response
	 * @throws IOException when printing fails
	 */
	private void printFileContentToOutput(File resultFile,
										  HttpServletResponse response) throws IOException
	{
		byte[] buffer = new byte[1024];
		DataInputStream input = null;
		ServletOutputStream output = null;
		
		try
		{
			input = new DataInputStream(new FileInputStream(resultFile));
			output = response.getOutputStream();
			
			int length;

			while ((input != null)
					&& ((length = input.read(buffer)) != -1)) 
			{
				output.write(buffer, 0, length);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			if(input != null)
			{
				try
				{
					input.close();
				}
				catch(Exception ex) {}
			}
			
			if(output != null)
			{
				try
				{
					output.close();
				}
				catch(Exception ex) {}
			}
		}
	}
	
}
