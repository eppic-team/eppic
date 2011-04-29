package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;

/**
 * This renderer is used to display view button used to open viewer
 * @author srebniak_a
 *
 */
public class WarningsCellRenderer implements GridCellRenderer<BeanModel> 
{
	private MainController mainController;

	public WarningsCellRenderer(MainController mainController) 
	{
		this.mainController = mainController;
	}

	public Object render(final BeanModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BeanModel> store, Grid<BeanModel> grid) 
	{
		
		ContentPanel warningPanel = new ContentPanel();
		warningPanel.getHeader().setVisible(false);
		warningPanel.setBodyBorder(false);
		warningPanel.setBorders(false);
		warningPanel.setWidth(20);
		warningPanel.setHeight(20);
		
		if(model.get("warnings") != null)
		{
//			warningPanel.setStyleAttribute("background", "white url(resources/images/gxt/icons/warning_icon.png) no-repeat;");
//			warningPanel.setStyleAttribute("background", "url(http://localhost/warning_icon.png) no-repeat;");
			
			List<String> warnings = (List<String>)model.get("warnings");
			
			ToolTipConfig toolTipConfig = new ToolTipConfig();  
			toolTipConfig.setTitle("Warnings");  
			toolTipConfig.setMouseOffset(new int[] {0, 0});  
			toolTipConfig.setAnchor("left");  
			toolTipConfig.setTemplate(new Template(generateWarningsTemplate(warnings)));  
			toolTipConfig.setMaxWidth(mainController.getWindowWidth());
			toolTipConfig.setShowDelay(100);
			toolTipConfig.setDismissDelay(0);
			
			warningPanel.setToolTip(toolTipConfig); 
		}
		
		
		warningPanel.add(IconHelper.createPath("resources/images/gxt/icons/warning_icon.PNG", 20, 20).createImage());
		
		return warningPanel;
	}
	
	private String generateWarningsTemplate(List<String> warnings)
	{
		String warningsList = "<div><ul style=\"list-style: disc; margin: 0px 0px 0px 15px\">";
		
		for(String warning : warnings)
		{
			warningsList += "<li>" + warning + "</li>";
		}
			
		warningsList += "</ul></div>";
		
		return warningsList;
	}
	
	

}
