package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.UpdateStatusLabelHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when status of the application is to be updated.
 * @author AS
 */
public class UpdateStatusLabelEvent extends GwtEvent<UpdateStatusLabelHandler> 
{
	public static Type<UpdateStatusLabelHandler> TYPE = new Type<UpdateStatusLabelHandler>();
	
	/**
	 * Text of the message to update.
	 */
	private final String statusText;
	
	
	/**
	 * Type of the message (internal error, system error, no error).
	 */
	private final StatusMessageType messageType;
	
	public UpdateStatusLabelEvent(String statusText,
			  					  StatusMessageType messageType)
	{
		this.statusText = statusText;
		this.messageType = messageType;
	}
	
	public UpdateStatusLabelEvent(String statusText,
								  Throwable throwable)
	{
		this.statusText = statusText;
		this.messageType = StatusMessageType.getTypeForThrowable(throwable);
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<UpdateStatusLabelHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(UpdateStatusLabelHandler handler) 
	{
		handler.onUpdateStatusLabel(this);
	}
	
	/**
	 * Retrieves text of the message to update.
	 * @return text of the message to update
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * Retrieves type of the message.
	 * @return type of the message
	 */
	public StatusMessageType getMessageType() {
		return messageType;
	}

}
