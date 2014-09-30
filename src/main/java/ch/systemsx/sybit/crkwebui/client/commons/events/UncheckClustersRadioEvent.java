package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.UncheckClustersRadioHandler;

import com.google.gwt.event.shared.GwtEvent;

public class UncheckClustersRadioEvent extends GwtEvent<UncheckClustersRadioHandler>{

	public static Type<UncheckClustersRadioHandler> TYPE = new Type<UncheckClustersRadioHandler>(); 
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<UncheckClustersRadioHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(UncheckClustersRadioHandler handler) {
		handler.onUncheckClustersRadio(this);		
	}

}
