package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.WindowHideHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when internal application windows are being closed.
 * @author AS
 */
public class WindowHideEvent extends GwtEvent<WindowHideHandler> 
{
	public static Type<WindowHideHandler> TYPE = new Type<WindowHideHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<WindowHideHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(WindowHideHandler handler) 
	{
		handler.onWindowHide(this);
	}

}
