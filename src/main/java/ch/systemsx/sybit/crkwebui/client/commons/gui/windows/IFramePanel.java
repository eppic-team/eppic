package ch.systemsx.sybit.crkwebui.client.commons.gui.windows;


import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;

public class IFramePanel extends DisplayPanel {

    public IFramePanel(String url) {
	this.addStyleName("eppic-text-panel");
	this.setScrollMode(ScrollMode.NONE);
	
	Iframe iframe = new Iframe(GWT.getHostPageBaseURL() + url);
	Iframe footer = new Iframe(GWT.getHostPageBaseURL() + "footer.html");
	footer.addStyleName("eppic-iframe-content-footer");
	iframe.add(footer);
	iframe.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				if(event.getWidth() < MIN_WIDTH)
					setWidth(MIN_WIDTH);
				else 
					setWidth(event.getWidth());
				
			}
		});
	this.setData(iframe);

    }
}
