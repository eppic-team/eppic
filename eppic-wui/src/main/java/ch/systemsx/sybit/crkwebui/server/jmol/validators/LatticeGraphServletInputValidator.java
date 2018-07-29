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
	 * @param clusters list if interface cluster identifiers
	 * @param assembly assembly identifier
	 * @throws ValidationException when validation fails
	 */
	public static void validateLatticeGraphInput(String jobId,
			String interfaces, String clusters, String assembly) throws ValidationException
	{
		if(jobId == null) {
			throw new ValidationException("Job identifier is not specified.");
		}

		if (interfaces == null && clusters == null && assembly == null) {
			throw new ValidationException("Either interface ids, interface cluster ids or assembly id must be specified.");
		}

		RunJobDataValidator.validateJobId(jobId);
		validateIdList(interfaces);
		validateIdList(clusters); // same format as interfaces
		validateAssemblyId(assembly);
	}

	/**
	 * Valid examples: "1", "1,2,3", "*"
	 * Invalid: "one", null
	 * @param ids the list of integer identifiers
	 * @throws ValidationException
	 */
	public static void validateIdList(String ids) throws ValidationException {

		String msg = "Invalid id list ("+ids+"). Expected '*' or comma-separated list of integers, with hyphens for intervals.";

		if (ids!=null && ids.isEmpty()) {
			throw new ValidationException(msg);
		}

		// Either '*' or a non-whitespace version of IntervalSet.isValidSelectionString(interfaces);
		if(ids != null && !ids.matches("^(\\*?|\\d+(-\\d+)?(,\\d+(-\\d+)?)*)$"))
		{
			throw new ValidationException(msg);
		}
	}

	public static void validateAssemblyId(String assembly) throws ValidationException {
		if (assembly!=null && !assembly.matches("^\\d+$")) {
			throw new ValidationException( "Invalid assembly id ("+assembly+"). Expected integer.");
		}
	}
}
 