package ch.systemsx.sybit.crkwebui.server.jmol.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Jmol data validator.
 */
public class JmolViewerServletInputValidator 
{
	/**
	 * Validates correctness of input data necessary to run jmol viewer.
	 * @param jobId identifier of the job
	 * @param type
	 * @param interfaceId identifier of the interface
	 * @param assemblyId assembly identifier
	 * @param format the format of the coordinates file to use (either pdb or cif)
	 * @param input input 
	 * @param size size of the picture
	 * @throws ValidationException when validation fails
	 */
	public static void validateJmolViewerInput(String jobId,
											   String type,
											   String interfaceId,
											   String assemblyId,
											   String format,
											   String input,
											   String size) throws ValidationException
	{
		if(jobId == null) {
			throw new ValidationException("Job identifier is not specified.");
		}
		else if (type==null) {
			throw new ValidationException("Type is not specified.");
		}
		else if (!type.equals(FileDownloadServlet.TYPE_VALUE_INTERFACE) && 
				!type.equals(FileDownloadServlet.TYPE_VALUE_ASSEMBLY)) {
			throw new ValidationException("Type is not correctly specified. Can only be "+
					FileDownloadServlet.TYPE_VALUE_INTERFACE + " or " + FileDownloadServlet.TYPE_VALUE_ASSEMBLY);
		}
		else if(type.equals(FileDownloadServlet.TYPE_VALUE_INTERFACE) && interfaceId == null) {
			throw new ValidationException("Interface identifier is not specified.");
		}
		else if (type.equals(FileDownloadServlet.TYPE_VALUE_ASSEMBLY) && assemblyId == null) {
			throw new ValidationException("Assembly identifier is not specified.");
		}
		else if(format == null) {
			throw new ValidationException("Coordinates file format is not specified.");
		}
		else if (!format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_PDB) && 
				!format.equals(FileDownloadServlet.COORDS_FORMAT_VALUE_CIF)) {
			throw new ValidationException("Coordinates format value not correctly specified. Can only be "+
					FileDownloadServlet.COORDS_FORMAT_VALUE_PDB + " or " + FileDownloadServlet.COORDS_FORMAT_VALUE_CIF);
		}
		else if(input == null)
		{
			throw new ValidationException("Input is not specified.");
		}
		else if(size == null)
		{
			throw new ValidationException("Size is not specified.");
		}
		else if(!interfaceId.matches("^[0-9]+$"))
		{
			throw new ValidationException("Interface identifier has incorrect format.");
		}
		else if (!assemblyId.matches("^[0-9]+$")) {
			throw new ValidationException("Assembly identifier has incorrect format.");
		}
		else if(!size.matches("^[0-9]+$"))
		{
			throw new ValidationException("Size has incorrect format.");
		}
		
		RunJobDataValidator.validateJobId(jobId);
		RunJobDataValidator.validateInput(input);
	}
}
 