package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.HideWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;
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
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_interface_residues_error(), true));
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
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_interface_residues_error() + " - incorrect result type", true));
		}
		
		EventBusManager.EVENT_BUS.fireEvent(new HideWaitingEvent());
	}
}
