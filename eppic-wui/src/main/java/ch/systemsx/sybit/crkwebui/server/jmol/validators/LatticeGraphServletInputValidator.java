package ch.systemsx.sybit.crkwebui.server.jmol.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Jmol data validator.
 */
public class LatticeGraphServletInputValidator 
{
	/**
	 * Validates correctness of input data necessary to run jmol viewer.
	 * @param jobId identifier of the job
	 * @param type
	 * @param interfaceId identifier of the interface
	 * @param assemblyId assembly identifier
	 * @param format the format of the coordinates file to use (either pdb or cif)
	 * @param input input 
	 * @param size size of the picture
	 * @throws ValidationException when validation fails
	 */
	public static void validateLatticeGraphInput(String jobId,String input,
			String interfaces) throws ValidationException
	{
		if(jobId == null) {
			throw new ValidationException("Job identifier is not specified.");
		}
		else if(input == null)
		{
			throw new ValidationException("Input is not specified.");
		}

		RunJobDataValidator.validateJobId(jobId);
		RunJobDataValidator.validateInput(input);
		validateInterfaceList(interfaces);
	}

	/**
	 * Valid examples: "1", "1,2,3", "*", ""
	 * Invalid: "one", null
	 * @param interfaces
	 * @throws ValidationException
	 */
	private static void validateInterfaceList(String interfaces) throws ValidationException {
		if(interfaces != null && !interfaces.matches("^(\\*?|[0-9]+(,[0-9]+)*)$"))
		{
			throw new ValidationException( "Invalid interfaces ({}). Expected '*' or comma-separated list of integers");
		}
	}
}
 