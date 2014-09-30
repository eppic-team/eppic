package ch.systemsx.sybit.crkwebui.server.files.uploader.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.RandomDirectoryGenerator;
import ch.systemsx.sybit.crkwebui.server.files.uploader.data.UploadingData;
import ch.systemsx.sybit.crkwebui.server.files.uploader.generators.FileItemsGenerator;
import ch.systemsx.sybit.crkwebui.server.files.uploader.generators.FileToUploadNameGenerator;
import ch.systemsx.sybit.crkwebui.server.files.uploader.generators.FileToUploadTmpDirectoryGenerator;
import ch.systemsx.sybit.crkwebui.server.files.uploader.generators.UploadingDataGenerator;
import ch.systemsx.sybit.crkwebui.server.files.uploader.validators.FileToUploadNameValidator;
import ch.systemsx.sybit.crkwebui.server.files.uploader.validators.UploadingValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Servlet used to upload documents by the users to server
 * 
 * @author srebniak_a
 * 
 */
public class FileUploadServlet extends BaseServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String generalTmpDirectoryName;
	private String generalDestinationDirectoryName;
	
	private FileItemsGenerator fileItemsGenerator;
	private UploadingValidator uploadingValidator;
	
	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);

		generalTmpDirectoryName = properties.getProperty("tmp_path");
		File tmpDir = new File(generalTmpDirectoryName);

		if (!tmpDir.isDirectory()) 
		{
			throw new ServletException(generalTmpDirectoryName
					+ " is not a directory");
		}

		generalDestinationDirectoryName = properties.getProperty("destination_path");
		File destinationDir = new File(generalDestinationDirectoryName);

		if (!destinationDir.isDirectory()) 
		{
			throw new ServletException(generalDestinationDirectoryName
					+ " is not a directory");
		}
	
		uploadingValidator = createUploadingValidator();
		fileItemsGenerator = createFileItemsGenerator();
	}

	@Override
	protected void doPost(HttpServletRequest request,
						  HttpServletResponse response) throws IOException 
	{
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		
		if (ServletFileUpload.isMultipartContent(request)) 
		{
			try 
			{
				String randomDirectoryName = RandomDirectoryGenerator.generateRandomDirectory(generalDestinationDirectoryName);
				File tmpDir = FileToUploadTmpDirectoryGenerator.generateTmpDirectory(generalTmpDirectoryName, randomDirectoryName);
	
				List<FileItem> items = fileItemsGenerator.generateFileItems(request, tmpDir);
				UploadingData uploadingData = UploadingDataGenerator.generateUploadingData(items);
				uploadingValidator.validate(uploadingData, request.getRemoteAddr());
				
				String fileName = FileToUploadNameGenerator.generateNameOfFileToUpload(uploadingData.getFileToUpload());
				FileToUploadNameValidator.validateName(fileName);
				
				File fileToUploadDirectory = new File(generalDestinationDirectoryName, randomDirectoryName);
				File file = new File(fileToUploadDirectory, fileName);
				uploadingData.getFileToUpload().write(file);
					
				out.println("crkupres:" + randomDirectoryName);
			} 
			catch(ValidationException ex)
			{
				out.println("err:" + "Verification failed - " + ex.getMessage());
			}
			catch(FileUploadException ex)
			{
				out.println("err:" + "Error while uploading the file - " + ex.getMessage());
			}
			catch(Exception ex) 
			{
				out.println("err:" + "Error encountered while uploading file - " + ex.getMessage());
			}
		}
		else 
		{
			out.println("err:" + "Error encountered while uploading file - Request content type is not supported by the servlet.");
		}
		
		out.close();
	}

	private FileItemsGenerator createFileItemsGenerator() 
	{
		int maxFileUploadSize = Integer.parseInt(properties.getProperty("max_file_upload_size", "10"));
		
		FileItemsGenerator fileItemsGenerator = new FileItemsGenerator(maxFileUploadSize);
		return fileItemsGenerator;
	}
	
	/**
	 * Generates general uploading validator.
	 * @return uploading validator
	 */
	private UploadingValidator createUploadingValidator()
	{
		boolean useCaptcha = Boolean.parseBoolean(properties.getProperty("use_captcha"));
		String captchaPublicKey = properties.getProperty("captcha_public_key");
		String captchaPrivateKey = properties.getProperty("captcha_private_key");
		int nrOfAllowedSubmissionsWithoutCaptcha = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_without_captcha"));
		
		boolean doIPBasedVerification = Boolean.parseBoolean(properties.getProperty("limit_access_by_ip","false"));
		int defaultNrOfAllowedSubmissionsForIP = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_for_ip","100"));

		UploadingValidator uploadingValidator = new UploadingValidator(useCaptcha, 
																	   nrOfAllowedSubmissionsWithoutCaptcha, 
																	   captchaPublicKey, 
																	   captchaPrivateKey, 
																	   doIPBasedVerification, 
																	   defaultNrOfAllowedSubmissionsForIP);
		return uploadingValidator;
	}
}
