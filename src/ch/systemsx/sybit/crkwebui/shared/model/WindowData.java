package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

/**
 * Window application data.
 * @author AS
 *
 */
public class WindowData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Width of the application window.
	 */
	private int windowWidth;
	
	/**
	 * Height of the application window.
	 */
	private int windowHeight;
	
	public WindowData()
	{
		
	}
	
	/**
	 * Creates instance of window data with specified width and height.
	 * @param windowWidth width of the application window
	 * @param windowHeight height of the application window
	 */
	public WindowData(int windowWidth,
					  int windowHeight)
	{
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
	}

	/**
	 * Retrieves width of the application window.
	 * @return width of the application window
	 */
	public int getWindowWidth() {
		return windowWidth;
	}

	/**
	 * Sets width of the application window.
	 * @param windowWidth width of the application window
	 */
	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	/**
	 * Retrieves height of the application window.
	 * @return height of the application window
	 */
	public int getWindowHeight() {
		return windowHeight;
	}

	/**
	 * Sets height of the application window.
	 * @param windowHeight height of the application window
	 */
	public void setWindowHeight(int windowHeight) 
	{
		this.windowHeight = windowHeight;
	}
}
