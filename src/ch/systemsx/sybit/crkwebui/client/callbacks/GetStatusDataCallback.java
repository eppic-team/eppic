package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.StatusData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetStatusDataCallback implements AsyncCallback
{
	private MainController mainController;
	
	public GetStatusDataCallback(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during getting status data from server1");
	}

	@Override
	public void onSuccess(Object result) 
	{
		if((result != null) && (result instanceof StatusData))
		{
			StatusData statusData = (StatusData)result;
			mainController.displayStatusView(statusData);
		}
		else
		{
			mainController.showError("Error during getting status data from server2");
		}
	}

}
