package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.RefreshStatusDataHandler;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when status panel is to be refreshed.
 * @author AS
 */
public class RefreshStatusDataEvent extends GwtEvent<RefreshStatusDataHandler> 
{
	public static Type<RefreshStatusDataHandler> TYPE = new Type<RefreshStatusDataHandler>();
	
	/**
	 * Status data of the job to display.
	 */
	private final ProcessingInProgressData statusData;
	
	public RefreshStatusDataEvent(ProcessingInProgressData statusData)
	{
		this.statusData = statusData;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<RefreshStatusDataHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(RefreshStatusDataHandler handler) 
	{
		handler.onRefreshStatusData(this);
	}

	/**
	 * Retrieves status data of the job to display.
	 * @return status data
	 */
	public ProcessingInProgressData getStatusData() {
		return statusData;
	}
}
