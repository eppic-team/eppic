package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show viewer event handler.
 * @author AS
 */
public interface ShowViewerHandler extends EventHandler 
{
	/**
	 * Method called when viewer is to be displayed.
	 * @param event Show viewer event
	 */
	 public void onShowViewer(ShowViewerEvent event);
}
