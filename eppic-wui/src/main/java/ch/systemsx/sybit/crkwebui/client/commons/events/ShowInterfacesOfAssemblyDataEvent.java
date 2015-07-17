package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowInterfacesOfAssemblyDataHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowResultsDataHandler;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;

/**
 * Event fired when results data panel is to be displayed.
 * @author AS
 */
public class ShowInterfacesOfAssemblyDataEvent extends GwtEvent<ShowInterfacesOfAssemblyDataHandler> 
{
	public static Type<ShowInterfacesOfAssemblyDataHandler> TYPE = new Type<ShowInterfacesOfAssemblyDataHandler>();
	
	/**
	 * Pdb score result item to display.
	 */
	private PdbInfo pdbScoreItem;
	
	public ShowInterfacesOfAssemblyDataEvent(PdbInfo pdbScoreItem)
	{
		this.pdbScoreItem = pdbScoreItem;
	}
	
	/*public ShowInterfacesOfAssemblyDataEvent()
	{

	}*/
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowInterfacesOfAssemblyDataHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowInterfacesOfAssemblyDataHandler handler) 
	{
		handler.onShowInterfacesOfAssembly(this);
	}
	
	/**
	 * Retrieves pdb score results item to display.
	 * @return retrieved pdb score item
	 */
	public PdbInfo getPdbScoreItem() {
		return pdbScoreItem;
	}
}
