package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Main application resized event handler.
 * @author AS
 */
public interface ApplicationWindowResizeHandler extends EventHandler 
{
	/**
	 * Method called when main application window was resized.
	 * @param event Application window resize event
	 */
	 public void onResizeApplicationWindow(ApplicationWindowResizeEvent event);
}
