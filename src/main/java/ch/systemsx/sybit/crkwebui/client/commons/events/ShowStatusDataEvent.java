package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowStatusDataHandler;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when status data panel is to be displayed.
 * @author AS
 */
public class ShowStatusDataEvent extends GwtEvent<ShowStatusDataHandler> 
{
	public static Type<ShowStatusDataHandler> TYPE = new Type<ShowStatusDataHandler>();
	
	/**
	 * Status data of the job.
	 */
	private final ProcessingInProgressData statusData;
	
	public ShowStatusDataEvent(ProcessingInProgressData statusData)
	{
		this.statusData = statusData;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowStatusDataHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowStatusDataHandler handler) 
	{
		handler.onShowStatusData(this);
	}

	/**
	 * Retrieves status data of the job.
	 * @return status data of the job
	 */
	public ProcessingInProgressData getStatusData() {
		return statusData;
	}
}
