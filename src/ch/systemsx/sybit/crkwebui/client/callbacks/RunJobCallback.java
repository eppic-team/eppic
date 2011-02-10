package ch.systemsx.sybit.crkwebui.client.callbacks;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

public class RunJobCallback implements AsyncCallback 
{
	private MainController mainController;
	
	public RunJobCallback(MainController mainController)
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
		mainController.getJobsForCurrentSession();
//		if((result != null) && (result instanceof String))
//		{
//			String resultInfo = (String)result;
//			mainController.showError(resultInfo);
//			mainController.getJobsForCurrentSession();
//		}
//		else
//		{
//			mainController.showError("Error during getting data from server");
//		}
	}
}
