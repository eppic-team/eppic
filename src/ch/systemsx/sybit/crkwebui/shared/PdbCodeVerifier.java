package ch.systemsx.sybit.crkwebui.shared;

/**
 * This class is used to verify correctness of the pdb code 
 *
 */
public class PdbCodeVerifier {

	private static String PDBCODE_PATTERN = "^\\d\\w\\w\\w$";
	
	public static boolean isValid(String pdbCode) {
		if (pdbCode==null) {
			return false;
		}
		if (pdbCode.matches(PDBCODE_PATTERN)) {
			return true;
		}
		return false;
	}
}
