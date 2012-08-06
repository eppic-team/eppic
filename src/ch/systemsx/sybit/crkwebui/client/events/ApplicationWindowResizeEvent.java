package ch.systemsx.sybit.crkwebui.client.events;

import ch.systemsx.sybit.crkwebui.client.handlers.ApplicationWindowResizeHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when application window was resized.
 * @author AS
 */
public class ApplicationWindowResizeEvent extends GwtEvent<ApplicationWindowResizeHandler> 
{
	public static Type<ApplicationWindowResizeHandler> TYPE = new Type<ApplicationWindowResizeHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ApplicationWindowResizeHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ApplicationWindowResizeHandler handler) 
	{
		handler.onResizeApplicationWindow(this);
	}

}
