package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.events.SaveResultsPanelGridSettingsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsPanel;

import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.BeforeRemoveEvent;
import com.sencha.gxt.widget.core.client.event.BeforeRemoveEvent.BeforeRemoveHandler;

/**
 * Main central panel.
 * @author srebniak_a
 */
public class CenterPanel extends SimpleContainer 
{
	private DisplayPanel displayPanel;
	
	public CenterPanel()
	{
		this.setBorders(false);
		
		displayPanel = new DisplayPanel();
		this.setWidget(displayPanel);
		
		this.addBeforeRemoveHandler(new BeforeRemoveHandler() {
			@Override
			public void onBeforeRemove(BeforeRemoveEvent event) {
				if(event.getWidget() instanceof ResultsPanel)
				{
					EventBusManager.EVENT_BUS.fireEvent(new SaveResultsPanelGridSettingsEvent());
				}
				
			}
		});
	}
	
	/**
	 * Retrieves content of the central panel.
	 * @return content of central panel
	 */
	public DisplayPanel getDisplayPanel()
	{
		return displayPanel;
	}
	
	/**
	 * Sets content of the central panel.
	 * @param displayPanel content of central panel
	 */
	public void setDisplayPanel(DisplayPanel displayPanel)
	{
		displayPanel.clearSizeCache();
		displayPanel.setPixelSize(this.getOffsetWidth(), this.getOffsetHeight());
		this.displayPanel = displayPanel;
		
		if(displayPanel != null)
		{
			this.setWidget(displayPanel);
		}
	}
}
