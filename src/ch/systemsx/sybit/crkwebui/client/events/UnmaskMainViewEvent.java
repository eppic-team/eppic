package ch.systemsx.sybit.crkwebui.client.events;

import ch.systemsx.sybit.crkwebui.client.handlers.UnmaskMainViewHandler;

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
