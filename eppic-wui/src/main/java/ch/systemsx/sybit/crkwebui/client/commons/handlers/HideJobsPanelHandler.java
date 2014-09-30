package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.HideJobsPanelEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Hide jobs panel handler.
 * @author nikhil
 */
public interface HideJobsPanelHandler extends EventHandler {
	
	/**
	 * Method called when jobs panel is to be hidden.
	 * @param event
	 */
	 public void onHideJobsPanel(HideJobsPanelEvent event);

}