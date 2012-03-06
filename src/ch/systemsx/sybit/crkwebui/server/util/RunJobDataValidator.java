package ch.systemsx.sybit.crkwebui.server.util;

import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

public class RunJobDataValidator 
{

	public static void validateInputParameters(InputParameters inputParameters) throws Exception
	{
		String searchMode = inputParameters.getSearchMode().toLowerCase();
		
		if(!searchMode.matches("^[a-z]+$"))
		{
			throw new Exception("Incorrect format of search mode - only a-z characters are allowed");
		}
	}
	
	public static void validateJobId(String jobId) throws Exception
	{
		if(!jobId.matches("^[A-Za-z0-9]+$"))
		{
			throw new Exception("Incorrect job id format");
		}
	}
	
	public static void validateInput(String input) throws Exception
	{
		String verificationError = verifyFileName(input);
		
		if(verificationError != null)
		{
			throw new Exception(verificationError);
		}
	}
	
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
