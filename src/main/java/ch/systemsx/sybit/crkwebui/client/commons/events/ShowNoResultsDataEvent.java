package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowNoResultsDataHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when no data for specified id was retrieved. 
 *
 */
public class ShowNoResultsDataEvent extends GwtEvent<ShowNoResultsDataHandler> 
{
	public static Type<ShowNoResultsDataHandler> TYPE = new Type<ShowNoResultsDataHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowNoResultsDataHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowNoResultsDataHandler handler) 
	{
		handler.onShowNoResultsData(this);
	}
}