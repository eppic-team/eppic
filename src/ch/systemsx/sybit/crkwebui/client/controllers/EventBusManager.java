package ch.systemsx.sybit.crkwebui.client.controllers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * Applicaiton wide event bus manager.
 * @author root
 *
 */
public class EventBusManager 
{
	public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
}
