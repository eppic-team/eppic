package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectResultsRowHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when results row is to be selected.
 * @author AS
 */
public class SelectResultsRowEvent extends GwtEvent<SelectResultsRowHandler> 
{
	/**
	 * Row index to select.
	 */
	private int rowIndex;
	
	public static Type<SelectResultsRowHandler> TYPE = new Type<SelectResultsRowHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SelectResultsRowHandler> getAssociatedType() 
	{
		return TYPE;
	}
	
	public SelectResultsRowEvent(int rowIndex)
	{
		this.rowIndex = rowIndex;
	}

	@Override
	protected void dispatch(SelectResultsRowHandler handler) 
	{
		handler.onSelectResultsRow(this);
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
