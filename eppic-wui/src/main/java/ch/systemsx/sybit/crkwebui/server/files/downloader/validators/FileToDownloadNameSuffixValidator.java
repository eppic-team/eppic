package ch.systemsx.sybit.crkwebui.server.files.downloader.validators;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

public class FileToDownloadNameSuffixValidator 
{

	/**
	 * Validates correctness of suffix of the file to download.
	 * @param suffix suffix to validate
	 * @throws ValidationException when validation fails
	 */
	public static void validateSuffix(String suffix) throws ValidationException
	{
		if(suffix == null)
		{
			throw new ValidationException("No support for specified type.");
		}
	}
}
