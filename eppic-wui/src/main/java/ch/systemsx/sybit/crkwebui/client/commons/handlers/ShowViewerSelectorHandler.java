package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerSelectorEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show viewer selector event handler.
 * @author nikhil
 */
public interface ShowViewerSelectorHandler extends EventHandler 
{
	/**
	 * Method called when viewer selector window is to be displayed.
	 * @param event Show about event
	 */
	 public void onShowWindow(ShowViewerSelectorEvent event);
}
