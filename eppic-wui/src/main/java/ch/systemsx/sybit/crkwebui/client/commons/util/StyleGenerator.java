package ch.systemsx.sybit.crkwebui.client.commons.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Class to generate the styles
 * @author biyani_n
 *
 */
public class StyleGenerator {
	
	/**
	 * Adds a default font style to a text
	 * @param text
	 * @return Safe Html with default font added
	 */
	public static SafeHtml defaultFontStyle(String text){
		text = "<span class='eppic-default-font'>" + text + "</span>";
		
		SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
		
		return safeHtmlBuilder.appendHtmlConstant(text).toSafeHtml();
	}

	
	/**
	 * Adds a default font style to a text
	 * @param text
	 * @return String with default font added
	 */
	public static String defaultFontStyleString(String text){
		return "<span class='eppic-default-font'>" + text + "</span>";
	}
}
