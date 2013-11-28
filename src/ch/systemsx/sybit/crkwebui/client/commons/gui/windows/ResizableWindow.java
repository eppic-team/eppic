package ch.systemsx.sybit.crkwebui.client.commons.gui.windows;

import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.sencha.gxt.widget.core.client.Dialog;

/**
 * Generic resizable window.
 * @author AS
 */
public class ResizableWindow extends Dialog
{
	private boolean resizeWindow;

	public ResizableWindow(int defaultWidth,
						   int defaultHeight,
						   WindowData windowData)
	{
		int windowWidth = defaultWidth;
		int windowHeight = defaultHeight;

		if(windowWidth > windowData.getWindowWidth() - 20)
		{
			windowWidth = windowData.getWindowWidth() - 20;
			
			if(windowWidth <= 0)
			{
				windowWidth = 1;
			}
		}

		if(windowHeight > windowData.getWindowHeight() - 50)
		{
			windowHeight = windowData.getWindowHeight() - 50;

			if(windowHeight <= 0)
			{
				windowHeight = 1;
			}
		}
		
		this.setPixelSize(windowWidth, windowHeight);
	}
	
	/**
	 * Sets flag pointing whether window should be resized.
	 * @param resizeWindow flag pointing whether window should be resized
	 */
	public void setResizeWindow(boolean resizeWindow)
	{
		this.resizeWindow = resizeWindow;
	}

	/**
	 * Retrieves information whether window should be resized.
	 * @return flag pointing whether window should be resized
	 */
	public boolean isResizeWindow() {
		return resizeWindow;
	}
}
