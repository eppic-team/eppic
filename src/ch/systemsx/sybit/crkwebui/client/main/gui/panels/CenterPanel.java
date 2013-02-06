package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.events.SaveResultsPanelGridSettingsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsPanel;

import com.extjs.gxt.ui.client.event.ContainerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * Main central panel.
 * @author srebniak_a
 */
public class CenterPanel extends LayoutContainer 
{
	private DisplayPanel displayPanel;
	
	public CenterPanel()
	{
		this.setBorders(false);
		this.setLayout(new FitLayout());
		
		displayPanel = new DisplayPanel();
		this.add(displayPanel);
		
		this.addListener(Events.BeforeRemove, new Listener<ContainerEvent>() {

			@Override
			public void handleEvent(ContainerEvent ce) 
			{
				if(ce.getItem() instanceof ResultsPanel)
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
		this.removeAll();
		this.displayPanel = displayPanel;
		
		if(displayPanel != null)
		{
			this.add(displayPanel);
		}
		
		this.layout();
	}
}
