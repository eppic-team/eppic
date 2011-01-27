package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CheckIfDataProcessedCallback implements AsyncCallback
{
	private MainController mainController;
	private String selectedId;
	
	public CheckIfDataProcessedCallback(MainController mainController,
										String selectedId)
	{
		this.mainController = mainController;
		this.selectedId = selectedId;
	}
	
	@Override
	public void onFailure(Throwable caught) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSuccess(Object result) 
	{
		if((result != null) && 
		   (result instanceof Boolean) &&
		   (((Boolean)result).booleanValue() == true))
		{
			mainController.getResultData(selectedId);
		}
		else
		{
			mainController.getStatusData(selectedId);
		}
		
	}

}
