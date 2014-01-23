package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.SubmitJobEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Submit job event handler.
 * @author AS
 */
public interface SubmitJobHandler extends EventHandler 
{
	/**
	 * Method called when job is to submitted.
	 * @param event Submit job event
	 */
	 public void onSubmitJob(SubmitJobEvent event);
}
