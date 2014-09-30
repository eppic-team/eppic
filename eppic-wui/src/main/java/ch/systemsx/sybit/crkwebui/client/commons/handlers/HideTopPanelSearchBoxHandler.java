package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.HideTopPanelSearchBoxEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Hide top panel search box handler.
 * @author nikhil
 */
public interface HideTopPanelSearchBoxHandler extends EventHandler {
	
	/**
	 * Method called when top box search box is to be hidden.
	 * @param event
	 */
	 public void onHideSearchBox(HideTopPanelSearchBoxEvent event);

}
