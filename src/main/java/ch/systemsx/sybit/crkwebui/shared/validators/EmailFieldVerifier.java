package ch.systemsx.sybit.crkwebui.shared.validators;

/**
 * Verifier of the email names.
 * @author srebniak_a
 *
 */
public class EmailFieldVerifier 
{
	public static final String EMAIL_PATTERN = "^(\\w+)([-+.][\\w]+)*@(\\w[-\\w]*\\.){1,5}([A-Za-z]){2,4}$";

	/**
	 * Checks whether provided string is email address.
	 * @param email string to validate
	 * @return true if valid, false otherwise
	 */
	public static boolean isValid(String email)
	{
		if (email == null) 
		{
			email = "";
		}
		if (email == "")
		{
			return true;
		}
		if (email.matches(EMAIL_PATTERN)) 
		{
			return true;
		}
		else 
		{
			return false;
		}
	}
}
