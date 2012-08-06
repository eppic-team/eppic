package ch.systemsx.sybit.crkwebui.client.controllers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * Application wide event bus manager.
 *
 */
public class EventBusManager 
{
	public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
}
