package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle response when closing connection with the server.
 * @author srebniak_a
 *
 */
public class UntieJobsFromSessionCallback implements AsyncCallback<Void> 
{
	public UntieJobsFromSessionCallback()
	{
		
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_untie_jobs_from_session_error(), 
																	   caught));
	}

	@Override
	public void onSuccess(Void result) 
	{

	}
}
