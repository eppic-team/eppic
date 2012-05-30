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
		if (pdbCode==null) 
		{
			return false;
		}
		
		if (pdbCode.matches(PDBCODE_PATTERN)) 
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
