package ch.systemsx.sybit.crkwebui.client.data;

/**
 * Window application data.
 * @author AS
 *
 */
public class WindowData 
{
	private int windowWidth;
	private int windowHeight;
	
	public WindowData(int windowWidth,
					  int windowHeight)
	{
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
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
}
