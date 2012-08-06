package ch.systemsx.sybit.crkwebui.client.gui;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * Base class for different views visible in center panel.
 * @author srebniak_a
 *
 */
public class DisplayPanel extends LayoutContainer 
{
	public DisplayPanel()
	{
		this.setBorders(false);
		this.setLayout(new FitLayout());
	}
}
