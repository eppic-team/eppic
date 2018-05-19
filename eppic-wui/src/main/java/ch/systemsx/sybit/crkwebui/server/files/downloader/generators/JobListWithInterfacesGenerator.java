package ch.systemsx.sybit.crkwebui.server.files.downloader.generators;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * This class is used to generate the job list from request
 * @author Nikhil Biyani
 *
 */
public class JobListWithInterfacesGenerator {
	
	/**
	 * Converts a string of integers separated by ',' to a list of integers
	 * @param commaSeparatedString the comma separated list of integers, spaces allowed
	 * @return set of integers or null if input is null or empty
	 * @throws ValidationException if any of the comma separated tokens are not strings representing ints
	 */
	public static Set<Integer> createIntegerList(String commaSeparatedString) throws ValidationException{

		if (commaSeparatedString == null || commaSeparatedString.trim().isEmpty()) {
			return null;
		}

		Set<Integer> intList = new HashSet<>();
		List<String> intStrList = Arrays.asList(commaSeparatedString.split("\\s*,\\s*"));
		
		for(String intStr: intStrList){
			try{
				int i = Integer.parseInt(intStr);
				intList.add(i);

			}catch(NumberFormatException e){
				throw new ValidationException("Non-integer id provided: " + e.getMessage());
			}
		}
		
		return intList;
	}
}