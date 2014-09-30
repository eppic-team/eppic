package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.StopJobsListAutoRefreshHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when autorefreshing of jobs list should be stopped.
 * @author AS
 */
public class StopJobsListAutoRefreshEvent extends GwtEvent<StopJobsListAutoRefreshHandler> 
{
	public static Type<StopJobsListAutoRefreshHandler> TYPE = new Type<StopJobsListAutoRefreshHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<StopJobsListAutoRefreshHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(StopJobsListAutoRefreshHandler handler) 
	{
		handler.onStopJobsListAutoRefresh(this);
	}
}
