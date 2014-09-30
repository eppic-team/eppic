package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.BeforeJobRemovedHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired before job is to be removed.
 * @author AS
 */
public class BeforeJobDeletedEvent extends GwtEvent<BeforeJobRemovedHandler> 
{
	public static Type<BeforeJobRemovedHandler> TYPE = new Type<BeforeJobRemovedHandler>();
	
	/**
	 * Identifier of the job to delete.
	 */
	private final String jobToDelete;
	
	public BeforeJobDeletedEvent(String jobToDelete)
	{
		this.jobToDelete = jobToDelete;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<BeforeJobRemovedHandler> getAssociatedType() 
	{
		return TYPE;
	}
	
	@Override
	protected void dispatch(BeforeJobRemovedHandler handler) 
	{
		handler.onBeforeJobRemoved(this);
	}
	
	/**
	 * Retrieves identifier of the job to delete.
	 * @return identifier of the job to delete
	 */
	public String getJobToDelete() {
		return jobToDelete;
	}
}
