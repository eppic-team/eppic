package ch.systemsx.sybit.crkwebui.client.handlers;

import ch.systemsx.sybit.crkwebui.client.events.ShowErrorEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show error event handler.
 * @author AS
 */
public interface ShowErrorHandler extends EventHandler 
{
	/**
	 * Method called when error window is to be displayed.
	 * @param event Show error event
	 */
	 public void onShowError(ShowErrorEvent event);
}
