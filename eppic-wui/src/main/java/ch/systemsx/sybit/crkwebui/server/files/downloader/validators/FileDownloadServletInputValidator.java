package ch.systemsx.sybit.crkwebui.server.files.downloader.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Jmol data validator.
 */
public class FileDownloadServletInputValidator 
{
	/**
	 * Validates correctness of input data necessary to download specified file.
	 * @param type type of the file
	 * @param jobId identifier of the job
	 * @param interfaceId identifier of the interface
	 * @param repChainId 
	 * @throws ValidationException when validation fails
	 */
	public static void validateFileDownloadInput(String type,
											   String jobId,
											   String interfaceId,
											   String assemblyId,
											   String repChainId,
											   String format) throws ValidationException
	{
		if((type == null) || (type.equals("")))
		{
			throw new ValidationException("File type not specified.");
		} else if (format == null || format.equals("")) {
			throw new ValidationException("Coordinates file format not specified");
		}
		else if((jobId == null) || (jobId.equals("")))
		{
			throw new ValidationException("Job identifier not specified.");
		}
		// if type=interface, then an interfaceId must be specified
		else if(((type.equals(FileDownloadServlet.TYPE_VALUE_INTERFACE) )) && 
			   ((interfaceId == null) ||
			    (interfaceId.equals("")))) {
			throw new ValidationException("No interface id specified.");
	    }
		else if((interfaceId != null) &&
				(!interfaceId.equals("")) &&
				(!interfaceId.matches("^[0-9]+$"))) {
			throw new ValidationException("Incorrect format of interface identifier.");
		}
		// if type=assembly, then an assemblyId must be specified
		else if (type.equals(FileDownloadServlet.TYPE_VALUE_ASSEMBLY) &&
				(assemblyId==null || assemblyId.equals("")) ) {
			throw new ValidationException("No assembly id specified.");
		}
		else if ((type.equals(FileDownloadServlet.TYPE_VALUE_INTERFACE) || 
				  type.equals(FileDownloadServlet.TYPE_VALUE_ASSEMBLY)) &&
				  (format==null || format.equals("")) ) {
			throw new ValidationException("No coordinates file format specified for interface or assembly.");
			
		}
		// if type=msa, then a repChainId must be specified
		else if(type.equals(FileDownloadServlet.TYPE_VALUE_MSA) &&
				((repChainId == null) ||
				(repChainId.equals("")) ||
				(!repChainId.matches("^[A-Za-z]+$")))) 
		{
			throw new ValidationException("Incorrect format of alignment.");
		}
		
		RunJobDataValidator.validateJobId(jobId);
		
	}
}
