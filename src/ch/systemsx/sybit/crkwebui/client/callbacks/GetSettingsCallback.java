package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.ApplicationInitEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;
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
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_settings_error(), true));
	}

	@Override
	public void onSuccess(ApplicationSettings result)
	{
		if (result != null)
		{
			ApplicationContext.setSettings(result);
			ApplicationContext.setNrOfSubmissions(result.getNrOfJobsForSession());
			
			ApplicationContext.setWindowData(new WindowData(Window.getClientWidth(), Window.getClientHeight()));
			ApplicationContext.adjustWindowWidth(Window.getClientWidth());
			ApplicationContext.adjustWindowHeight(Window.getClientHeight());
			
			EventBusManager.EVENT_BUS.fireEvent(new ApplicationInitEvent());
			
			if((result.getNotificationOnStart() != null) &&
			   (!result.getNotificationOnStart().equals("")))
			{
				EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(result.getNotificationOnStart(), false));
			}
			
			History.fireCurrentHistoryState();
		}
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_settings_error() + " - incorrect type", true));
		}
	}
}
