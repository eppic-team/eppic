package ch.systemsx.sybit.crkwebui.server.commons.validators;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

/**
 * Run job data validator.
 * @author AS
 *
 */
public class RunJobDataValidator 
{
	/**
	 * Checks whether input parameters fulfill the requirements.
	 * @param inputParameters input parameters to validate
	 * @throws ValidationException when values are not valid
	 */
	public static void validateInputParameters(InputParameters inputParameters) throws ValidationException
	{
		String searchMode = inputParameters.getSearchMode().toLowerCase();
		
		if(!searchMode.matches("^[a-z]+$"))
		{
			throw new ValidationException("Incorrect format of search mode - only a-z characters are allowed");
		}
	}
	
	/**
	 * Checks whether jobid is alphanumeric value.
	 * @param jobId job id to validate
	 * @throws ValidationException when job id is not valid
	 */
	public static void validateJobId(String jobId) throws ValidationException
	{
		if(!jobId.matches("^[A-Za-z0-9]+$"))
		{
			throw new ValidationException("Incorrect job id format");
		}
	}
	
	/**
	 * Checks whether input is valid filename.
	 * @param input input value to validate
	 * @throws ValidationException when input is not valid
	 */
	public static void validateInput(String input) throws ValidationException
	{
		String verificationError = verifyFileName(input);
		
		if(verificationError != null)
		{
			throw new ValidationException(verificationError);
		}
	}
	
	/**
	 * Verifies whether filename is alphanumeric or contains ".", "-", "_".
	 * @param fileName name of the file to validate
	 * @return null if valid, otherwise error string
	 */
	public static String verifyFileName(String fileName) 
	{
		String result = null;
		
		if(!fileName.matches("^[A-Za-z0-9\\.\\-\\_]+$"))
		{
			return "Filename: " + fileName + 
				   " contains not allowed characters. Only the following characters are allowed: A-Z, a-z, 0-9, \".\", \"-\", \"_\"";
		}
		
		return result;
	}
}
