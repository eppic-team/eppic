package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.UncheckClustersRadioEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Uncheck cluster similar interfaces radio event handler.
 * @author AS
 */
public interface UncheckClustersRadioHandler extends EventHandler 
{
	/**
	 * Method called when radio is to be unchecked
	 * @param event 
	 */
	 public void onUncheckClustersRadio(UncheckClustersRadioEvent event);
}