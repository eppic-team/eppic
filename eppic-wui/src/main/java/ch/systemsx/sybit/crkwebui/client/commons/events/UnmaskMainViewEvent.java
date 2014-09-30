package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.UnmaskMainViewHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when main panel is to be unmasked.
 * @author AS
 */
public class UnmaskMainViewEvent extends GwtEvent<UnmaskMainViewHandler> 
{
	public static Type<UnmaskMainViewHandler> TYPE = new Type<UnmaskMainViewHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<UnmaskMainViewHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(UnmaskMainViewHandler handler) 
	{
		handler.onUnmaskMainView(this);
	}
}
