package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowTopPanelSearchBoxEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show top panel search box handler.
 * @author nikhil
 */
public interface ShowTopPanelSearchBoxHandler extends EventHandler {
	
	/**
	 * Method called when top box search box is to be shown.
	 * @param event
	 */
	 public void onShowSearchBox(ShowTopPanelSearchBoxEvent event);

}
