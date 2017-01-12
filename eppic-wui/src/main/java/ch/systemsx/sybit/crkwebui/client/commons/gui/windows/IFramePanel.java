package ch.systemsx.sybit.crkwebui.client.commons.gui.windows;


import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Frame;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.container.AbstractHtmlLayoutContainer.HtmlData;
import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
import com.sencha.gxt.widget.core.client.container.Viewport;

public class IFramePanel extends DisplayPanel {

    public interface HtmlLayoutContainerTemplate extends XTemplates {
	@XTemplate("<div class='main' style=\"height:100%;\"></div><div class='footer'></div>")
	SafeHtml getTemplate();
    }

    public IFramePanel(String url, boolean isRelativeUrl) {
	this.addStyleName("eppic-text-panel");
	this.setScrollMode(ScrollMode.NONE);

	HtmlLayoutContainerTemplate templates = GWT.create(HtmlLayoutContainerTemplate.class);
	HtmlLayoutContainer container = new HtmlLayoutContainer(templates.getTemplate());

	//uncomment - test only
	//Frame iframe = new Frame(GWT.getHostPageBaseURL() + url);
	
	//for eppic explorer integration
	Frame iframe = null;
	if (isRelativeUrl){
		iframe = new Frame(url);
	}else{
		iframe = new Frame(GWT.getHostPageBaseURL() + url);
	}
	
	
	iframe.removeStyleName("gwt-Frame");
	iframe.addStyleName("eppic-ifram-content-main");
	container.add(iframe, new HtmlData(".main"));
	Frame footer = new Frame(GWT.getHostPageBaseURL() + "footer.html");
	footer.addStyleName("eppic-iframe-content-footer");
	footer.removeStyleName("gwt-Frame");
	container.add(footer, new HtmlData(".footer"));

	
	Viewport v = new Viewport();
	v.add(container);
	this.setData(v);
    }
}
