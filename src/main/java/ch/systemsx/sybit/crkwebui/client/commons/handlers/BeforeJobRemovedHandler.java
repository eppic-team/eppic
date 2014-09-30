package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.BeforeJobDeletedEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Before job removed event handler.
 * @author AS
 */
public interface BeforeJobRemovedHandler extends EventHandler 
{
	/**
	 * Method called before job is removed.
	 * @param event Before job deleted event
	 */
	 public void onBeforeJobRemoved(BeforeJobDeletedEvent event);
}
