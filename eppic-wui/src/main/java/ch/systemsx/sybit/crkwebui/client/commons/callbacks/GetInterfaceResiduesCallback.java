package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.InterfaceResiduesDataRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.model.ResidueBurial;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to retrieve residues data.
 * @author srebniak_a
 *
 */
public class GetInterfaceResiduesCallback implements AsyncCallback<HashMap<Integer, List<ResidueBurial>>>
{
	private String jobId;
	private int interfaceId;

	public GetInterfaceResiduesCallback(String jobId,
										int interfaceId) 
	{
		this.jobId = jobId;
		this.interfaceId = interfaceId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_interface_residues_error(), caught));
	}

	@Override
	public void onSuccess(HashMap<Integer, List<ResidueBurial>> result) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new ShowWaitingEvent("Loading"));
		
		if (result != null)
		{
			if(ApplicationContext.getSelectedJobId().equals(jobId) &&
			   (ApplicationContext.getSelectedInterface() == interfaceId))
			{
				EventBusManager.EVENT_BUS.fireEvent(new InterfaceResiduesDataRetrievedEvent(result));
				
//				if(GXT.isIE8)
//				{
//					if(mainController.getInterfaceResiduesItemsList() == null)
//					{
//						mainController.setResiduesForInterface(new ResiduesList());
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
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_interface_residues_error() + " - incorrect result type", 
																		   StatusMessageType.INTERNAL_ERROR));
		}
		
		EventBusManager.EVENT_BUS.fireEvent(new HideWaitingEvent());
	}
}
