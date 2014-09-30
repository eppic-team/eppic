package ch.systemsx.sybit.crkwebui.server.commons.validators;

import javax.servlet.http.HttpSession;

/**
 * Class used to validate session to the server.
 * @author adam
 *
 */
public class SessionValidator 
{
	private static final String VALIDATION_ATTRIBUTE = "valid";
	
	/**
	 * Validates session by setting proper attribute.
	 * @param session session to validate
	 */
	public static void validateSession(HttpSession session)
	{
		session.setAttribute(VALIDATION_ATTRIBUTE, "true");
	}
	
	/**
	 * Invalidates session by removing validation attribute.
	 * @param session session to invalidate
	 */
	public static void invalidateSession(HttpSession session)
	{
		session.removeAttribute(VALIDATION_ATTRIBUTE);
	}
	
	/**
	 * Checks whether session has been validated.
	 * @param session session to check
	 * @return information whether session has been validated
	 */
	public static boolean isSessionValid(HttpSession session)
	{
		boolean isValid = false;
		
		if((session.getAttribute(VALIDATION_ATTRIBUTE) != null) &&
		   (session.getAttribute(VALIDATION_ATTRIBUTE).equals("true")))
		{
			isValid = true;
		}
		
		return isValid;
	}
}
