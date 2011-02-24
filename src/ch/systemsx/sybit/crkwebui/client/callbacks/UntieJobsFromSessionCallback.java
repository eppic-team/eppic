package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UntieJobsFromSessionCallback implements AsyncCallback 
{
	private MainController mainController;
	
	public UntieJobsFromSessionCallback(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel("Error during closing session", true);
//		mainController.showError("Error during getting data from server");
	}

	@Override
	public void onSuccess(Object result) 
	{
		if((result != null) && (result instanceof String))
		{
			mainController.updateStatusLabel("Error during cleaning data: " + (String)result, true);
//			mainController.showError("Error during cleaning data: " + (String)result);
		}
		else
		{
//			mainController.getJobsForCurrentSession();
		}
	}
}
