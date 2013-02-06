package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;


/**
 * Crkweb service provider.
 */
public class CrkWebServiceProvider
{
	private static ServiceController serviceController = new ServiceControllerImpl();
	
	public static ServiceController getServiceController()
	{
		return serviceController;
	}
}
