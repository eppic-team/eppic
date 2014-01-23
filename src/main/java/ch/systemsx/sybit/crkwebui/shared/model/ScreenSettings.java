package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

/**
 * Screen settings stored on the server side and transfered to the client during initialization.
 * @author srebniak_a
 *
 */
public class ScreenSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Min width/height of main screen.
	 */
	private WindowData minWindowData;
	
	public ScreenSettings()
	{
		minWindowData = new WindowData();
	}

	/**
	 * Sets min height/width of main application screen.
	 * @param minWindowHeight min height/width of main application screen
	 */
	public void setMinWindowData(WindowData minWindowData) {
		this.minWindowData = minWindowData;
	}
	
	/**
	 * Retrieves min height/width of main application screen.
	 * @return min height/width of main application screen
	 */
	public WindowData getMinWindowData() {
		return minWindowData;
	}

}
