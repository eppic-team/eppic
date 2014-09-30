package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;

/**
 * Callback used to handle xsrf validation when trying to retrieve list of jobs for current session.
 * @author srebniak_a
 *
 */
public class GetJobsForCurrentSessionCallbackXsrf implements AsyncCallback<XsrfToken>
{
	private CrkWebServiceAsync crkWebService;
	
	public GetJobsForCurrentSessionCallbackXsrf(CrkWebServiceAsync crkWebService)
	{
		this.crkWebService = crkWebService;
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
			crkWebService.getJobsForCurrentSession(new GetJobsForCurrentSessionCallback());
		}
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_xsrf_token_error() + " - incorrect type", 
																		   StatusMessageType.SYSTEM_ERROR));
		}
	}
}
