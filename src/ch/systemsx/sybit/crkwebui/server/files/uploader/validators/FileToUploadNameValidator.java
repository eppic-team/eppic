package ch.systemsx.sybit.crkwebui.server.files.uploader.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

public class FileToUploadNameValidator 
{

	/**
	 * Validates correctness of name of the file to upload.
	 * @param name name to validate
	 * @throws ValidationException when validation fails
	 */
	public static void validateName(String fileName) throws ValidationException
	{
		String fileNameVerificationError = RunJobDataValidator.verifyFileName(fileName);
		
		if(fileNameVerificationError != null)
		{
			throw new ValidationException("Incorrect name of the file: " + fileNameVerificationError);
		}
	}
}
