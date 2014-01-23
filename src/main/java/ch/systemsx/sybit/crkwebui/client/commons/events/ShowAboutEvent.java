package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAboutHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when about window is to be displayed.
 * @author AS
 */
public class ShowAboutEvent extends GwtEvent<ShowAboutHandler> 
{
	public static Type<ShowAboutHandler> TYPE = new Type<ShowAboutHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowAboutHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowAboutHandler handler) 
	{
		handler.onShowAbout(this);
	}

}
