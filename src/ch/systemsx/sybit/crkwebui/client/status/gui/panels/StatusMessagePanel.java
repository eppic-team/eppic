package ch.systemsx.sybit.crkwebui.client.status.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.UpdateStatusLabelHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTML;

/**
 * Bottom panel containing status label and contact information.
 * @author srebniak_a
 *
 */
public class StatusMessagePanel extends LayoutContainer
{
	private HTML status;
	private StatusMessageType lastMessageType = StatusMessageType.NO_ERROR;

	public StatusMessagePanel()
	{
		this.setScrollMode(Scroll.AUTOY);

		status = new HTML();
		this.add(status);
		
		initializeEventsListeners();
	}
	
	/**
	 * Updates text of the status message label.
	 * @param message text to display.
	 * @param messageType type of the message(no error, internal error, system error)
	 */
	private void updateStatusMessage(String message, StatusMessageType messageType)
	{
		String messageText = "<span style=\"color:";

		String color = "black";

		if(messageType != StatusMessageType.NO_ERROR)
		{
			color = "red; font-weight: bold";
		}

		messageText += color + "\">" + "Status: " + EscapedStringGenerator.generateEscapedString(message);

		if(messageType != StatusMessageType.NO_ERROR)
		{
			messageText += " - " + AppPropertiesManager.CONSTANTS.bottom_panel_status_error_refresh_page();
		}

		messageText += "</span>";

		status.setHTML(messageText);
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(UpdateStatusLabelEvent.TYPE, new UpdateStatusLabelHandler() 
		{
			@Override
			public void onUpdateStatusLabel(UpdateStatusLabelEvent event)
			{
				if(lastMessageType != StatusMessageType.INTERNAL_ERROR)
				{
					updateStatusMessage(event.getStatusText(), event.getMessageType());
					lastMessageType = event.getMessageType();
				}
			}
		});
	}
}
