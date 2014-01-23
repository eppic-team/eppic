package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when viewer is to be displayed.
 * @author AS
 */
public class ShowViewerEvent extends GwtEvent<ShowViewerHandler> 
{
	public static Type<ShowViewerHandler> TYPE = new Type<ShowViewerHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowViewerHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowViewerHandler handler) 
	{
		handler.onShowViewer(this);
	}

}
