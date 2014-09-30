package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowJobsPanelHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when jobs panel is to be shown.
 * @author biyani_n
 *
 */
public class ShowJobsPanelEvent extends GwtEvent<ShowJobsPanelHandler> {

	public static Type<ShowJobsPanelHandler> TYPE = new Type<ShowJobsPanelHandler>();
	
	@Override
	public Type<ShowJobsPanelHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ShowJobsPanelHandler handler) {
		handler.onShowJobsPanel(this);		
	}


}
