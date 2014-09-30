package ch.systemsx.sybit.crkwebui.client.input.listeners;

import ch.systemsx.sybit.crkwebui.client.commons.events.SubmitJobEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;

/**
 * Submit job listener.
 */
public class SubmitKeyListener implements KeyDownHandler
{
	/**
	 * Fires submit job event when enter pressed.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onKeyDown(KeyDownEvent event) {
		if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
		{
		    if(event.getSource() instanceof ValueBaseField) {
			@SuppressWarnings("rawtypes")
			ValueBaseField valueBaseField = (ValueBaseField)event.getSource();
			valueBaseField.setValue(valueBaseField.getCurrentValue());
		    }
		    EventBusManager.EVENT_BUS.fireEvent(new SubmitJobEvent());
		}
	}

}
