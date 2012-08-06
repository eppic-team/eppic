package ch.systemsx.sybit.crkwebui.client.handlers;

import ch.systemsx.sybit.crkwebui.client.events.ApplicationInitEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Application init event handler.
 * @author AS
 */
public interface ApplicationInitHandler extends EventHandler 
{
	/**
	 * Method called when application initialization started.
	 * @param event Application init event
	 */
	 public void onApplicationInit(ApplicationInitEvent event);
}
