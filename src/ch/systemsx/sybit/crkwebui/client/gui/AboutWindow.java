package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;

public class AboutWindow extends ResizableWindow 
{
	private static int ABOUT_WINDOW_DEFAULT_WIDTH = 400;
	private static int ABOUT_WINDOW_DEFAULT_HEIGHT = 400;
	
	public AboutWindow(final MainController mainController) 
	{
		super(mainController,
			  ABOUT_WINDOW_DEFAULT_WIDTH,
			  ABOUT_WINDOW_DEFAULT_HEIGHT);
		
		this.setSize(windowWidth, windowHeight);
		
		this.setHeading(MainController.CONSTANTS.about_window_title());
		this.setPlain(true);
		this.setHideOnButtonClick(true);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setScrollMode(Scroll.AUTO);
		
		this.addText("CRK 2012. Please add here some description...");
		
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
				MainViewPort mainViewPort = mainController.getMainViewPort();
				
				if((mainViewPort != null) &&
		           (mainViewPort.getCenterPanel() != null) &&
		           (mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		           (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
		           {
				    	((ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel()).getResultsGrid().focus();
		           }
			}
		});
	}
}
