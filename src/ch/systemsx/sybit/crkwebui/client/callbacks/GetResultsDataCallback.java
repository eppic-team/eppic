package ch.systemsx.sybit.crkwebui.client.callbacks;

import model.InterfaceItem;
import model.InterfaceScoreItem;
import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.google.gwt.user.client.Window;
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
		if((result != null) && (result instanceof PDBScoreItem))
		{
			PDBScoreItem resultsData = (PDBScoreItem)result;
			resultsData.setJobId(selectedId);
			mainController.setPDBScoreItem(resultsData);
			mainController.displayResultView(resultsData);
			
//			BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(InterfaceScoreItem.class);
//			BeanModel model = beanModelFactory.createModel(new InterfaceScoreItem());
//			Window.alert(model.getProperties().toString());
		}
		else
		{
			mainController.showError("Error during getting results from server");
		}
	}
}
