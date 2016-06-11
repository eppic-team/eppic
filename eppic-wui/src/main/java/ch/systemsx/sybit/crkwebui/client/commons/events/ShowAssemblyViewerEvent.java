package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssemblyViewerHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when viewer is to be displayed.
 * @author AS
 */
public class ShowAssemblyViewerEvent extends GwtEvent<ShowAssemblyViewerHandler> 
{
	public static Type<ShowAssemblyViewerHandler> TYPE = new Type<ShowAssemblyViewerHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowAssemblyViewerHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowAssemblyViewerHandler handler) 
	{
		handler.onShowAssemblyViewer(this);
	}

}
