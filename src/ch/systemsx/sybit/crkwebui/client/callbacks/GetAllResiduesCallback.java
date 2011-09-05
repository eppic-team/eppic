package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to retrieve residues data
 * @author srebniak_a
 *
 */
public class GetAllResiduesCallback implements AsyncCallback<InterfaceResiduesItemsList>
{
	private MainController mainController;
	private String jobId;

	public GetAllResiduesCallback(MainController mainController,
								  String jobId) 
	{
		this.mainController = mainController;
		this.jobId = jobId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_interface_residues_error(), true);
	}

	@Override
	public void onSuccess(InterfaceResiduesItemsList result) 
	{
		if (result != null)
		{
			if(mainController.getSelectedJobId().equals(jobId))
			{
				mainController.setResiduesForInterface(result);
			}
		}
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_interface_residues_error() + " - incorrect result type", true);
		}
		
		mainController.hideWaiting();
	}
}
