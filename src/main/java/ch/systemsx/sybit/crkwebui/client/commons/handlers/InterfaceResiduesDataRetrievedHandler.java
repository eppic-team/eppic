package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.InterfaceResiduesDataRetrievedEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Interface residues data retrieved event handler.
 * @author AS
 */
public interface InterfaceResiduesDataRetrievedHandler extends EventHandler 
{
	/**
	 * Method called when interface residues data was retrieved.
	 * @param event Interface residues data retrieved event
	 */
	 public void onInterfaceResiduesDataRetrieved(InterfaceResiduesDataRetrievedEvent event);
}
