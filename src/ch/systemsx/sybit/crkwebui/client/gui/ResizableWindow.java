package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.Dialog;

/**
 * Generic resizable window.
 * @author AS
 */
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

		if(windowWidth > mainController.getWindowData().getWindowWidth())
		{
			windowWidth = mainController.getWindowData().getWindowWidth();
		}

		if(windowHeight > mainController.getWindowData().getWindowHeight() - 50)
		{
			windowHeight = mainController.getWindowData().getWindowHeight() - 50;

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
