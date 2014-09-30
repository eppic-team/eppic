package ch.systemsx.sybit.crkwebui.server.jmol.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Jmol data validator.
 */
public class JmolViewerServletInputValidator 
{
	/**
	 * Validates correctness of input data necessary to run jmol viewer.
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param input input 
	 * @param size size of the picture
	 * @throws ValidationException when validation fails
	 */
	public static void validateJmolViewerInput(String jobId,
											   String interfaceId,
											   String input,
											   String size) throws ValidationException
	{
		if(jobId == null)
		{
			throw new ValidationException("Job identifier is not specified.");
		}
		else if(interfaceId == null)
		{
			throw new ValidationException("Interface identifier is not specified.");
		}
		else if(input == null)
		{
			throw new ValidationException("Input is not specified.");
		}
		else if(size == null)
		{
			throw new ValidationException("Size is not specified.");
		}
		else if(!interfaceId.matches("^[0-9]+$"))
		{
			throw new ValidationException("Interface identifier has incorrect format.");
		}
		else if(!size.matches("^[0-9]+$"))
		{
			throw new ValidationException("Size has incorrect format.");
		}
		
		RunJobDataValidator.validateJobId(jobId);
		RunJobDataValidator.validateInput(input);
	}
}
