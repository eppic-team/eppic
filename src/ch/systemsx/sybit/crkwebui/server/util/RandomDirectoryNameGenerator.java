package ch.systemsx.sybit.crkwebui.server.util;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;

public class RandomDirectoryNameGenerator 
{
	public static String generateRandomDirectoryName(String generalDestinationDirectoryName)
	{
		String randomDirectoryName = null;
		boolean isDirectorySet = false;

		while (!isDirectorySet) 
		{
			randomDirectoryName = RandomStringUtils.randomAlphanumeric(30);

			File randomDirectory = new File(generalDestinationDirectoryName + "/" + randomDirectoryName);

			if (!randomDirectory.exists()) 
			{
				isDirectorySet = true;
			}
		}
		
		return randomDirectoryName;
	}
}
