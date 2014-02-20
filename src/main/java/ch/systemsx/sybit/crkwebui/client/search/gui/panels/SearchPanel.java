package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;

/**
 * Panel to display search results
 * @author biyani_n
 *
 */
public class SearchPanel extends DisplayPanel
{
	private SearchHeaderPanel headerPanel;
	private SearchGridPanel gridPanel;
	
	public SearchPanel(String uniProtId, String searchResultLabel){
		headerPanel = new SearchHeaderPanel();
		headerPanel.updateContent(searchResultLabel, uniProtId);
		
		gridPanel = new SearchGridPanel(uniProtId);
		
		DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
		
		dock.addNorth(headerPanel, 50);
		dock.add(gridPanel);
		
		this.setData(dock);
	}
}
