package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to retrieve residues data.
 * @author srebniak_a
 *
 */
public class GetInterfaceResiduesCallback implements AsyncCallback<HashMap<Integer, List<InterfaceResidueItem>>>
{
	private MainController mainController;
	private String jobId;
	private int interfaceId;

	public GetInterfaceResiduesCallback(MainController mainController,
										String jobId,
										int interfaceId) 
	{
		this.mainController = mainController;
		this.jobId = jobId;
		this.interfaceId = interfaceId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(AppPropertiesManager.CONSTANTS.callback_get_interface_residues_error(), true);
	}

	@Override
	public void onSuccess(HashMap<Integer, List<InterfaceResidueItem>> result) 
	{
		mainController.showWaiting("Loading");
		
		if (result != null)
		{
			if(mainController.getSelectedJobId().equals(jobId) &&
			   (mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() == interfaceId))
			{
				mainController.setInterfacesResiduesWindowData(result);
				
//				if(GXT.isIE8)
//				{
//					if(mainController.getInterfaceResiduesItemsList() == null)
//					{
//						mainController.setResiduesForInterface(new InterfaceResiduesItemsList());
//					}
//					
//					if(mainController.getInterfaceResiduesItemsList().get(interfaceId) == null)
//					{
//						mainController.getInterfaceResiduesItemsList().put(interfaceId, result);
//					}
//				}
			}
		}
		else 
		{
			mainController.updateStatusLabel(AppPropertiesManager.CONSTANTS.callback_get_interface_residues_error() + " - incorrect result type", true);
		}
		
		mainController.hideWaiting();
	}
}
