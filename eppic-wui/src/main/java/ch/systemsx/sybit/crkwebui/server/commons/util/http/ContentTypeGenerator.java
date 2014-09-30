package ch.systemsx.sybit.crkwebui.server.commons.util.http;

/**
 * This class is used to generate content types.
 */
public class ContentTypeGenerator 
{

	/**
	 * Creates content type using file name.
	 * @param fileName name of the file
	 * @return content type
	 */
	public static String generateContentTypeByFileExtension(String fileName)
	{
		String contentType = null;
		
		if(fileName != null)
		{
			if(fileName.endsWith(".pdb.gz"))
			{
				contentType = "chemical/x-pdb";
			}
			else if(fileName.endsWith(".zip"))
			{
				contentType = "application/zip";
			}
			else if(fileName.endsWith(".pse.gz"))
			{
				contentType = "application/pymol-session";
			}
			else if(fileName.endsWith(".aln"))
			{
				contentType = "text/plain";
			}
			else
			{
				contentType = "application/octet-stream";
			}
		}
		
		return contentType;
	}
}
