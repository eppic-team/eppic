package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.top.gui.panels.NavigationPanel;
import ch.systemsx.sybit.crkwebui.client.top.gui.panels.TopPanelSearchBox;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.core.client.util.Margins;

/**
 * Bottom panel containing status label and contact information.
 * @author srebniak_a
 *
 */
public class TopPanel extends FramedPanel
{
	public TopPanel()
	{
		this.setStyleName("eppic-rounded-border");
		
		this.getHeader().setVisible(false);
		this.setHeight(40);
		
		HorizontalLayoutContainer mainContainer = new HorizontalLayoutContainer();
		mainContainer.setHeight(40);
		
		this.setWidget(mainContainer);
		
		mainContainer.add(createLogoPanel(), new HorizontalLayoutData(-1, 1, new Margins(0,10,0,10)));
		mainContainer.add(new TopPanelSearchBox(), new HorizontalLayoutData(1,1, new Margins(0,10,0,10)));
		mainContainer.add(new NavigationPanel(), new HorizontalLayoutData(-1, 1, new Margins(0)));
		
	}
	
	private HBoxLayoutContainer createLogoPanel(){
		HBoxLayoutContainer eppicLogoPanel = new HBoxLayoutContainer();
		eppicLogoPanel.add(getLogo());
		eppicLogoPanel.setWidth(70);
		
		return eppicLogoPanel;
	}
	
	private ImageLinkWithTooltip getLogo() {
		String logoIconSource = "resources/images/eppic-logo.png";
		ImageLinkWithTooltip logo = 
				new ImageLinkWithTooltip(logoIconSource,
						50, 20,
						"EPPIC",
						GWT.getHostPageBaseURL());
		logo.setWidth("50px");
		logo.setHeight("20px");
		return logo;
	}
}
