package ch.systemsx.sybit.crkwebui.client.commons.events;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.InterfaceResiduesDataRetrievedHandler;
import eppic.dtomodel.Residue;

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
	private final HashMap<Integer, List<Residue>> interfaceResidues;
	
	public InterfaceResiduesDataRetrievedEvent(HashMap<Integer, List<Residue>> interfaceResidues)
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
	public HashMap<Integer, List<Residue>> getInterfaceResidues() {
		return interfaceResidues;
	}
}
