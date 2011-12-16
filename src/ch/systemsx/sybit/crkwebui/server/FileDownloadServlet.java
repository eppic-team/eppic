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
 * 
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
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * Return file specified by the id - name of the directory where file is
	 * stored
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		final String type = request.getParameter("type");
		final String jobId = request.getParameter("id");
		final String interfaceId = request.getParameter("interface");
		final String alignment = request.getParameter("alignment");

		ServletOutputStream output = response.getOutputStream();

		if ((type != null) && (type.length() != 0))
		{
			if ((jobId != null) && (jobId.length() != 0)) 
			{
				if(!jobId.matches("^[A-Za-z0-9]+$"))
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect format of job id");
				}
				else if(((type.equals("interface") || (type.equals("pse")))) && 
				   ((interfaceId == null) ||
				    (interfaceId.equals(""))))
			    {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, messages.getProperty("fileDownloadResultNoInterfaceSpecified"));
			    }
				else if((interfaceId != null) &&
						(!interfaceId.equals("")) &&
						(!interfaceId.matches("^[0-9]+$")))
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect format of interface");
				}
				else if(type.equals("fasta") &&
						((alignment == null) ||
						(alignment.equals("")) ||
						(!alignment.matches("^[A-Za-z]$"))))
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect format of alignment");
				}
				else
				{
					File resultFileDirectory = new File(
							properties.getProperty("destination_path") + "/"
									+ jobId);
		
					if (resultFileDirectory.exists()
						&& resultFileDirectory.isDirectory()) 
					{
						final String suffixType = FileNameGenerator.generateFileNameToDownload(type, jobId, interfaceId, alignment);
						
						if(suffixType == null)
						{
							response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No support for selected type");
						}
						else
						{
							String[] directoryContent = resultFileDirectory
									.list(new FilenameFilter() {
			
										public boolean accept(File dir, String name) {
											if (name.endsWith(suffixType)) {
												return true;
											} else {
												return false;
											}
										}
									});
			
							if ((directoryContent != null) && (directoryContent.length > 0)) 
							{
								File resultFile = new File(resultFileDirectory + "/"
										+ directoryContent[0]);
			
								if (resultFile.exists()) 
								{
									if(directoryContent[0].endsWith(".pdb"))
									{
										response.setContentType("chemical/x-pdb");
									}
									else if(directoryContent[0].endsWith(".zip"))
									{
										response.setContentType("application/zip");
									}
									else if(directoryContent[0].endsWith(".pse"))
									{
										response.setContentType("application/pymol-session");
									}
									else if(directoryContent[0].endsWith(".aln"))
									{
										response.setContentType("text/plain");
									}
									else
									{
										response.setContentType("application/octet-stream");
									}
									
									
									String processedFileName = directoryContent[0];
									
									if(directoryContent[0].contains("."))
									{
										processedFileName = directoryContent[0].substring(0, directoryContent[0].indexOf("."));
										processedFileName = processedFileName + "-" + jobId + ".";
										
										if(directoryContent[0].indexOf(".") + 1 != directoryContent[0].length())
										{
											processedFileName = processedFileName + directoryContent[0].substring(directoryContent[0].indexOf(".") + 1);
										}
									}
									
									response.setContentLength((int) resultFile.length());
									response.setHeader("Content-Disposition",
											"attachment; filename=\"" + processedFileName + "\"");
	//								response.setHeader("Content-Encoding", "gzip");
			
									byte[] buffer = new byte[1024];
									DataInputStream input = new DataInputStream(
											new FileInputStream(resultFile));
			
									int length;
			
									while ((input != null)
											&& ((length = input.read(buffer)) != -1)) 
									{
										output.write(buffer, 0, length);
									}
			
									input.close();
									output.flush();
									output.close();
								} 
								else 
								{
									response.sendError(
											HttpServletResponse.SC_NOT_FOUND,
											messages.getProperty("fileDownloadResultFileNofFound"));
								}
							} 
							else 
							{
								response.sendError(
										HttpServletResponse.SC_NOT_FOUND,
										messages.getProperty("fileDownloadResultFileNofFound"));
							}
						}
					}
					else
					{
						response.sendError(HttpServletResponse.SC_NOT_FOUND, messages
								.getProperty("fileDownloadResultDirectoryNotFound"));
					}
				}
			} 
			else
			{
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						messages.getProperty("fileDownloadResultIdNotSpecified"));
			}
		}
		else
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					messages.getProperty("fileDownloadResultTypeNotSpecified"));
		}
	}
}
