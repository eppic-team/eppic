package ch.systemsx.sybit.crkwebui.server.jobs.generators.queuing;

import java.util.Properties;

import ch.systemsx.sybit.crkwebui.shared.exceptions.NativeSpecificationException;

/**
 * Native specification generator for SGE.
 * @author AS
 */
public class SgeNativeSpecificationGenerator implements NativeSpecificationGenerator
{
	private Properties queuingSystemProperties;
	
	public SgeNativeSpecificationGenerator(Properties queuingSystemProperties)
	{
		this.queuingSystemProperties = queuingSystemProperties;
	}
	
	@Override
	public String generateNativeSpecificationForSubmission(int nrOfThreadsForSubmission) throws NativeSpecificationException 
	{
		String nativeSpecification = null;
		
		if(nrOfThreadsForSubmission > 1)
		{
			String parallelEnvironment = queuingSystemProperties.getProperty("parallel_environment");
			if(parallelEnvironment == null)
			{
				throw new NativeSpecificationException("Parallel environment property not specified");
			}
			
			nativeSpecification = "-pe " + parallelEnvironment + " " + nrOfThreadsForSubmission; 
		}
		
		return nativeSpecification;
	}
	
}
