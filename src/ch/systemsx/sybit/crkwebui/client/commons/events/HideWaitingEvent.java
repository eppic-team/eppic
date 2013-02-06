package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideWaitingHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when waiting window is to be closed.
 * @author AS
 */
public class HideWaitingEvent extends GwtEvent<HideWaitingHandler> 
{
	public static Type<HideWaitingHandler> TYPE = new Type<HideWaitingHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<HideWaitingHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(HideWaitingHandler handler) 
	{
		handler.onHideWaiting(this);
	}

}
