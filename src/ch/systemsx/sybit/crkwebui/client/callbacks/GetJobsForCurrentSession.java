package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.data.StatusData;

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
		mainController.showError("Error during getting results data from server");
	}

	@Override
	public void onSuccess(Object result) 
	{
		if(result != null)
		{
			List<StatusData> myJobs = (List<StatusData>)result;
			mainController.setJobs(myJobs);
		}
		else
		{
			mainController.showError("Error during getting results from server");
		}
	}
}
