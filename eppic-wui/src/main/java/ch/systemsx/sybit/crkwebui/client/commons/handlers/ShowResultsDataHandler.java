package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowResultsDataEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show results data panel event handler.
 * @author AS
 */
public interface ShowResultsDataHandler extends EventHandler 
{
	/**
	 * Method called when results data panel is to be displayed.
	 * @param event Show results data event
	 */
	 public void onShowResultsData(ShowResultsDataEvent event);
}
