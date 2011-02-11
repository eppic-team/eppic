package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class KillJobCallback implements AsyncCallback {
	private MainController mainController;
	private String selectedId;

	public KillJobCallback(MainController mainController, String selectedId) {
		this.mainController = mainController;
		this.selectedId = selectedId;
	}

	@Override
	public void onFailure(Throwable caught) {
		mainController.showError("Error during getting data from server");
	}

	@Override
	public void onSuccess(Object result) {
		if ((result != null) && (result instanceof String)) {
			String resultInfo = (String) result;
			mainController.showError(resultInfo);
			mainController.getJobsForCurrentSession();
		} else {
			mainController.showError("Error during getting data from server");
		}
	}

}
