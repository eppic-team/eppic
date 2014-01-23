/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideTopPanelSearchBoxHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when search box is to be hidden.
 * @author biyani_n
 *
 */
public class HideTopPanelSearchBoxEvent extends GwtEvent<HideTopPanelSearchBoxHandler> {

	public static Type<HideTopPanelSearchBoxHandler> TYPE = new Type<HideTopPanelSearchBoxHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<HideTopPanelSearchBoxHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(HideTopPanelSearchBoxHandler handler) {
		handler.onHideSearchBox(this);
	}

}
