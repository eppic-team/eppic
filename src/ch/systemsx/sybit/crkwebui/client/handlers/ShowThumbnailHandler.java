package ch.systemsx.sybit.crkwebui.client.handlers;

import ch.systemsx.sybit.crkwebui.client.events.ShowThumbnailEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show thumbnail event handler.
 * @author AS
 */
public interface ShowThumbnailHandler extends EventHandler 
{
	/**
	 * Method called when thumbnail is to be shown or hidden.
	 * @param event Show thumbnail event
	 */
	 public void onShowThumbnail(ShowThumbnailEvent event);
}
