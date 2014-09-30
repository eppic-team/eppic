package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowMessageEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show message event handler.
 * @author AS
 */
public interface ShowMessageHandler extends EventHandler 
{
	/**
	 * Method called when message window is to be displayed.
	 * @param event Show message event
	 */
	 public void onShowMessage(ShowMessageEvent event);
}
