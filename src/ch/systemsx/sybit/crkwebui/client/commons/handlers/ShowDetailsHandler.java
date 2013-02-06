package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show details event handler.
 * @author AS
 */
public interface ShowDetailsHandler extends EventHandler 
{
	/**
	 * Method called when details about interface are to be displayed.
	 * @param event Show details event
	 */
	 public void onShowDetails(ShowDetailsEvent event);
}
