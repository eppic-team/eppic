/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowHomologsEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show homologs event handler.
 * @author nikhil
 *
 */
public interface ShowHomologsHandler extends EventHandler 
{
	/**
	 * Method called when homologs window is to be displayed.
	 * @param event Show homologs event
	 */
	 public void onShowHomologs(ShowHomologsEvent event);
}
