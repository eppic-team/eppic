package ch.systemsx.sybit.crkwebui.client.commons.services.xsrf;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

/**
 * Xsrf service provider.
 */
public class XsrfTokenServiceProvider
{
	/**
	 * Create a remote service proxy to talk to the server-side xsrf service.
	 */
	private static final XsrfTokenServiceAsync xsrfTokenService = GWT.create(XsrfTokenService.class);

	static {
		((ServiceDefTarget)xsrfTokenService).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
	}
	
	/**
	 * Retrieves xsrf service.
	 * @return xsrf service
	 */
	public static XsrfTokenServiceAsync getXsrfTokenService()
	{
		return xsrfTokenService;
	}
}
