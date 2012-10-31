package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.data.StatusMessageType;
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
public class NavigationPanel extends LayoutContainer
{
	private HTML status;
	private StatusMessageType lastMessageType = StatusMessageType.NO_ERROR;

	public NavigationPanel()
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

	    Label homeLink = createHomeLink();
	    Label aboutLink = createAboutLink();
	    Label helpLink = createHelpLink();
	    Label downloadsLink = createDownloadsLink();
	    Label contactLink = createContactLink();

	    linksContainer.add(homeLink);
	    linksContainer.add(downloadsLink);
	    linksContainer.add(helpLink);
		linksContainer.add(aboutLink);
		linksContainer.add(contactLink);

		linksContainerWrapper.add(linksContainer);
		this.add(linksContainerWrapper, new RowData(300, 1, new Margins(0)));
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates link to the home page.
	 * @return link to the home page
	 */
	private Label createHomeLink()
	{
		Label homeLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_home_link_label());
		homeLink.addStyleName("eppic-horizontal-nav");
		homeLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("");
			}
		});
	    
	    return homeLink;
	}
	
	/**
	 * Creates link to open about window.
	 * @return link to about window
	 */
	private Label createAboutLink()
	{
		Label aboutLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_about_link_label());
	    aboutLink.addStyleName("eppic-horizontal-nav");
	    aboutLink.addStyleName("eppic-default-left-margin");
	    aboutLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowAboutEvent());
			}
		});
	    
	    return aboutLink;
	}
	
	/**
	 * Creates link to help page.
	 * @return link to help page
	 */
	private Label createHelpLink()
	{
		Label helpLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_help_link_label());
		helpLink.addStyleName("eppic-default-left-margin");
		helpLink.addStyleName("eppic-horizontal-nav");
		helpLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("help");
			}
		});
		
		return helpLink;
	}
	
	/**
	 * Creates link to the view containing downloads.
	 * @return link to downloads view
	 */
	private Label createDownloadsLink()
	{
		Label downloadsLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_downloads_link_label());
		downloadsLink.addStyleName("eppic-horizontal-nav");
		downloadsLink.addStyleName("eppic-default-left-margin");
		downloadsLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("downloads");
			}
		});
	    
	    return downloadsLink;
	}
	
	/**
	 * Creates contact link.
	 * @return contact link
	 */
	private Label createContactLink()
	{
		Label contactLink = new Label("<a href=\"" +
				AppPropertiesManager.CONSTANTS.bottom_panel_contact_link() +
				"\" target=\"_blank\">" +
				AppPropertiesManager.CONSTANTS.bottom_panel_contact_link_label() +
				"</a>");
		contactLink.addStyleName("eppic-default-left-margin");
		contactLink.addStyleName("eppic-horizontal-nav");

		return contactLink;
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

		messageText += color + "\">" + "Status: " + message;

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
