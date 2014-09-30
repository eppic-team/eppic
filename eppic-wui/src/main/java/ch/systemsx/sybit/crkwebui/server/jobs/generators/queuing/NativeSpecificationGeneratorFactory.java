package ch.systemsx.sybit.crkwebui.server.jobs.generators.queuing;

import java.util.Properties;

/**
 * Factory class used to retrieve native specification generator for queuing system.
 * @author adam
 */
public class NativeSpecificationGeneratorFactory 
{
	/**
	 * Retrieves native specification generator for queuing system.
	 * @param queuingSystemName name of the queuing system
	 * @param queuingSystemProperties queuing system properties
	 * @return native specification generator for specified queuing system
	 */
	public static NativeSpecificationGenerator getNativeSpecificationGenerator(String queuingSystem,
			   												 				   Properties queuingSystemProperties)
	{
		NativeSpecificationGenerator nativeSpecificationGenerator = null;
	
		if(queuingSystem != null)
		{
			if(queuingSystem.equals("sge"))
			{
				nativeSpecificationGenerator = new SgeNativeSpecificationGenerator(queuingSystemProperties);
			}
		}
		
		if(nativeSpecificationGenerator == null)
		{
			nativeSpecificationGenerator = new DefaultNativeSpecificationGenerator();
		}
		
		return nativeSpecificationGenerator;
	}
}
