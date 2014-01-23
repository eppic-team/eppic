package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowErrorHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when error window is to be displayed.
 * @author AS
 */
public class ShowErrorEvent extends GwtEvent<ShowErrorHandler> 
{
	public static Type<ShowErrorHandler> TYPE = new Type<ShowErrorHandler>();
	
	/**
	 * Error message text.
	 */
	private final String errorText;
	
	public ShowErrorEvent(String errorText)
	{
		this.errorText = errorText;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowErrorHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowErrorHandler handler) 
	{
		handler.onShowError(this);
	}

	/**
	 * Retrieves error message text.
	 * @return error message text
	 */
	public String getErrorText() {
		return errorText;
	}

}
