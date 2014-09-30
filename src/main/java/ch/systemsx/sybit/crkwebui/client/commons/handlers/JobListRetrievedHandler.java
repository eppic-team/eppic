package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.JobListRetrievedEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * List of jobs retrieved event handler.
 * @author AS
 */
public interface JobListRetrievedHandler extends EventHandler 
{
	/**
	 * Method called when list of jobs was retrieved.
	 * @param event Job list retrieved event
	 */
	 public void onJobListRetrieved(JobListRetrievedEvent event);
}
