package ch.systemsx.sybit.crkwebui.shared.validators;

/**
 * Pdb code verifier.
 *
 */
public class PdbCodeVerifier {

	public static final String PDBCODE_PATTERN = "^\\d\\w\\w\\w$";

	/**
	 * Checks whether provided string is correct pdb code.
	 * @param pdbCode text to validate
	 * @return true if valid, false otherwise
	 */
	public static boolean isValid(String pdbCode)
	{
		if(pdbCode==null)
		{
			return false;
		}

		return isNotNullStringPdbCode(pdbCode);
	}

	/**
	 * Checks whether trimmed string is correct pdb code.
	 * @param pdbCode text to validate
	 * @return true if valid, false otherwise
	 */
	public static boolean isTrimmedValid(String pdbCode)
	{
		if (pdbCode == null)
		{
			return false;
		}

		return isNotNullStringPdbCode(pdbCode.trim());
	}

	/**
	 * Checks whether provide not null string is valid pdb code.
	 * @param pdbCode string to validate
	 * @return true if valid, false otherwise
	 */
	private static boolean isNotNullStringPdbCode(String pdbCode)
	{
		if(pdbCode.matches(PDBCODE_PATTERN))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
