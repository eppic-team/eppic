package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle response when closing connection with the server
 * @author srebniak_a
 *
 */
public class UntieJobsFromSessionCallback implements AsyncCallback<Void> 
{
	private MainController mainController;
	
	public UntieJobsFromSessionCallback(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_untie_jobs_from_session_error(), true);
	}

	@Override
	public void onSuccess(Void result) 
	{

	}
}
