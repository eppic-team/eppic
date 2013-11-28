package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.status.gui.panels.StatusMessagePanel;

import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;

/**
 * Bottom panel containing status
 * @author srebniak_a; nikhil
 *
 */
public class BottomPanel extends HorizontalLayoutContainer
{
	public BottomPanel()
	{
		StatusMessagePanel statusMessagePanel = new StatusMessagePanel();
	    this.add(statusMessagePanel, new HorizontalLayoutData(1, 1));
	}
}
