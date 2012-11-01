package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.CrkWebServiceAsync;
import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;

/**
 * Callback used to handle xsrf validation when trying to delete the job.
 * @author srebniak_a
 *
 */
public class DeleteJobCallbackXsrf implements AsyncCallback<XsrfToken>
{
	private CrkWebServiceAsync crkWebService;
	
	private String jobToDelete;
	
	public DeleteJobCallbackXsrf(CrkWebServiceAsync crkWebService,
							   String jobToDelete)
	{
		this.crkWebService = crkWebService;
		this.jobToDelete = jobToDelete;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_xsrf_token_error(), caught));
	}

	@Override
	public void onSuccess(XsrfToken token)
	{
		if (token != null)
		{
			((HasRpcToken)crkWebService).setRpcToken(token);
			crkWebService.deleteJob(jobToDelete, new DeleteJobCallback(jobToDelete));
		}
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_xsrf_token_error() + " - incorrect type", 
																		   StatusMessageType.SYSTEM_ERROR));
		}
	}
}
