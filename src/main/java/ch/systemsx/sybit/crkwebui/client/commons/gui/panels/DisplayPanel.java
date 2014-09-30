package ch.systemsx.sybit.crkwebui.client.commons.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

/**
 * Base class for different views visible in center panel.
 * @author srebniak_a
 *
 */
public class DisplayPanel extends VerticalLayoutContainer
{
	public static final int MIN_WIDTH = ApplicationContext.getSettings().getScreenSettings().getMinWindowData().getWindowWidth()-150;
	Widget mainWidget = new Widget();
	
	public DisplayPanel()
	{
		this.setScrollMode(ScrollMode.AUTOY);
		this.setHeight(ApplicationContext.getWindowData().getWindowHeight() - 70);
		this.setBorders(false);
		//this.addStyleName("eppic-default-padding");
		//this.addStyleName("eppic-rounded-border");
		this.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				if(event.getWidth() < MIN_WIDTH)
					setWidth(MIN_WIDTH);
				else 
					setWidth(event.getWidth());
				
			}
		});
	}
	
	public void setData(Widget w){
		if(mainWidget != null) this.remove(mainWidget);
		
		mainWidget = w;
		this.add(mainWidget, new VerticalLayoutData(1,1));
	}
	

}
