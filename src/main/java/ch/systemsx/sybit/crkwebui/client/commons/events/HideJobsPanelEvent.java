/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideJobsPanelHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when jobs panel is to be hidden
 * @author biyani_n
 *
 */
public class HideJobsPanelEvent extends GwtEvent<HideJobsPanelHandler> {

	public static Type<HideJobsPanelHandler> TYPE = new Type<HideJobsPanelHandler>();
	
	@Override
	public Type<HideJobsPanelHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(HideJobsPanelHandler handler) {
		handler.onHideJobsPanel(this);		
	}


}