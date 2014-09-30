package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Window hide event handler.
 * @author AS
 */
public interface WindowHideHandler extends EventHandler 
{
	/**
	 * This method is called when internal application window is closed.
	 * @param event Window hide event
	 */
	 public void onWindowHide(WindowHideEvent event);
}
