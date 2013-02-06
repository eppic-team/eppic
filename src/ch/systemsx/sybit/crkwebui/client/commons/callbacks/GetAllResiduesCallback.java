package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to retrieve residues data.
 * @author srebniak_a
 *
 */
public class GetAllResiduesCallback implements AsyncCallback<InterfaceResiduesItemsList>
{
	private String jobId;

	public GetAllResiduesCallback(String jobId) 
	{
		this.jobId = jobId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_interface_residues_error(), caught));
	}

	@Override
	public void onSuccess(InterfaceResiduesItemsList result) 
	{
		if (result != null)
		{
			if(ApplicationContext.getSelectedJobId().equals(jobId))
			{
				ApplicationContext.setResiduesForInterface(result);
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
