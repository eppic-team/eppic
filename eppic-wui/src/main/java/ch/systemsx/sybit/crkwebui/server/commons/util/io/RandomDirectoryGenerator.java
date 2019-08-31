package ch.systemsx.sybit.crkwebui.server.commons.util.io;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to generate directory with random job id as name.
 * @author AS
 *
 */
public class RandomDirectoryGenerator 
{

	private static final Logger logger = LoggerFactory.getLogger(RandomDirectoryGenerator.class);

	/**
	 * Creates unique directory for randomly generated job id.
	 * @param generalDestinationDirectoryName directory where jobid subdirectory will be created
	 * @return name of the directory which was generated
	 */
	public synchronized static String generateRandomDirectory(String generalDestinationDirectoryName)
	{
		String randomDirectoryName = null;
		boolean isDirectorySet = false;

		while (!isDirectorySet) 
		{
			// job name can not start from number for drmaa
			randomDirectoryName = RandomStringUtils.randomAlphabetic(1) + 
								  RandomStringUtils.randomAlphanumeric(29);

			File randomDirectory = new File(generalDestinationDirectoryName, randomDirectoryName);

			if (!randomDirectory.exists()) 
			{
				boolean couldCreate = randomDirectory.mkdir();
				if (!couldCreate) {
					logger.warn("Could not create job directory {}", randomDirectory);
				} else {
					logger.info("Job directory {} created", randomDirectory);
				}
				isDirectorySet = true;
			}
		}
		
		return randomDirectoryName;
	}
}
