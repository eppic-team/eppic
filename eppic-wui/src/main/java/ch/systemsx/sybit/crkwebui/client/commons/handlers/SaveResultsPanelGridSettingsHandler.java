package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.SaveResultsPanelGridSettingsEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Save results grid setting event handler.
 * @author AS
 */
public interface SaveResultsPanelGridSettingsHandler extends EventHandler 
{
	/**
	 * Method called when results panel grid settings are to be saved
	 * @param event save results grid settings event
	 */
	 public void onSaveResultsPanelGridSettings(SaveResultsPanelGridSettingsEvent event);
}
