package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show interface residues window event handler.
 * @author AS
 */
public interface ShowInterfaceResiduesWindowHandler extends EventHandler 
{
	/**
	 * Method called when interface residues window is to be displayed.
	 * @param event Show interface residues window event
	 */
	 public void onShowInterfaceResidues(ShowInterfaceResiduesEvent event);
}
