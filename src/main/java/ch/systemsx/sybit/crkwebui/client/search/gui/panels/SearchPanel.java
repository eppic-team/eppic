package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

/**
 * Panel to display search results
 * @author biyani_n
 *
 */
public class SearchPanel extends DisplayPanel
{
	private SearchHeaderPanel headerPanel;
	private SearchGridPanel gridPanel;
	
	public SearchPanel(){
		headerPanel = new SearchHeaderPanel();		
		gridPanel = new SearchGridPanel();
		
		DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
		
		dock.addNorth(headerPanel, 50);
		dock.add(gridPanel);
		
		this.setData(dock);
	}
	
	public void fillSearchPanel(String uniProtId, String searchResultLabel, List<PDBSearchResult> list){
		headerPanel.updateContent(searchResultLabel, uniProtId);
		gridPanel.fillGrid(list);
	}
	
	public void resizePanel(){
		gridPanel.resizeGrid();
	}
}
