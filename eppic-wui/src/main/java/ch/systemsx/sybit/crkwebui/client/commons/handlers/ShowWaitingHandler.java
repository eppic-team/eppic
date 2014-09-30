package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowWaitingEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show waiting event handler.
 * @author AS
 */
public interface ShowWaitingHandler extends EventHandler 
{
	/**
	 * Method called when waiting window is to be displayed.
	 * @param event Show waiting event
	 */
	 public void onShowWaiting(ShowWaitingEvent event);
}
