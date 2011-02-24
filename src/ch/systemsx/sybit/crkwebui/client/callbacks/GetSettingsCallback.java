package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetSettingsCallback implements AsyncCallback 
{
	private MainController mainController;

	public GetSettingsCallback(MainController mainController) {
		this.mainController = mainController;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel("Error during loading settings. " + caught.getMessage(), true);
//		mainController.showError("Error during loading settings. " + caught.getMessage());
	}

	@Override
	public void onSuccess(Object result)
	{
		if ((result != null) && (result instanceof ApplicationSettings)) 
		{
			ApplicationSettings settings = (ApplicationSettings) result;
			mainController.setSettings(settings);
			mainController.setNrOfSubmissions(settings.getNrOfJobsForSession());
			mainController.setMainView();
			mainController.runMyJobsAutoRefresh();
			History.fireCurrentHistoryState();
		}
		else 
		{
			mainController.updateStatusLabel("Error during getting settings from server", true);
//			mainController.showError("Error during getting settings from server");
		}
	}
}
