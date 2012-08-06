package ch.systemsx.sybit.crkwebui.client.listeners;

import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.SubmitJobEvent;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.google.gwt.event.dom.client.KeyCodes;

/**
 * Submit job listener.
 */
public class SubmitKeyListener extends KeyListener
{
	/**
	 * Fires submit job event when enter pressed.
	 */
	@Override
	public void componentKeyPress(ComponentEvent event)
	{
		if(event.getKeyCode() == KeyCodes.KEY_ENTER)
		{
			EventBusManager.EVENT_BUS.fireEvent(new SubmitJobEvent());
		}
	}
}
