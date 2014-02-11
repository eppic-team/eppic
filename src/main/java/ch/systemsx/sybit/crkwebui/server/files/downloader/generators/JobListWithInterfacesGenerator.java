package ch.systemsx.sybit.crkwebui.server.files.downloader.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * This class is used to generate the job list from request
 * @author biyani_n
 *
 */
public class JobListWithInterfacesGenerator {
	
	/**
	 * Generates a list of jobIds from following type of string:
	 * 1smt_1:2:4,2gs2,1dan
	 * 
	 * Returns a map of jobsIds to list of interface ids; if all
	 * interface id's from a job are to be considered then the 
	 * list for that job is set to null
	 * @param commaSeparatedJobIdsWithInterfaceIds
	 * @return a map containing a list of jobIds mapped to list of interface Ids, 
	 * @throws ValidationException 
	 */
	public static Map<String, List<Integer>> generateJobList(String commaSeparatedJobIdsWithInterfaceIds) throws ValidationException
	{
		if(commaSeparatedJobIdsWithInterfaceIds == null) 
			throw new ValidationException("Please provide comma seperated values of either PDB-Codes or EPPIC-JobIds with &id=");
		
		Map<String, List<Integer>> map = new TreeMap<String, List<Integer>>();
		
		//Convert comma sep string to list
		List<String> jobIds = Arrays.asList(commaSeparatedJobIdsWithInterfaceIds.split("\\s*,\\s*"));
		
		for(String jobId:jobIds){
			if(jobId.contains("_")){
				String[] splitStr = jobId.split("_");
				List<Integer> interfaceIdList = createIntegerList(splitStr[1]);
				map.put(splitStr[0], interfaceIdList);	
			} 
			else {
				map.put(jobId, null);
			}
		}
		
		return map;
	}
	
	/**
	 * converts a string of integers separated by ':' to a list of integers
	 * @param colonSeparatedString
	 * @return list of integers
	 * @throws ValidationException 
	 */
	private static List<Integer> createIntegerList(String colonSeparatedString) throws ValidationException{
		List<Integer> intList = new ArrayList<Integer>();
		List<String> intStrList = Arrays.asList(colonSeparatedString.split("\\s*:\\s*"));
		
		for(String intStr: intStrList){
			try{
				int i = Integer.parseInt(intStr);
				intList.add(i);
			}catch(NumberFormatException e){
				throw new ValidationException("Non-Integer interfaceId provided for some id");
			}
		}
		
		return intList;
	}
}