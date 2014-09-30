package ch.systemsx.sybit.crkwebui.server.jobs.generators.queuing;

import ch.systemsx.sybit.crkwebui.shared.exceptions.NativeSpecificationException;

/**
 * Interface for native specification generators for queuing systems.
 * @author adam
 */
public interface NativeSpecificationGenerator 
{
	/**
	 * Generates native specification for submission.
	 * @param nrOfThreadsForSubmission nr of threads used to run command
	 * @return native specification
	 * @throws NativeSpecificationException when can not generate native specification
	 */
	public String generateNativeSpecificationForSubmission(int nrOfThreadsForSubmission) throws NativeSpecificationException;
}
