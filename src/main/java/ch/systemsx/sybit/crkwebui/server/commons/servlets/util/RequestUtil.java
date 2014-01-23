package ch.systemsx.sybit.crkwebui.server.commons.servlets.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

	/**
	 * Checks whether compression of response is allowed in the browser.
	 * @param request servlet request
	 * @return information whether browser supports compression
	 */
	public static boolean checkIfAcceptGzipEncoding(HttpServletRequest request)
	{
		boolean acceptGzipEncoding = false;
		
		String acceptEncodingHeader = request.getHeader("Accept-Encoding");
		if((acceptEncodingHeader != null) &&
		   (acceptEncodingHeader.contains("gzip")))
		{
			acceptGzipEncoding = true;
		}
		
		return acceptGzipEncoding;
	}
}
