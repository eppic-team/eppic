/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowJobsPanelEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handles the event to show the jobs panel
 * @author nikhil
 *
 */
public interface ShowJobsPanelHandler extends EventHandler {
	
	/**
	 * Method called when jobs panel is to be shown.
	 * @param event
	 */
	 public void onShowJobsPanel(ShowJobsPanelEvent event);

}