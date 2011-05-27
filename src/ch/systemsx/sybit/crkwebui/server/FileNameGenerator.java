package ch.systemsx.sybit.crkwebui.server;

/**
 * 
 * @author srebniak_a
 *
 */
public class FileNameGenerator 
{

	public static String generateFileNameToDownload(String type,
													String jobId,
													String interfaceId)
	{
		String pattern = "";
		
		if(type.equals("zip"))
		{
			pattern = ".zip";
		}
		else if(type.equals("interface"))
		{
			pattern = "." + interfaceId + ".rimcore.pdb";
		}
		
		return pattern;
	}
}
