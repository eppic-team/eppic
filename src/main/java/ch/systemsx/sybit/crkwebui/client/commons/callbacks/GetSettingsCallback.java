package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationInitEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to retrieve initial settings.
 * @author srebniak_a
 *
 */
public class GetSettingsCallback implements AsyncCallback<ApplicationSettings>
{
	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_settings_error(), caught));
	}

	@Override
	public void onSuccess(ApplicationSettings result)
	{
		if (result != null)
		{
			ApplicationContext.setSettings(result);
			ApplicationContext.setNrOfSubmissions(result.getNrOfJobsForSession());
			
			ApplicationContext.setWindowData(new WindowData(Window.getClientWidth(), Window.getClientHeight()));
			ApplicationContext.adjustWindowData(Window.getClientWidth(), Window.getClientHeight());
			
			EventBusManager.EVENT_BUS.fireEvent(new ApplicationInitEvent());
			
			if((result.getNotificationOnStart() != null) &&
			   (!result.getNotificationOnStart().equals("")))
			{
				EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(result.getNotificationOnStart(), 
																			   StatusMessageType.NO_ERROR));
			}
			
			History.fireCurrentHistoryState();
		}
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_settings_error() + " - incorrect type", 
																		   StatusMessageType.INTERNAL_ERROR));
		}
	}
}
