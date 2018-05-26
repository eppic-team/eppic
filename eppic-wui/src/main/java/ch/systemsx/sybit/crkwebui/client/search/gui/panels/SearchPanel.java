package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.server.db.data.PDBSearchResult;

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
		
		dock.addNorth(headerPanel, 80);
		dock.add(gridPanel);
		
		this.setData(dock);
	}
	
	public void fillSearchPanel(String pdBCode, String chain, String searchResultLabel, List<PDBSearchResult> list){		
		headerPanel.updateContent(searchResultLabel, pdBCode + " Chain " + chain, list.size());
		gridPanel.fillGrid(list, pdBCode);
	}
	
	public void resizePanel(){
		int width = ApplicationContext.getWindowData().getWindowWidth() - 180;
		
		if(width < MIN_WIDTH) width = MIN_WIDTH-20;
		
		gridPanel.resizeGrid(width);
		headerPanel.resizePanel(width + 30);
	}
}
