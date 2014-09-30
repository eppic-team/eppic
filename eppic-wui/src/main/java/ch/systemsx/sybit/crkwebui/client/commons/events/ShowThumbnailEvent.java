package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowThumbnailHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when thumbnail is to be shown or hidden.
 * @author AS
 */
public class ShowThumbnailEvent extends GwtEvent<ShowThumbnailHandler> 
{
	/**
	 * Flag pointing whether thumbnail should be shown or hidden.
	 */
	private final boolean hideThumbnail;
	
	public static Type<ShowThumbnailHandler> TYPE = new Type<ShowThumbnailHandler>();
	
	public ShowThumbnailEvent(boolean hideThumbnail)
	{
		this.hideThumbnail = hideThumbnail;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowThumbnailHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowThumbnailHandler handler) 
	{
		handler.onShowThumbnail(this);
	}

	/**
	 * Retrieves information whether thumbnail should be shown or hidden.
	 * @return show/hide thumbnail
	 */
	public boolean isHideThumbnail() {
		return hideThumbnail;
	}
}
