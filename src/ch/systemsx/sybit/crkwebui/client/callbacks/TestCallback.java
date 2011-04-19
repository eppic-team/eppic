package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TestCallback implements AsyncCallback<String>
{
	private MainController mainController;
	
	public TestCallback(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during getting data from server");
	}

	@Override
	public void onSuccess(String result) 
	{
		if(result != null)
		{
			String resultData = (String)result;
			Window.alert(resultData);
		}
		else
		{
			mainController.showError("Error during getting data from server");
		}
	}


}
