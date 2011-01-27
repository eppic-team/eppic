package ch.systemsx.sybit.crkwebui.server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used to download stored on the server side files
 * @author srebniak_a
 *
 */
public class FileDownloadServlet extends FileBaseServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Read properties files
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
	}
	
	/**
	 * Return file specified by the id - name of the directory where file is stored
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String selectedId = request.getParameter("id");
		
		ServletOutputStream output = response.getOutputStream();
		
		if((selectedId != null) && (selectedId.length() != 0))
		{
			
			File resultFileDirectory = new File(properties.getProperty("destination_path") + "/" + selectedId);
			
			if(resultFileDirectory.exists() &&
				resultFileDirectory.isDirectory())
			{
				String[] directoryContent = resultFileDirectory.list(new FilenameFilter() {
					
					public boolean accept(File dir, String name) 
					{
						if(name.endsWith(".scores"))
						{
							return true;
						}
						else
						{
							return false;
						}
					}
				});
				
				if(directoryContent != null &&
					directoryContent.length > 0)
				{
					File resultFile = new File(resultFileDirectory + "/" + directoryContent[0]);
					
					if(resultFile.exists())
					{
						response.setContentType("application/txt");
						response.setContentLength((int) resultFile.length());
						response.setHeader("Content-Disposition", "attachment; filename*=\"utf-8''" + resultFile + "");
						
						byte[] buffer = new byte[1024];
			            DataInputStream in = new DataInputStream(new FileInputStream(resultFile));
			
			            int length;
			            
			            while ((in != null) && ((length = in.read(buffer)) != -1)) 
			            {
			                output.write(buffer, 0, length);
			            }
			
			            in.close();
			            output.flush();
			            output.close();
					}
					else
					{
						response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.getProperty("fileDownloadResultFileNofFound"));
					}
				}
				else
				{
					response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.getProperty("fileDownloadResultFileNofFound"));
				}
			}
			else
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.getProperty("fileDownloadResultDirectoryNotFound"));
			}
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, messages.getProperty("fileDownloadResultIdNotSpecified"));
		}
	}
}
