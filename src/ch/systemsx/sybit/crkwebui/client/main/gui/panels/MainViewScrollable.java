package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.google.gwt.user.client.Window;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;

/**
 * Scrollable wrapper of main view.
 * @author adam
 */
public class MainViewScrollable extends FlowLayoutContainer
{
	public MainViewScrollable(MainViewPort mainViewPort)
	{
		this.addStyleName("eppic-panel-with-gray-background");
		this.setScrollMode(selectScrollType(Window.getClientWidth(), Window.getClientHeight()));
		this.setPixelSize(Window.getClientWidth(),
					 Window.getClientHeight());
		this.add(mainViewPort);
		
		initializeEventsListeners();
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(ApplicationWindowResizeEvent.TYPE, new ApplicationWindowResizeHandler() {
			
			@Override
			public void onResizeApplicationWindow(ApplicationWindowResizeEvent event) {
				setPixelSize(Window.getClientWidth(),
				    	Window.getClientHeight());
				
				setScrollMode(selectScrollType(Window.getClientWidth(), Window.getClientHeight()));
			}
		});
	}
	
	/**
	 * Select correct type of the scroll based on the size of client window and minimum values of 
	 * width and height.
	 *  
	 * @param width width of client window
	 * @param height height of client window
	 * @return scroll type
	 */
	private ScrollMode selectScrollType(int width,
									int height)
	{
		ScrollMode scrollType = ScrollMode.NONE;
		
		if((width < ApplicationContext.getAdjustedWindowData().getWindowWidth()) &&
		   (height < ApplicationContext.getAdjustedWindowData().getWindowHeight()))
		{
			scrollType = ScrollMode.AUTO;
		}
		else if(width < ApplicationContext.getAdjustedWindowData().getWindowWidth())
		{
			scrollType = ScrollMode.AUTOX;
		}
		else if(height < ApplicationContext.getAdjustedWindowData().getWindowHeight())
		{
			scrollType = ScrollMode.AUTOY;
		}
		
		return scrollType;
	}
}
