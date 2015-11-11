package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssemblyViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDiagramViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when viewer is to be displayed.
 * @author AS
 */
public class ShowDiagramViewerEvent extends GwtEvent<ShowDiagramViewerHandler> 
{
	public static Type<ShowDiagramViewerHandler> TYPE = new Type<ShowDiagramViewerHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowDiagramViewerHandler> getAssociatedType() 
	{
		return TYPE; 
	}

	@Override
	protected void dispatch(ShowDiagramViewerHandler handler) 
	{
		handler.onShowDiagramViewer(this);
	}

}
