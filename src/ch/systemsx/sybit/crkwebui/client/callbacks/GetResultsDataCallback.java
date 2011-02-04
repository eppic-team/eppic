package ch.systemsx.sybit.crkwebui.client.callbacks;

import model.PdbScore;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

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
		if((result != null) && (result instanceof PdbScore))
		{
			PdbScore resultsData = (PdbScore)result;
			resultsData.setJobId(selectedId);
			mainController.displayResultView(resultsData);
			
//			BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(InterfaceScore.class);
//			BeanModel model = beanModelFactory.createModel(resultsData.getInterfaceScoreMap().get(1));
//			Window.alert(model.getProperties().toString());
		}
		else
		{
			mainController.showError("Error during getting results from server");
		}
	}
}
