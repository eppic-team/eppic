package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAboutEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show about event handler.
 * @author AS
 */
public interface ShowAboutHandler extends EventHandler 
{
	/**
	 * Method called when about window is to be displayed.
	 * @param event Show about event
	 */
	 public void onShowAbout(ShowAboutEvent event);
}
