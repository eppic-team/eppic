package ch.systemsx.sybit.crkwebui.server.files.uploader.generators;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileItemsGenerator 
{
	private int maxFileUploadSize;
	
	public FileItemsGenerator(int maxFileUploadSize)
	{
		this.maxFileUploadSize = maxFileUploadSize;
	}
	
	public List<FileItem> generateFileItems(HttpServletRequest request,
			File localTmpDir) throws FileUploadException
	{
		ServletFileUpload uploadHandler = createServletFileUpload(localTmpDir);
		List<FileItem> items = uploadHandler.parseRequest(request);
		return items;
	}
	
	private ServletFileUpload createServletFileUpload(File localTmpDir)
	{
		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
		fileItemFactory.setSizeThreshold(maxFileUploadSize * 1024 * 1024);
		fileItemFactory.setRepository(localTmpDir);

		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		uploadHandler.setFileSizeMax(maxFileUploadSize * 1024 * 1024);
		return uploadHandler;
	}
}
