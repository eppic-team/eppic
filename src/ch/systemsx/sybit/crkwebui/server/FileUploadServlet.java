package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.RandomStringUtils;

import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

/**
 * Servlet used to upload documents by the users to server
 * @author srebniak_a
 *
 */
public class FileUploadServlet extends FileBaseServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String generalTmpDirectoryName;
	private String generalDestinationDirectoryName;
	
	/**
	 * Read properties file
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		
		InputStream propertiesStream = 
			getServletContext().getResourceAsStream("/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/server.properties");
		
		properties = new Properties();
		
		try 
		{
			properties.load(propertiesStream);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			throw new ServletException("Properties file can not be read");
		}
		
		InputStream messagesStream = 
			getServletContext().getResourceAsStream("/WEB-INF/classes/ch/systemsx/sybit/crkwebui/server/constants.properties");
		
		messages = new Properties();
		
		try 
		{
			messages.load(messagesStream);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			throw new ServletException("Properties with messages can not be read");
		}
		
		generalTmpDirectoryName = properties.getProperty("tmp_path");
		File tmpDir = new File(generalTmpDirectoryName);
		
		if(!tmpDir.isDirectory()) 
		{
			throw new ServletException(generalTmpDirectoryName + " is not a directory");
		}
		
//		String realPath = getServletContext().getRealPath(properties.getProperty("destination_path"));
		generalDestinationDirectoryName = properties.getProperty("destination_path");
		File destinationDir = new File(generalDestinationDirectoryName);
		
		if(!destinationDir.isDirectory()) 
		{
			throw new ServletException(generalDestinationDirectoryName + " is not a directory");
		}
	}
 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		if (ServletFileUpload.isMultipartContent(request)) 
		{
			String randomDirectoryName = null;
			boolean isDirectorySet = false;
			
			while(!isDirectorySet)
			{
				randomDirectoryName = RandomStringUtils.randomAlphanumeric(30);
				
				File randomDirectory = new File(randomDirectoryName);
				
				if(!randomDirectory.exists())
				{
					isDirectorySet = true;
				}
			}

			File localTmpDir = new File(generalTmpDirectoryName + "/" + randomDirectoryName);
			localTmpDir.mkdir();
			
			String localDestinationDirName = generalDestinationDirectoryName + "/" + randomDirectoryName;
			File localDestinationDir = new File (localDestinationDirName);
			localDestinationDir.mkdir();
			
			PrintWriter out = response.getWriter();
		    response.setContentType("text/plain");
		    
			DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();
			
			//Set the size threshold, above which content will be stored on disk.
			fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
			
			//Set the temporary directory to store the uploaded files of size above threshold.
			fileItemFactory.setRepository(localTmpDir);
	 
			ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
			
			String fileName = "";
			
			try 
			{
				/*
				 * Parse the request
				 */
				List<FileItem> items = uploadHandler.parseRequest(request);
				
				for(FileItem item : items) 
				{
					if(!item.isFormField())
					{
						fileName = item.getName(); 
						File file = new File(localDestinationDir, item.getName());
						item.write(file);
						
						File processingFile = new File(localDestinationDir + "/" + item.getName());
						processingFile.createNewFile();
					}
				}
				
				out.println(randomDirectoryName);
				out.close();
			}
			catch(FileUploadException ex) 
			{
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
				  				   "Error during uploading the file.");
			} 
			catch(Exception ex) 
			{
				String errorStack = "";
				for(StackTraceElement element : ex.getStackTrace())
				{
					errorStack += element.toString() + "<br>"; 
				}
				
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
								   "Error encountered while uploading file" + errorStack);
			}
			
		}
		else
		{
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
							  "Request contents type is not supported by the servlet.");
		}
 
	}
	
}
