package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.SelectAssemblyResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Select results row event handler.
 * @author AS
 */
public interface SelectAssemblyResultsRowHandler extends EventHandler 
{
	/**
	 * Method called when results row is to be selected
	 * @param event Select results row event
	 */
	 public void onSelectAssemblyResultsRow(SelectAssemblyResultsRowEvent event);
}
