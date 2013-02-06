package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowNoResultsDataEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show no results data event handler.
 *
 */
public interface ShowNoResultsDataHandler extends EventHandler 
{
	/**
	 * Method called when no data about the job was retrieved.
	 * @param event Show no results data event
	 */
	 public void onShowNoResultsData(ShowNoResultsDataEvent event);
}
