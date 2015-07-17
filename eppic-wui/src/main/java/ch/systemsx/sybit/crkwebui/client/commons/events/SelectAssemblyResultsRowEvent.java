package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectAssemblyResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectResultsRowHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when results row is to be selected.
 * @author AS
 */
public class SelectAssemblyResultsRowEvent extends GwtEvent<SelectAssemblyResultsRowHandler> 
{
	/**
	 * Row index to select.
	 */
	private int rowIndex;
	
	public static Type<SelectAssemblyResultsRowHandler> TYPE = new Type<SelectAssemblyResultsRowHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SelectAssemblyResultsRowHandler> getAssociatedType() 
	{
		return TYPE;
	}
	
	public SelectAssemblyResultsRowEvent(int rowIndex)
	{
		this.rowIndex = rowIndex;
	}

	@Override
	protected void dispatch(SelectAssemblyResultsRowHandler handler) 
	{
		handler.onSelectAssemblyResultsRow(this);
	}
	
	/**
	 * Retrieves identifier of the row to select.
	 * @return identifier of the row to select
	 */
	public int getRowIndex()
	{
		return rowIndex;
	}

}
