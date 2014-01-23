package ch.systemsx.sybit.crkwebui.server.files.downloader.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.servlets.util.RequestUtil;
import ch.systemsx.sybit.crkwebui.server.commons.servlets.util.ResponseUtil;
import ch.systemsx.sybit.crkwebui.server.commons.util.http.ContentTypeGenerator;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.DirectoryContentReader;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.FileToDownloadNameGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.FileToDownloadNameSuffixGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileDownloadServletInputValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileToDownloadLocationValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileToDownloadNameSuffixValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.FileToDownloadValidator;
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
		boolean acceptGzipEncoding = RequestUtil.checkIfAcceptGzipEncoding(request);
		
		String type = request.getParameter("type");
		String jobId = request.getParameter("id");
		String interfaceId = request.getParameter("interface");
		String alignment = request.getParameter("alignment");

		try
		{
			FileDownloadServletInputValidator.validateFileDownloadInput(type, jobId, interfaceId, alignment);
			
			File fileToDownloadLocation = new File(properties.getProperty("destination_path"), jobId);
			FileToDownloadLocationValidator.validateLocation(fileToDownloadLocation);
			
			String suffix = FileToDownloadNameSuffixGenerator.generateFileNameSuffix(type, jobId, interfaceId, alignment);
			FileToDownloadNameSuffixValidator.validateSuffix(suffix);
			
			boolean isContentGzipped = ResponseUtil.checkIfDoGzipEncoding(acceptGzipEncoding, suffix);
			
			File fileToDownload = DirectoryContentReader.getFileFromDirectoryWithSpecifiedSuffix(fileToDownloadLocation, suffix);
			FileToDownloadValidator.validateFile(fileToDownload);

			prepareResponse(response, fileToDownload.getName(), jobId, isContentGzipped);
			ResponseUtil.printFileContentToOutput(fileToDownload, response);
		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		}
		catch(IOException e)
		{
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "Error while trying to download the file: " + e.getMessage());
		}
	}
	
	/**
	 * Sets header and content type of response.
	 * @param response response to update
	 * @param resultFileName name of the file to donwload
	 * @param jobId identiifer of the job
	 * @param isContentGzipped flag pointing whether compressed content is provided
	 */
	private void prepareResponse(HttpServletResponse response,
								 String resultFileName,
								 String jobId,
								 boolean isContentGzipped)
	{
		String contentType = ContentTypeGenerator.generateContentTypeByFileExtension(resultFileName);
		response.setContentType(contentType);
		
		String processedFileName = FileToDownloadNameGenerator.generateNameOfTheFileToDownload(resultFileName, jobId);
		
		//remove gz
		if(isContentGzipped)
		{
			processedFileName = processedFileName.substring(0, processedFileName.length() - 3);
			response.setHeader("Content-Encoding", "gzip");
		}
		
//				response.setContentLength((int) resultFile.length());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + processedFileName + "\"");
	}
}
