package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.footer.gui.panels.BottomLinksPanel;

import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * Bottom panel containing status
 * @author srebniak_a; nikhil
 *
 */
public class BottomPanel extends SimpleContainer
{
	public BottomPanel()
	{
		//this.getHeader().setVisible(false);
		this.setHeight(15);
		
		HorizontalLayoutContainer mainContainer = new HorizontalLayoutContainer();
		//StatusMessagePanel statusMessagePanel = new StatusMessagePanel();
	    //mainContainer.add(statusMessagePanel, new HorizontalLayoutData(-1, 1));
		
		BottomLinksPanel bottomLinksPanel = new BottomLinksPanel();
		mainContainer.add(bottomLinksPanel, new HorizontalLayoutData(1,1));
		
		this.setWidget(mainContainer);
	}
}
