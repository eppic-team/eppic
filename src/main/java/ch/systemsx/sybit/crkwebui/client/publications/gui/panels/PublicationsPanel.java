package ch.systemsx.sybit.crkwebui.client.publications.gui.panels;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.Iframe;

/**
 * Panel used to display publications
 * @author biyani_n
 *
 */
public class PublicationsPanel extends DisplayPanel{

	public PublicationsPanel(){
		this.addStyleName("eppic-text-panel");
		this.setScrollMode(ScrollMode.NONE);

		Iframe iframe = new Iframe(GWT.getHostPageBaseURL() + "publications.html");
		this.setData(iframe);
	}
}
