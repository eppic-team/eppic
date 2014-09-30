package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.GetFocusOnJobsListEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Focus on jobs list event handler.
 * @author AS
 */
public interface GetFocusOnJobsListHandler extends EventHandler 
{
	/**
	 * Method called when focus is to be set on jobs list.
	 * @param event Get focus on jobs list event
	 */
	 public void onGrabFocusOnJobsList(GetFocusOnJobsListEvent event);
}
