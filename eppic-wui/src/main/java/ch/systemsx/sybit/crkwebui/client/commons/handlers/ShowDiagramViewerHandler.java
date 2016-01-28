package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAssemblyViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDiagramViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show viewer event handler.
 * @author AS
 */
public interface ShowDiagramViewerHandler extends EventHandler 
{
	/**
	 * Method called when viewer is to be displayed.
	 * @param event Show viewer event
	 */
	 public void onShowDiagramViewer(ShowDiagramViewerEvent event);
}
