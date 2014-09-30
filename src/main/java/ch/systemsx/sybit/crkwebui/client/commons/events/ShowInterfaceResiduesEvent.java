package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowInterfaceResiduesWindowHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when interface residue data is to be retrieved.
 * @author AS
 */
public class ShowInterfaceResiduesEvent extends GwtEvent<ShowInterfaceResiduesWindowHandler> 
{
	public static Type<ShowInterfaceResiduesWindowHandler> TYPE = new Type<ShowInterfaceResiduesWindowHandler>();
	
	/**
	 * Identifier of the interface to retrieve.
	 */
	private final int interfaceId;
	
	public ShowInterfaceResiduesEvent(int interfaceId)
	{
		this.interfaceId = interfaceId;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowInterfaceResiduesWindowHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowInterfaceResiduesWindowHandler handler) 
	{
		handler.onShowInterfaceResidues(this);
	}

	/**
	 * Gets identifier of the interface to retrieve.
	 * @return identifier of the interface to retrieve
	 */
	public int getInterfaceId() {
		return interfaceId;
	}

}
