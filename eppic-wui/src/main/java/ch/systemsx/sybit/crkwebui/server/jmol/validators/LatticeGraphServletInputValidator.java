package ch.systemsx.sybit.crkwebui.server.jmol.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Lattice graph data validator.
 */
public class LatticeGraphServletInputValidator 
{
	/**
	 * Validates correctness of input data necessary to run jmol viewer.
	 * @param jobId identifier of the job
	 * @param interfaces list of interface identifiers
	 * @param clusters
	 * @param assembly
	 * @throws ValidationException when validation fails
	 */
	public static void validateLatticeGraphInput(String jobId,
			String interfaces, String clusters, String assembly) throws ValidationException
	{
		if(jobId == null) {
			throw new ValidationException("Job identifier is not specified.");
		}

		RunJobDataValidator.validateJobId(jobId);
		validateInterfaceList(interfaces);
		validateInterfaceList(clusters); // same format as interfaces
		validateAssemblyId(assembly);
	}

	/**
	 * Valid examples: "1", "1,2,3", "*", ""
	 * Invalid: "one", null
	 * @param interfaces
	 * @throws ValidationException
	 */
	public static void validateInterfaceList(String interfaces) throws ValidationException {
		// Either '*' or a non-whitespace version of IntervalSet.isValidSelectionString(interfaces);
		if(interfaces != null && !interfaces.matches("^(\\*?|\\d+(-\\d+)?(,\\d+(-\\d+)?)*)$"))
		{
			throw new ValidationException( "Invalid interfaces ("+interfaces+"). Expected '*' or comma-separated list of integers");
		}
	}

	public static void validateAssemblyId(String assembly) throws ValidationException {
		if (assembly!=null && !assembly.matches("^\\d+$")) {
			throw new ValidationException( "Invalid assembly id ("+assembly+"). Expected integer.");
		}
	}
}
 