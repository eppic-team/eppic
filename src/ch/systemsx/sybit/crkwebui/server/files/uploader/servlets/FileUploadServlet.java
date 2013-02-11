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
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.commons.util.io.RandomDirectoryGenerator;
import ch.systemsx.sybit.crkwebui.server.commons.validators.CaptchaValidator;
import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.ip.validators.IPVerifier;
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
	
	private boolean useCaptcha;
	private CaptchaValidator captchaValidator;
	private int nrOfAllowedSubmissionsWithoutCaptcha = 1;
	
	private boolean doIPBasedVerification;
	private int defaultNrOfAllowedSubmissionsForIP;
	
	private int maxFileUploadSize;

	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);

		generalTmpDirectoryName = properties.getProperty("tmp_path");
		File tmpDir = new File(generalTmpDirectoryName);

		if (!tmpDir.isDirectory()) {
			throw new ServletException(generalTmpDirectoryName
					+ " is not a directory");
		}

		// String realPath =
		// getServletContext().getRealPath(properties.getProperty("destination_path"));
		generalDestinationDirectoryName = properties
				.getProperty("destination_path");
		File destinationDir = new File(generalDestinationDirectoryName);

		if (!destinationDir.isDirectory()) {
			throw new ServletException(generalDestinationDirectoryName
					+ " is not a directory");
		}
		
		useCaptcha = Boolean.parseBoolean(properties.getProperty("use_captcha"));
		String captchaPublicKey = properties.getProperty("captcha_public_key");
		String captchaPrivateKey = properties.getProperty("captcha_private_key");
		captchaValidator = new CaptchaValidator(captchaPublicKey, captchaPrivateKey);
		nrOfAllowedSubmissionsWithoutCaptcha = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_without_captcha"));
		
		doIPBasedVerification = Boolean.parseBoolean(properties.getProperty("limit_access_by_ip","false"));
		defaultNrOfAllowedSubmissionsForIP = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_for_ip","100"));
		
		maxFileUploadSize = Integer.parseInt(properties.getProperty("max_file_upload_size", "10"));
	}

	@Override
	protected void doPost(HttpServletRequest request,
						  HttpServletResponse response) throws ServletException, IOException 
	{
//		String currentSessionId = request.getSession().getId();
		
		PrintWriter out = response.getWriter();
		
		int nrOfSubmittedJobs = 0;
		
		if (ServletFileUpload.isMultipartContent(request)) 
		{
			String randomDirectoryName = RandomDirectoryGenerator.generateRandomDirectory(generalDestinationDirectoryName);

			File localTmpDir = new File(generalTmpDirectoryName, randomDirectoryName);
			localTmpDir.mkdir();

			response.setContentType("text/html");

			DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();

			// Set the size threshold, above which content will be stored on
			// disk.
			fileItemFactory.setSizeThreshold(maxFileUploadSize * 1024 * 1024); // 1 MB

			// Set the temporary directory to store the uploaded files of size
			// above threshold.
			fileItemFactory.setRepository(localTmpDir);

			ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
			
			uploadHandler.setFileSizeMax(maxFileUploadSize * 1024 * 1024);

			try 
			{
				List<FileItem> items = uploadHandler.parseRequest(request);
				
				String captchaResponse = null;
				String challenge = null;
				
				FileItem fileToUpload = null;
				
				for (FileItem item : items) 
				{
					if(item.isFormField())
					{
						if((useCaptcha) && (nrOfSubmittedJobs > nrOfAllowedSubmissionsWithoutCaptcha))
						{
							if(item.getFieldName() != null)
							{
								if(item.getFieldName().equals("recaptcha_response_field"))
								{
									captchaResponse = item.getString();
								}
								else if(item.getFieldName().equals("recaptcha_challenge_field"))
								{
									challenge = item.getString();
								}
							}
						}
					}
					else 
					{
						fileToUpload = item;
					}
				}
				
				if((useCaptcha) && (nrOfSubmittedJobs > nrOfAllowedSubmissionsWithoutCaptcha))
				{
					captchaValidator.verifyChallenge(challenge, captchaResponse, request.getRemoteAddr());
				}
				
				if(doIPBasedVerification)
				{
					IPVerifier.verifyIfCanBeSubmitted(request.getRemoteAddr(), defaultNrOfAllowedSubmissionsForIP);
				}
				
				String fileName = fileToUpload.getName();
				
				if(fileName.contains("\\"))
				{
					fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
				}
				
				String fileNameVerificationError = RunJobDataValidator.verifyFileName(fileName);
				
				if(fileNameVerificationError == null)
				{
					File fileToUploadDirectory = new File(generalDestinationDirectoryName, randomDirectoryName);
					File file = new File(fileToUploadDirectory, fileName);
					fileToUpload.write(file);
					
					out.println("crkupres:" + randomDirectoryName);
				}
				else
				{
					out.println("err:Incorrect file name - " + fileNameVerificationError);
				}
				
			} 
			catch(ValidationException ex)
			{
				out.println("err:" + "Verification failed - " + ex.getMessage());
			}
			catch(FileUploadException ex)
			{
				out.println("err:" + "Error during uploading the file." + ex.getMessage());
			}
			catch(Exception ex) 
			{
				out.println("err:" + "Error encountered while uploading file" + ex.getMessage());
			}
		}
		else 
		{
			out.println("err:" + "Error encountered while uploading file - Request content type is not supported by the servlet.");
		}
		
		out.close();
	}
}
