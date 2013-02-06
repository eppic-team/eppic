package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.GetFocusOnPdbCodeFieldHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when focus is to be set on pdb code field.
 * @author AS
 */
public class GetFocusOnPdbCodeFieldEvent extends GwtEvent<GetFocusOnPdbCodeFieldHandler> 
{
	public static Type<GetFocusOnPdbCodeFieldHandler> TYPE = new Type<GetFocusOnPdbCodeFieldHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<GetFocusOnPdbCodeFieldHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(GetFocusOnPdbCodeFieldHandler handler) 
	{
		handler.onGrabFocusOnPdbCodeField(this);
	}

}
