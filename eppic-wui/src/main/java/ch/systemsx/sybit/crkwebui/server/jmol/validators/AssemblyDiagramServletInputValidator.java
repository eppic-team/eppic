package ch.systemsx.sybit.crkwebui.server.jmol.validators;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Assembly diagram data validator.
 */
public class AssemblyDiagramServletInputValidator 
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
		// Same parameters as the LatticeGraph
		LatticeGraphServletInputValidator.validateLatticeGraphInput(jobId, interfaces, clusters, assembly);
	}
}
 