package ch.systemsx.sybit.crkwebui.client.handlers;

import ch.systemsx.sybit.crkwebui.client.events.ShowNoResultsDataEvent;

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
