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
													String interfaceId,
													String alignment)
	{
		String pattern = null;
		
		if(type.equals("zip"))
		{
			pattern = ".zip";
		}
		else if(type.equals("interface"))
		{
			pattern = "." + interfaceId + ".pdb";
		}
		else if(type.equals("pse"))
		{
			pattern = "." + interfaceId + ".pse";
		}
		else if(type.equals("pse"))
		{
			pattern = "." + interfaceId + ".pse";
		}
		else if(type.equals("fasta"))
		{
			pattern = "." + alignment + ".aln";
		}
		
		return pattern;
	}
}
