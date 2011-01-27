package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.data.ResultsData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetResultsDataCallback implements AsyncCallback
{
	private MainController mainController;
	
	private String selectedId;
	
	public GetResultsDataCallback(MainController mainController,
								  String selectedId)
	{
		this.mainController = mainController;
		this.selectedId = selectedId;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during getting results data from server");
	}

	@Override
	public void onSuccess(Object result) 
	{
		if((result != null) && (result instanceof ResultsData))
		{
			ResultsData resultsData = (ResultsData)result;
			resultsData.setJobId(selectedId);
			mainController.displayResultView(resultsData);
		}
		else
		{
			mainController.showError("Error during getting results from server");
		}
	}
}
