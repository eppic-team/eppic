package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;

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
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_untie_jobs_from_session_error(), true));
	}

	@Override
	public void onSuccess(Void result) 
	{

	}
}
