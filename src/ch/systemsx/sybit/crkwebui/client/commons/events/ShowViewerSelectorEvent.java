package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerSelectorHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when viewer selector window is to be displayed.
 * @author nikhil
 */
public class ShowViewerSelectorEvent extends GwtEvent<ShowViewerSelectorHandler> 
{
	public static Type<ShowViewerSelectorHandler> TYPE = new Type<ShowViewerSelectorHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowViewerSelectorHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowViewerSelectorHandler handler) 
	{
		handler.onShowWindow(this);
	}

}