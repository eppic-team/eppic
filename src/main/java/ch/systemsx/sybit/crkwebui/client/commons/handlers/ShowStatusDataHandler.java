package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowStatusDataEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show status data panel event handler.
 *
 */
public interface ShowStatusDataHandler extends EventHandler 
{
	/**
	 * Method called when status data panel is to be displayed.
	 * @param event Show status panel data event
	 */
	 public void onShowStatusData(ShowStatusDataEvent event);
}
