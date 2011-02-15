package ch.systemsx.sybit.crkwebui.shared;

public class EmailFieldVerifier 
{
	private static String emailPattern = "^(\\w+)([-+.][\\w]+)*@(\\w[-\\w]*\\.){1,5}([A-Za-z]){2,4}$";

	public static boolean isValid(String email) {
		if (email == null) 
		{
			return false;
		}

		if (email.matches(emailPattern)) {
			return true;
		} else {
			return false;
		}
	}
}
