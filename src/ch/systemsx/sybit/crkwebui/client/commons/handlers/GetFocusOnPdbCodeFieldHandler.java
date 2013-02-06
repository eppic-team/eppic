package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.GetFocusOnPdbCodeFieldEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Focus on pdb code field handler.
 * @author AS
 */
public interface GetFocusOnPdbCodeFieldHandler extends EventHandler 
{
	/**
	 * Method called when focus is to be set on pdb code field.
	 * @param event Get focus on pdb code field event
	 */
	 public void onGrabFocusOnPdbCodeField(GetFocusOnPdbCodeFieldEvent event);
}
