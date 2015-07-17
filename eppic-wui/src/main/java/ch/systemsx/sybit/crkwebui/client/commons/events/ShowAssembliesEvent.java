package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssembliesHandler;

import com.google.gwt.event.shared.GwtEvent;

public class ShowAssembliesEvent extends GwtEvent<ShowAssembliesHandler>{

	public static Type<ShowAssembliesHandler> TYPE = new Type<ShowAssembliesHandler>(); 
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowAssembliesHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ShowAssembliesHandler handler) {
		handler.onShowAssemblies(this);		
	}

}
