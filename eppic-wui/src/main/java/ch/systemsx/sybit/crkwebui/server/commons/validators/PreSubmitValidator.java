package ch.systemsx.sybit.crkwebui.server.commons.validators;

import java.io.File;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Submission validator.
 * @author AS
 *
 */
public class PreSubmitValidator
{
	/**
	 * Checks whether job can be submitted. It validates existence of cif file for specified pdb code.
	 * @param localCifDir directory where cif files are stored
	 * @param pdbCode pdb code
	 * @throws ValidationException when cif file for specified pdb code can not be found in the desired location
	 */
	public static void checkIfSubmit(String localCifDir,
									 String pdbCode) throws ValidationException
	{
		pdbCode = pdbCode.toLowerCase();

		File gzCifFile = new File(localCifDir, pdbCode + ".cif.gz");

		if(!gzCifFile.exists())
		{
			throw new ValidationException("mmCIF file for specified PDB code does not exist in EPPIC's local PDB repository. Does the PDB code exist?");
		}
	}
}
