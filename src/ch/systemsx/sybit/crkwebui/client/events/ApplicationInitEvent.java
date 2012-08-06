package ch.systemsx.sybit.crkwebui.client.events;

import ch.systemsx.sybit.crkwebui.client.handlers.ApplicationInitHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when main view is to be initialized.
 * @author AS
 */
public class ApplicationInitEvent extends GwtEvent<ApplicationInitHandler> 
{
	public static Type<ApplicationInitHandler> TYPE = new Type<ApplicationInitHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ApplicationInitHandler> getAssociatedType() 
	{
		return TYPE;
	}
	
	@Override
	protected void dispatch(ApplicationInitHandler handler) 
	{
		handler.onApplicationInit(this);
	}
}
