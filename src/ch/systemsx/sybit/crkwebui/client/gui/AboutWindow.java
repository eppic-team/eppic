package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.events.WindowHideEvent;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;

/**
 * Window containing general information about the application.
 * @author AS
 */
public class AboutWindow extends ResizableWindow 
{
	private static int ABOUT_WINDOW_DEFAULT_WIDTH = 400;
	private static int ABOUT_WINDOW_DEFAULT_HEIGHT = 300;
	
	public AboutWindow(MainController mainController) 
	{
		super(mainController,
			  ABOUT_WINDOW_DEFAULT_WIDTH,
			  ABOUT_WINDOW_DEFAULT_HEIGHT);
		
		this.setSize(windowWidth, windowHeight);
		
		this.setHeading(AppPropertiesManager.CONSTANTS.about_window_title());
		this.setPlain(true);
		this.setHideOnButtonClick(true);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setScrollMode(Scroll.AUTO);
		
		this.addText(
				"<table cellspacing=\"10\">" +
				"<tr>" +
				"<td>" +
				"<b>EPPIC</b>" +
				"</td>" +
				"<td>" +
				"An Evolutionary Protein-Protein Interface Classifier." +
				"</td>" +
				"</tr>" +
				"<tr>" +
				"<td></td>" +
				"<td>" +
				"Laboratory of Biomolecular Research<br>Paul Scherrer Institut<br>Villigen PSI<br>Switzerland" +
				"</td>" +
				"</tr>" +
				"</table>");
		
		Listener<WindowEvent> resizeWindowListener = new Listener<WindowEvent>() {

			@Override
			public void handleEvent(WindowEvent be) 
			{
				windowHeight = be.getHeight();
				windowWidth = be.getWidth();
			}
		};
		
		this.addListener(Events.Resize, resizeWindowListener);
		
		this.addWindowListener(new WindowListener()
		{
			@Override
			public void windowHide(WindowEvent we)
			{
				EventBusManager.EVENT_BUS.fireEvent(new WindowHideEvent());
			}
		});
	}
}
