package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.RefreshStatusDataEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Refresh status data panel event handler.
 *
 */
public interface RefreshStatusDataHandler extends EventHandler 
{
	/**
	 * Method called when status data panel is to be refreshed.
	 * @param event Refresh status data event
	 */
	 public void onRefreshStatusData(RefreshStatusDataEvent event);
}
