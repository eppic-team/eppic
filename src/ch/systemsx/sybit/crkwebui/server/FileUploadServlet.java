package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ch.systemsx.sybit.crkwebui.server.util.RandomDirectoryNameGenerator;

/**
 * Servlet used to upload documents by the users to server
 * 
 * @author srebniak_a
 * 
 */
public class FileUploadServlet extends FileBaseServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String generalTmpDirectoryName;
	private String generalDestinationDirectoryName;
	
	private boolean useCaptcha;
	private String captchaPublicKey;
	private String captchaPrivateKey;
	private int nrOfAllowedSubmissionsWithoutCaptcha = 1;

	/**
	 * Read properties file
	 */
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
		captchaPublicKey = properties.getProperty("captcha_public_key");
		captchaPrivateKey = properties.getProperty("captcha_private_key");
		nrOfAllowedSubmissionsWithoutCaptcha = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_without_captcha"));
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		String currentSessionId = request.getSession().getId();
		
		PrintWriter out = response.getWriter();
		
		int nrOfSubmittedJobs = 0;
		
		if (ServletFileUpload.isMultipartContent(request)) 
		{
			String randomDirectoryName = RandomDirectoryNameGenerator.generateRandomDirectoryName(generalDestinationDirectoryName);

			File localTmpDir = new File(generalTmpDirectoryName + "/"
					+ randomDirectoryName);
			localTmpDir.mkdir();

			String localDestinationDirName = generalDestinationDirectoryName
					+ "/" + randomDirectoryName;
			File localDestinationDir = new File(localDestinationDirName);
			localDestinationDir.mkdir();

			response.setContentType("text/html");

			DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();

			// Set the size threshold, above which content will be stored on
			// disk.
			fileItemFactory.setSizeThreshold(1 * 1024 * 1024); // 1 MB

			// Set the temporary directory to store the uploaded files of size
			// above threshold.
			fileItemFactory.setRepository(localTmpDir);

			ServletFileUpload uploadHandler = new ServletFileUpload(
					fileItemFactory);

			try 
			{
				/*
				 * Parse the request
				 */
				List<FileItem> items = uploadHandler.parseRequest(request);
				
				boolean isVerified = true;
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
					if((captchaResponse == null) || (challenge == null))
					{
						isVerified = false;
					}
					else
					{
						isVerified = verifyChallenge(challenge, captchaResponse, request.getRemoteAddr());
					}
				}
				
				if(isVerified)
				{
					String fileName = fileToUpload.getName();
					
					if(fileName.contains("\\"))
					{
						fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
					}
					
					File file = new File(localDestinationDir,
							fileName);
					fileToUpload.write(file);

//					File processingFile = new File(localDestinationDir
//							+ "/" + fileName);
//					processingFile.createNewFile();
				}
				else
				{
					response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"Verification failed. Try again");
				}

				out.println("crkupres:" + randomDirectoryName);
				
			} 
			catch (FileUploadException ex)
			{
				out.println("err:" + "Error during uploading the file." + ex.getMessage());
//				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
//						"Error during uploading the file." + ex.getMessage());
			}
			catch (Exception ex) 
			{
				out.println("err:" + "Error encountered while uploading file" + ex.getMessage());
//				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
//						"Error encountered while uploading file" + ex.getMessage());
			}
		}
		else 
		{
			out.println("err:" + "Error encountered while uploading file" + "Request contents type is not supported by the servlet.");
//			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
//					"Request contents type is not supported by the servlet.");
		}
		
		out.close();
	}

	private boolean verifyChallenge(String challenge, String response, String remoteAddress) 
	{
		if(response == null)
		{
			return false;
		}
		else
		{
			ReCaptcha r = ReCaptchaFactory.newReCaptcha(captchaPublicKey, captchaPrivateKey, true);
			return r.checkAnswer(remoteAddress, challenge, response).isValid();
		}
	}

}
