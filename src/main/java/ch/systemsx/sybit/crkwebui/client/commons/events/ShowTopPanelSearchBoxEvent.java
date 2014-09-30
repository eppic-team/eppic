/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowTopPanelSearchBoxHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when search box is to be shown.
 * @author biyani_n
 *
 */
public class ShowTopPanelSearchBoxEvent extends GwtEvent<ShowTopPanelSearchBoxHandler> {

	public static Type<ShowTopPanelSearchBoxHandler> TYPE = new Type<ShowTopPanelSearchBoxHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowTopPanelSearchBoxHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ShowTopPanelSearchBoxHandler handler) {
		handler.onShowSearchBox(this);		
	}

}
