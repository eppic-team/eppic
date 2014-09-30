package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.StopJobsListAutoRefreshEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Stops autorefreshing of jobs list event handler.
 * @author AS
 */
public interface StopJobsListAutoRefreshHandler extends EventHandler 
{
	/**
	 * Method called when autorefreshing of the list of jobs is to be stopped.
	 * @param event Stop jobs list autorefreshing event
	 */
	 public void onStopJobsListAutoRefresh(StopJobsListAutoRefreshEvent event);
}
