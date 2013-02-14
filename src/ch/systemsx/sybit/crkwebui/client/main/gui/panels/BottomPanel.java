package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.navigation.gui.panels.NavigationPanel;
import ch.systemsx.sybit.crkwebui.client.status.gui.panels.StatusMessagePanel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * Bottom panel containing status label and contact information.
 * @author srebniak_a
 *
 */
public class BottomPanel extends LayoutContainer
{
	public BottomPanel()
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));

		StatusMessagePanel statusMessagePanel = new StatusMessagePanel();
	    this.add(statusMessagePanel, new RowData(1, 1, new Margins(0)));

	    NavigationPanel navigationPanel = new NavigationPanel();
		this.add(navigationPanel, new RowData(500, 1, new Margins(0)));
	}
}
