package ch.systemsx.sybit.crkwebui.server.files.downloader.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Jmol data validator.
 */
public class FileDownloadServletInputValidator 
{
	/**
	 * Validates correctness of input data necessary to download specified file.
	 * @param type type of the file
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param alignment 
	 * @throws ValidationException when validation fails
	 */
	public static void validateFileDownloadInput(String type,
											   String jobId,
											   String interfaceId,
											   String alignment) throws ValidationException
	{
		if((type == null) || (type.equals("")))
		{
			throw new ValidationException("File type not specified.");
		}
		else if((jobId == null) || (jobId.equals("")))
		{
			throw new ValidationException("Job identifier not specified.");
		}
		else if(((type.equals("interface") || (type.equals("pse")))) && 
			   ((interfaceId == null) ||
			    (interfaceId.equals(""))))
	    {
			throw new ValidationException("No interface specified.");
	    }
		else if((interfaceId != null) &&
				(!interfaceId.equals("")) &&
				(!interfaceId.matches("^[0-9]+$")))
		{
			throw new ValidationException("Incorrect format of interface identifier.");
		}
		else if(type.equals("fasta") &&
				((alignment == null) ||
				(alignment.equals("")) ||
				(!alignment.matches("^[A-Za-z]$"))))
		{
			throw new ValidationException("Incorrect format of alignment.");
		}
		
		RunJobDataValidator.validateJobId(jobId);
		
	}
}
