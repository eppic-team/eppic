package ch.systemsx.sybit.crkwebui.server.jobs.generators.queuing;

/**
 * Default native specification generator. It's used when no queuing system specific one is provided.
 * @author AS
 *
 */
public class DefaultNativeSpecificationGenerator implements NativeSpecificationGenerator
{
	@Override
	public String generateNativeSpecificationForSubmission(int nrOfThreadsForSubmission)
	{
		return null;
	}
	
}
