package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;


/**
 * Crkweb service provider.
 */
public class CrkWebServiceProvider
{
	private static CrkWebServiceController serviceController = new CrkWebServiceControllerImpl();
	
	public static CrkWebServiceController getServiceController()
	{
		return serviceController;
	}
}
