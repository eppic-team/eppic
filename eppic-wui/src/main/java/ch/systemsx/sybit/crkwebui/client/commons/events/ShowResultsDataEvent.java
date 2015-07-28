package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowResultsDataHandler;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

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
	private final PdbInfo pdbScoreItem;
	//private final int viewType;
	
	public ShowResultsDataEvent(PdbInfo pdbScoreItem)
	{
		this.pdbScoreItem = pdbScoreItem;
		//this.viewType = viewType;
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
	public PdbInfo getPdbScoreItem() {
		return pdbScoreItem;
	}
	
	/*public int getViewType(){
		return viewType;
	}*/
}
