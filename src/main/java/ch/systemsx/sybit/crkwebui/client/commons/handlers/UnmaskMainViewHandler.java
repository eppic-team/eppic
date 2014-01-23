package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.UnmaskMainViewEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Unmask main view handler.
 * @author AS
 */
public interface UnmaskMainViewHandler extends EventHandler 
{
	/**
	 * Method called when main view is to be unmasked.
	 * @param event Unmask main view event
	 */
	 public void onUnmaskMainView(UnmaskMainViewEvent event);
}
