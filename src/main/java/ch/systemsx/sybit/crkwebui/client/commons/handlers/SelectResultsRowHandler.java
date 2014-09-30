package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Select results row event handler.
 * @author AS
 */
public interface SelectResultsRowHandler extends EventHandler 
{
	/**
	 * Method called when results row is to be selected
	 * @param event Select results row event
	 */
	 public void onSelectResultsRow(SelectResultsRowEvent event);
}
