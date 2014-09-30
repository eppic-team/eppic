package ch.systemsx.sybit.crkwebui.client.commons.gui.windows;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.sencha.gxt.widget.core.client.Dialog;

/**
 * Generic resizable window.
 * @author AS, nikhil
 */
public class ResizableWindow extends Dialog
{
	private int defaultWidth;
	private int defaultHeight;

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
		
		this.defaultHeight = windowHeight;
		this.defaultWidth = windowWidth;
		this.setPixelSize(windowWidth, windowHeight);
	}
	
	public int getDefaultWidth() {
		return defaultWidth;
	}

	public void setDefaultWidth(int defaultWidth) {
		this.defaultWidth = defaultWidth;
	}

	public int getDefaultHeight() {
		return defaultHeight;
	}

	public void setDefaultHeight(int defaultHeight) {
		this.defaultHeight = defaultHeight;
	}

	@Override
	public void setPagePosition(int x, int y) {
		int browserWidth = ApplicationContext.getWindowData().getWindowWidth();
		int windowWidth = this.defaultWidth;
		
		if((x + windowWidth) > browserWidth){
			x = browserWidth - windowWidth;
		}
		
		super.setPagePosition(x, y);
	}
}
