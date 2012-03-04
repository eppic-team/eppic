package ch.systemsx.sybit.crkwebui.server.util;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;

public class RandomDirectoryNameGenerator 
{
	public synchronized static String generateRandomDirectory(String generalDestinationDirectoryName)
	{
		String randomDirectoryName = null;
		boolean isDirectorySet = false;

		while (!isDirectorySet) 
		{
			// job name can not start from number for drmaa
			randomDirectoryName = RandomStringUtils.randomAlphabetic(1) + 
								  RandomStringUtils.randomAlphanumeric(29);

			File randomDirectory = new File(generalDestinationDirectoryName + "/" + randomDirectoryName);

			if (!randomDirectory.exists()) 
			{
				randomDirectory.mkdir();
				isDirectorySet = true;
			}
		}
		
		return randomDirectoryName;
	}
}
