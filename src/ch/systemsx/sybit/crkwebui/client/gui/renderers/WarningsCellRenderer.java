package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.WarningItem;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * This renderer is used to display view button used to open viewer
 * @author srebniak_a
 *
 */
public class WarningsCellRenderer implements GridCellRenderer<BaseModel> 
{
	private MainController mainController;
	
	private ToolTip toolTip;
	private boolean refreshTooltip = true;

	public WarningsCellRenderer(MainController mainController) 
	{                                
		this.mainController = mainController;
	}

	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, Grid<BaseModel> grid) 
	{
		final List<WarningItem> warnings = (List<WarningItem>)model.get("warnings");
		
		if((warnings != null) && (warnings.size() > 0))
		{
			String source = "resources/icons/warning_icon.png";
			
			final Image image  = new Image(source);
			image.addMouseOverHandler(new MouseOverHandler() {
				
				@Override
				public void onMouseOver(MouseOverEvent event)
				{
					if((toolTip != null) && (refreshTooltip))
					{
						toolTip.disable();
					}
					
					if(refreshTooltip)
					{
						ToolTipConfig toolTipConfig = new ToolTipConfig();  
						toolTipConfig.setTitle(MainController.CONSTANTS.results_grid_warnings_tooltip_title());  
						toolTipConfig.setMouseOffset(new int[] {0, 0});  
						toolTipConfig.setTemplate(new Template(generateWarningsTemplate(warnings)));  
						
						int width = 500;
						if(width > mainController.getWindowWidth())
						{
							width = image.getAbsoluteLeft();
						}
						
						int toolTipXPosition = image.getAbsoluteLeft() - width;
						
						toolTipConfig.setMinWidth(width);
						toolTipConfig.setMaxWidth(width);
						toolTipConfig.setShowDelay(100);
						toolTipConfig.setDismissDelay(0);
						
						toolTip = new ToolTip(null, toolTipConfig);
						
						toolTip.showAt(toolTipXPosition, 
									   image.getAbsoluteTop() + image.getOffsetWidth() + 5);
						refreshTooltip = false;
					}
				}
			});
			
			image.addMouseOutHandler(new MouseOutHandler() 
			{
				@Override
				public void onMouseOut(MouseOutEvent event) 
				{
					if(toolTip != null)
					{
						toolTip.disable();
					}
					
					refreshTooltip = true;
				}
			});
			
			return image;
		}
		else
		{
			return "";
		}
		
	}
	
	/**
	 * Create list of interface warnings
	 * @param warnings list of warnings to display
	 * @return template containing list of warnings
	 */
	private String generateWarningsTemplate(List<WarningItem> warnings)
	{
		String warningsList = "<div><ul style=\"list-style: disc; margin: 0px 0px 0px 15px;\">";
		
		for(WarningItem warning : warnings)
		{
			if(!warning.getText().equals(""))
			{
				warningsList += "<li>" + warning.getText() + "</li>";
			}
		}
			
		warningsList += "</ul></div>";
		
		return warningsList;
	}
}
