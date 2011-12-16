package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.Dialog;

public class ResizableWindow extends Dialog 
{
	protected MainController mainController;
	
	protected int windowWidth;
	protected int windowHeight;
	
	private boolean resizeWindow;

	public ResizableWindow(final MainController mainController,
						   int defaultWidth,
						   int defaultHeight) 
	{
		this.mainController = mainController;
		this.windowWidth = defaultWidth;
		this.windowHeight = defaultHeight;
		
		if(windowWidth > mainController.getWindowWidth())
		{
			windowWidth = mainController.getWindowWidth();
		}
		
		if(windowHeight > mainController.getWindowHeight() - 50)
		{
			windowHeight = mainController.getWindowHeight() - 50;
			
			if(windowHeight <= 0)
			{
				windowHeight = 1;
			}
		}
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public void setResizeWindow(boolean resizeWindow) 
	{
		this.resizeWindow = resizeWindow;
	}

	public boolean isResizeWindow() {
		return resizeWindow;
	}
}
