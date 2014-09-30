package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideAllWindowsHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when all internal windows are to be closed.
 * @author AS
 */
public class HideAllWindowsEvent extends GwtEvent<HideAllWindowsHandler> 
{
	public static Type<HideAllWindowsHandler> TYPE = new Type<HideAllWindowsHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<HideAllWindowsHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(HideAllWindowsHandler handler) 
	{
		handler.onHideAllWindows(this);
	}

}
