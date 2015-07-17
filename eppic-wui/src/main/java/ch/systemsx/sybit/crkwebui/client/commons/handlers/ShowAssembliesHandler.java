package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAssembliesEvent;

import com.google.gwt.event.shared.EventHandler;


public interface ShowAssembliesHandler extends EventHandler 
{
	/**
	 * Method called when radio is to be unchecked
	 * @param event 
	 */
	 public void onShowAssemblies(ShowAssembliesEvent event);
}