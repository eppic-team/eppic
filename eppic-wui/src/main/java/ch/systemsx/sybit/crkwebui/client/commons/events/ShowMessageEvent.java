package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowMessageHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when message window is to be displayed.
 * @author AS
 */
public class ShowMessageEvent extends GwtEvent<ShowMessageHandler> 
{
	public static Type<ShowMessageHandler> TYPE = new Type<ShowMessageHandler>();
	
	/**
	 * Title of the message box.
	 */
	private final String title;
	
	/**
	 * Content of the message box.
	 */
	private final String message;
	
	public ShowMessageEvent(String title,
							String message)
	{
		this.title = title;
		this.message = message;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowMessageHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowMessageHandler handler) 
	{
		handler.onShowMessage(this);
	}

	/**
	 * Retrieves title of the message box.
	 * @return title of the message box
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Retrieves content of the message box.
	 * @return content of the message box
	 */
	public String getMessage() {
		return message;
	}

}
