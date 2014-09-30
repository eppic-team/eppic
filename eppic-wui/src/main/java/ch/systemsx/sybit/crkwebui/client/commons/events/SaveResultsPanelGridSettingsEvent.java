package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.SaveResultsPanelGridSettingsHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when settings of results panel grid are to be saved.
 * @author AS
 */
public class SaveResultsPanelGridSettingsEvent extends GwtEvent<SaveResultsPanelGridSettingsHandler> 
{
	public static Type<SaveResultsPanelGridSettingsHandler> TYPE = new Type<SaveResultsPanelGridSettingsHandler>();
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SaveResultsPanelGridSettingsHandler> getAssociatedType() 
	{
		return TYPE;
	}
	
	@Override
	protected void dispatch(SaveResultsPanelGridSettingsHandler handler) 
	{
		handler.onSaveResultsPanelGridSettings(this);
	}
}
