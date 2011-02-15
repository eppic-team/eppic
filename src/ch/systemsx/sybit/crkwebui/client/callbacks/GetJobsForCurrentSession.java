package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetJobsForCurrentSession implements AsyncCallback 
{
	private MainController mainController;
	
	public GetJobsForCurrentSession(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during getting current jobs from server. " + caught.getMessage());
	}

	@Override
	public void onSuccess(Object result) 
	{
		if(result != null)
		{
			List<ProcessingInProgressData> myJobs = (List<ProcessingInProgressData>)result;
			mainController.setJobs(myJobs);
		}
		else
		{
			mainController.showError("Error during getting current jobs for current session");
		}
	}
}
