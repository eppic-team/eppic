package ch.systemsx.sybit.crkwebui.client.gui.util;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;

/**
 * Generator of escaped strings.
 * @author adam
 *
 */
public class EscapedStringGenerator 
{
	/**
	 * Creates html escaped version of the string.
	 * @param stringToEscape string to escape
	 * @return escaped string
	 */
	public static String generateEscapedString(String stringToEscape)
	{
		String escapedString = null;
		
		if(stringToEscape != null)
		{
			SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
			escapedString = safeHtmlBuilder.appendEscaped(stringToEscape).toSafeHtml().asString();
		}
		
		return escapedString;
	}
	
	/**
	 * Produces instances of SafeHtml from input strings by applying a simple sanitization algorithm at run-time.
	 * @param stringToSanitaze string to sanitize
	 * @return sanitized string
	 */
	public static String generateSanitizedString(String stringToSanitaze)
	{
		String sanitazedString = null;
		
		if(stringToSanitaze != null)
		{
			ExtendedHtmlSanitizer extendedHtmlSanitizer = new ExtendedHtmlSanitizer();
			sanitazedString = extendedHtmlSanitizer.sanitize(stringToSanitaze).asString();
		}
		
		return sanitazedString;
	}
}
