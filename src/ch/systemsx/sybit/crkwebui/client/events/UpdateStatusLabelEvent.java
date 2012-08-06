package ch.systemsx.sybit.crkwebui.client.events;

import ch.systemsx.sybit.crkwebui.client.handlers.UpdateStatusLabelHandler;

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
	 * Flag pointing whether message is error.
	 */
	private final boolean wasError;
	
	public UpdateStatusLabelEvent(String statusText,
								  boolean wasError)
	{
		this.statusText = statusText;
		this.wasError = wasError;
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
	 * Retrieves information whether message is the error.
	 * @return information whether message is the error
	 */
	public boolean isWasError() {
		return wasError;
	}

}
