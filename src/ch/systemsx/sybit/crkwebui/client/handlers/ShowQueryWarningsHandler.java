package ch.systemsx.sybit.crkwebui.client.handlers;

import ch.systemsx.sybit.crkwebui.client.events.ShowQueryWarningsEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show query warnings event handler.
 * @author AS
 */
public interface ShowQueryWarningsHandler extends EventHandler 
{
	/**
	 * Method called when query warnings tooltip is to be displayed.
	 * @param event Show query warnings event
	 */
	 public void onShowQueryWarnings(ShowQueryWarningsEvent event);
}
