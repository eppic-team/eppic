package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowWaitingHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when waiting window is to be displayed.
 * @author AS
 */
public class ShowWaitingEvent extends GwtEvent<ShowWaitingHandler> 
{
	public static Type<ShowWaitingHandler> TYPE = new Type<ShowWaitingHandler>();
	
	/**
	 * Message to display.
	 */
	private final String message;
	
	public ShowWaitingEvent(String message)
	{
		this.message = message;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowWaitingHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowWaitingHandler handler) 
	{
		handler.onShowWaiting(this);
	}

	/**
	 * Retrieves message to display.
	 * @return message to display
	 */
	public String getMessage() {
		return message;
	}

}
