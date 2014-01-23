package ch.systemsx.sybit.crkwebui.client.faq.gui.panels;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.Iframe;

/**
 * Panel used to display FAQ's
 * @author biyani_n
 *
 */
public class FAQPanel extends DisplayPanel {

	public FAQPanel(){
		this.addStyleName("eppic-text-panel");
		this.setScrollMode(ScrollMode.NONE);

		Iframe iframe = new Iframe(GWT.getHostPageBaseURL() + "faq.html");
		this.setData(iframe);
	}
	
}
