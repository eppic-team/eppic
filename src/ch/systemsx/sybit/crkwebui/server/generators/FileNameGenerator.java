package ch.systemsx.sybit.crkwebui.server.generators;

/**
 * This class is used to create file suffixes for specified input parameters.
 * @author srebniak_a
 *
 */
public class FileNameGenerator 
{

	/**
	 * Creates file suffix for specified input parameters.
	 * @param type type of the file
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param alignment alignment identifier
	 * @return suffix of the file
	 */
	public static String generateFileSuffixNameToDownload(String type,
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
		else if(type.equals("fasta"))
		{
			pattern = "." + alignment + ".aln";
		}
		
		return pattern;
	}
}
