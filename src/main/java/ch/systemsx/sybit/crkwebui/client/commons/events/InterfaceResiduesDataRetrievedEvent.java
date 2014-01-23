package ch.systemsx.sybit.crkwebui.client.commons.events;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.InterfaceResiduesDataRetrievedHandler;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when interface residues data was retrieved.
 * @author AS
 */
public class InterfaceResiduesDataRetrievedEvent extends GwtEvent<InterfaceResiduesDataRetrievedHandler> 
{
	public static Type<InterfaceResiduesDataRetrievedHandler> TYPE = new Type<InterfaceResiduesDataRetrievedHandler>();
	
	/**
	 * Retrieved interace residues data.
	 */
	private final HashMap<Integer, List<InterfaceResidueItem>> interfaceResidues;
	
	public InterfaceResiduesDataRetrievedEvent(HashMap<Integer, List<InterfaceResidueItem>> interfaceResidues)
	{
		this.interfaceResidues = interfaceResidues;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<InterfaceResiduesDataRetrievedHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(InterfaceResiduesDataRetrievedHandler handler) 
	{
		handler.onInterfaceResiduesDataRetrieved(this);
	}

	/**
	 * Gets retrieved interface residues data.
	 * @return retrieved interface residues data
	 */
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues() {
		return interfaceResidues;
	}
}
