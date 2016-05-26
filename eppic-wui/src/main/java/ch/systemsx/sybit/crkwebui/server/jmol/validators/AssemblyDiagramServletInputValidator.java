package ch.systemsx.sybit.crkwebui.server.jmol.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Jmol data validator.
 */
public class AssemblyDiagramServletInputValidator 
{
	/**
	 * Validates correctness of input data necessary to run jmol viewer.
	 * @param jobId identifier of the job
	 * @param interfaces list of interface identifiers
	 * @throws ValidationException when validation fails
	 */
	public static void validateLatticeGraphInput(String jobId,
			String interfaces, String clusters, String format) throws ValidationException
	{
		if(jobId == null) {
			throw new ValidationException("Job identifier is not specified.");
		}

		RunJobDataValidator.validateJobId(jobId);
		LatticeGraphServletInputValidator.validateInterfaceList(interfaces);
		LatticeGraphServletInputValidator.validateInterfaceList(clusters); // same format as interfaces
		validateFormat(format);
	}

	private static void validateFormat(String format) throws ValidationException {
		if(format == null || format.equalsIgnoreCase("html") || format.equalsIgnoreCase("json"))
			return;
		throw new ValidationException( "Invalid format ({}). Expected html or json.");
	}

}
 