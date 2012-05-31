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

import ch.systemsx.sybit.crkwebui.server.generators.FileNameGenerator;

/**
 * Servlet used to download stored on the server side files.
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
		boolean acceptGzipEncoding = false;
		String acceptEncodingHeader = request.getHeader("Accept-Encoding");
		if((acceptEncodingHeader != null) &&
		   (acceptEncodingHeader.contains("gzip")))
		{
			acceptGzipEncoding = true;
		}
		
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
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, messages.getProperty("fileDownloadResultIncorrectFormatOfJobId"));
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
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, messages.getProperty("fileDownloadResultIncorrectFormatOfInterface"));
				}
				else if(type.equals("fasta") &&
						((alignment == null) ||
						(alignment.equals("")) ||
						(!alignment.matches("^[A-Za-z]$"))))
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, messages.getProperty("fileDownloadResultIncorrectFormatOfAlignment"));
				}
				else
				{
					File resultFileDirectory = new File(
							properties.getProperty("destination_path"), jobId);
		
					if (resultFileDirectory.exists()
						&& resultFileDirectory.isDirectory()) 
					{
						String suffixType = FileNameGenerator.generateFileSuffixNameToDownload(type, jobId, interfaceId, alignment);
						
						if(suffixType == null)
						{
							response.sendError(HttpServletResponse.SC_BAD_REQUEST, messages.getProperty("fileDownloadResultNoSupportForType"));
						}
						else
						{
							boolean isContentGzipped = false;
							
							if(suffixType.endsWith(".pdb") ||
							  (suffixType.endsWith(".pse")))
							{
								isContentGzipped = true;
							}
							
							if(isContentGzipped && !acceptGzipEncoding)
							{
								response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, messages.getProperty("fileDownloadResultNoSupportForGzipEncoding"));
							}
							else
							{
								if(isContentGzipped)
								{
									suffixType = suffixType + ".gz";
								}
								
								final String usedSuffix = suffixType;
								
								String[] directoryContent = resultFileDirectory
										.list(new FilenameFilter() {
				
											public boolean accept(File dir, String name) {
												if (name.endsWith(usedSuffix)) {
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
										if(directoryContent[0].endsWith(".pdb.gz"))
										{
											response.setContentType("chemical/x-pdb");
										}
										else if(directoryContent[0].endsWith(".zip"))
										{
											response.setContentType("application/zip");
										}
										else if(directoryContent[0].endsWith(".pse.gz"))
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
										
										//remove gz
										if(isContentGzipped)
										{
											processedFileName = processedFileName.substring(0, processedFileName.length() - 3);
											response.setHeader("Content-Encoding", "gzip");
										}
										
	//									response.setContentLength((int) resultFile.length());
										response.setHeader("Content-Disposition",
												"attachment; filename=\"" + processedFileName + "\"");
				
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
