package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAlignmentsEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Show alignments event handler.
 * @author AS
 */
public interface ShowAlignmentsHandler extends EventHandler 
{
	/**
	 * Method called when alignments window is to be displayed.
	 * @param event Show alignments event
	 */
	 public void onShowAlignments(ShowAlignmentsEvent event);
}
