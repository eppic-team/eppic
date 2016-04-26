package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssemblyViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssemblyViewerInNewTabHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when viewer is to be displayed.
 * @author AS
 */
public class ShowAssemblyViewerInNewTabEvent extends GwtEvent<ShowAssemblyViewerInNewTabHandler> 
{
	public static Type<ShowAssemblyViewerInNewTabHandler> TYPE = new Type<ShowAssemblyViewerInNewTabHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowAssemblyViewerInNewTabHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowAssemblyViewerInNewTabHandler handler) 
	{
		handler.onShowAssemblyViewerInNewTab(this);
	}

}
