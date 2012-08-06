package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.ShowAboutEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.handlers.UpdateStatusLabelHandler;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;

/**
 * Bottom panel containing status label and contact information.
 * @author srebniak_a
 *
 */
public class BottomPanel extends LayoutContainer
{
	private HTML status;
	private Label helpLink;
	private Label contactLink;
	private Label aboutLink;

	public BottomPanel()
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));

	    status = new HTML();
	    this.add(status, new RowData(1, 1, new Margins(0)));

	    LayoutContainer linksContainerWrapper = new LayoutContainer();
	    linksContainerWrapper.setBorders(false);

	    VBoxLayout vBoxLayout = new VBoxLayout();
	    vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
	    linksContainerWrapper.setLayout(vBoxLayout);

	    LayoutContainer linksContainer = new LayoutContainer();
	    linksContainer.setBorders(false);

	    aboutLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_about_link());
	    aboutLink.addStyleName("eppic-horizontal-nav");
	    
	    aboutLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowAboutEvent());
			}
		});

		contactLink = new Label("<a href=\"" +
								AppPropertiesManager.CONSTANTS.bottom_panel_contact_link() +
								"\" target=\"_blank\">" +
								AppPropertiesManager.CONSTANTS.bottom_panel_contact_link_label() +
								"</a>");
		contactLink.addStyleName("eppic-default-left-margin");
		contactLink.addStyleName("eppic-horizontal-nav");
		
		helpLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_help_link());
		helpLink.addStyleName("eppic-default-left-margin");
		helpLink.addStyleName("eppic-horizontal-nav");
		helpLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("help");
			}
		});

		linksContainer.add(aboutLink);
		linksContainer.add(helpLink);
		linksContainer.add(contactLink);

		linksContainerWrapper.add(linksContainer);
		this.add(linksContainerWrapper, new RowData(150, 1, new Margins(0)));
		
		initializeEventsListeners();
	}

	/**
	 * Updates text of the status message label.
	 * @param message text to display.
	 * @param isError flag specifying whether message is the error.
	 */
	private void updateStatusMessage(String message, boolean isError)
	{
		String messageText = "<span style=\"color:";

		String color = "black";

		if(isError)
		{
			color = "red; font-weight: bold";
		}

		messageText += color + "\">" + "Status: " + message;

		if(isError)
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
				updateStatusMessage(event.getStatusText(), event.isWasError());
			}
		});
	}
}
