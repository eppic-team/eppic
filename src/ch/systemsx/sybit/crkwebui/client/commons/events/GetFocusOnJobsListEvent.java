package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.GetFocusOnJobsListHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when focus is to be set on jobs list.
 * @author AS
 */
public class GetFocusOnJobsListEvent extends GwtEvent<GetFocusOnJobsListHandler> 
{
	public static Type<GetFocusOnJobsListHandler> TYPE = new Type<GetFocusOnJobsListHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<GetFocusOnJobsListHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(GetFocusOnJobsListHandler handler) 
	{
		handler.onGrabFocusOnJobsList(this);
	}

}
