package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetSettingsCallback implements AsyncCallback 
{
	private MainController mainController;
	
	
	public GetSettingsCallback(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during getting data from server");
	}

	@Override
	public void onSuccess(Object result) 
	{
		if((result != null) && (result instanceof ApplicationSettings))
		{
			ApplicationSettings settings  = (ApplicationSettings) result;
			mainController.setSettings(settings);
			mainController.runAutoRefresh();
			History.fireCurrentHistoryState();
		}
		else
		{
			mainController.showError("Error during getting settings from server");
		}
	}
}
