package eppic.model.dto;


/**
 * DTO class for PDBInfo.
 * @author AS
 */
public class PdbInfo
{

	/**
	 * Truncates the given fileName by removing anything after the last dot.
	 * If no dot present in fileName then nothing is truncated.
	 * @param fileName
	 * @return
	 */
	public static String truncateFileName(String fileName) {
		if( fileName == null) return null;
		
		String newName = fileName;
		int lastPeriodPos = fileName.lastIndexOf('.');
		if (lastPeriodPos >= 0)
		{
			newName = fileName.substring(0, lastPeriodPos);
		}
		return newName;

	}

}
