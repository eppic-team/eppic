package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowThumbnailEvent;

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
