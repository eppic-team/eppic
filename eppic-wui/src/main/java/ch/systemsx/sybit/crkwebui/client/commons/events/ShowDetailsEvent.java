package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDetailsHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when detailed information about interface is to be displayed.
 * @author AS
 */
public class ShowDetailsEvent extends GwtEvent<ShowDetailsHandler> 
{
	public static Type<ShowDetailsHandler> TYPE = new Type<ShowDetailsHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowDetailsHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowDetailsHandler handler) 
	{
		handler.onShowDetails(this);
	}

}
