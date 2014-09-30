package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Update status label handler.
 * @author AS
 */
public interface UpdateStatusLabelHandler extends EventHandler 
{
	/**
	 * Method called when status of the application is to be changed
	 * @param event Update status label event
	 */
	 public void onUpdateStatusLabel(UpdateStatusLabelEvent event);
}
