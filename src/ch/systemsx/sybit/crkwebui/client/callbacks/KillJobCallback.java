package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class KillJobCallback implements AsyncCallback 
{
	private MainController mainController;

	public KillJobCallback(MainController mainController) {
		this.mainController = mainController;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel("Error during stopping the job: ", true);
//		mainController.showError("Error during getting data from server: " + caught.getMessage());
	}

	@Override
	public void onSuccess(Object result) 
	{
		if ((result != null) && (result instanceof String)) 
		{
			String resultInfo = (String) result;
			mainController.showMessage("Job stopping",resultInfo);
			mainController.getJobsForCurrentSession();
			mainController.getCurrentStatusData();
		} 
		else 
		{
			mainController.updateStatusLabel("Error during stopping the job " + mainController.getSelectedJobId(), true);
//			mainController.showError("Error during stopping the job " + mainController.getSelectedJobId());
		}
	}

}
