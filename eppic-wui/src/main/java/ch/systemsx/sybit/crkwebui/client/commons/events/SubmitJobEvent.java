package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.SubmitJobHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when job is to be submitted.
 * @author AS
 */
public class SubmitJobEvent extends GwtEvent<SubmitJobHandler> 
{
	public static Type<SubmitJobHandler> TYPE = new Type<SubmitJobHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SubmitJobHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(SubmitJobHandler handler) 
	{
		handler.onSubmitJob(this);
	}
}
