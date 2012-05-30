package ch.systemsx.sybit.crkwebui.client.handlers;

import ch.systemsx.sybit.crkwebui.client.events.WindowHideEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Window hide event handler.
 * @author AS
 */
public interface WindowHideHandler extends EventHandler 
{
	/**
	 * This method is called when internal application window is closed.
	 * @param event window hide event
	 */
	 public void onWindowHide(WindowHideEvent event);
}
