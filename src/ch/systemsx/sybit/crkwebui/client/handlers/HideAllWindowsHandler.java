package ch.systemsx.sybit.crkwebui.client.handlers;

import ch.systemsx.sybit.crkwebui.client.events.HideAllWindowsEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Window hide event handler.
 * @author AS
 */
public interface HideAllWindowsHandler extends EventHandler 
{
	/**
	 * This method is called when all internal windows are to be closed.
	 * @param event Hide all windows event
	 */
	 public void onHideAllWindows(HideAllWindowsEvent event);
}
