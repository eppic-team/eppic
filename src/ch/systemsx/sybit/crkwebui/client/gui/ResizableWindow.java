package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.data.WindowData;

import com.extjs.gxt.ui.client.widget.Dialog;

/**
 * Generic resizable window.
 * @author AS
 */
public class ResizableWindow extends Dialog
{
	protected int windowWidth;
	protected int windowHeight;

	private boolean resizeWindow;

	public ResizableWindow(int defaultWidth,
						   int defaultHeight,
						   WindowData windowData)
	{
		this.windowWidth = defaultWidth;
		this.windowHeight = defaultHeight;

		if(windowWidth > windowData.getWindowWidth())
		{
			windowWidth = windowData.getWindowWidth();
		}

		if(windowHeight > windowData.getWindowHeight() - 50)
		{
			windowHeight = windowData.getWindowHeight() - 50;

			if(windowHeight <= 0)
			{
				windowHeight = 1;
			}
		}
	}

	/**
	 * Retrieves width of the window.
	 * @return width of the window
	 */
	public int getWindowWidth() {
		return windowWidth;
	}

	/**
	 * Sets width of the window.
	 * @param windowWidth width of the window
	 */
	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	/**
	 * Retrieves height of the window.
	 * @return height of the window
	 */
	public int getWindowHeight() {
		return windowHeight;
	}

	/**
	 * Sets height of the window.
	 * @param windowHeight height of the window
	 */
	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
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
