package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowResultsDataHandler;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when results data panel is to be displayed.
 * @author AS
 */
public class ShowResultsDataEvent extends GwtEvent<ShowResultsDataHandler> 
{
	public static Type<ShowResultsDataHandler> TYPE = new Type<ShowResultsDataHandler>();
	
	/**
	 * Pdb score result item to display.
	 */
	private final PDBScoreItem pdbScoreItem;
	
	public ShowResultsDataEvent(PDBScoreItem pdbScoreItem)
	{
		this.pdbScoreItem = pdbScoreItem;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowResultsDataHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowResultsDataHandler handler) 
	{
		handler.onShowResultsData(this);
	}
	
	/**
	 * Retrieves pdb score results item to display.
	 * @return retrieved pdb score item
	 */
	public PDBScoreItem getPdbScoreItem() {
		return pdbScoreItem;
	}
}
