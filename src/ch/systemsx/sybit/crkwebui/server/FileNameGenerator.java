package ch.systemsx.sybit.crkwebui.server;

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
		else if(type.equals("interfaces"))
		{
			pattern = interfaceId + ".rimcore.pdb";
		}
		
		return pattern;
	}
}
