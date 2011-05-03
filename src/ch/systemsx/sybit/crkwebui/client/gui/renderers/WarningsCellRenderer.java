package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
public class WarningsCellRenderer implements GridCellRenderer<BeanModel> 
{
	private MainController mainController;
	
	private ToolTip toolTip;
	private boolean refreshTooltip = true;

	public WarningsCellRenderer(MainController mainController) 
	{                                
		this.mainController = mainController;
	}

	public Object render(final BeanModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BeanModel> store, Grid<BeanModel> grid) 
	{
		String source = "resources/images/gxt/icons/warning_icon.PNG";
		
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
					List<String> warnings = (List<String>)model.get("warnings");
					
					ToolTipConfig toolTipConfig = new ToolTipConfig();  
					toolTipConfig.setTitle(MainController.CONSTANTS.results_grid_warnings_tooltip_title());  
					toolTipConfig.setMouseOffset(new int[] {0, 0});  
					toolTipConfig.setTemplate(new Template(generateWarningsTemplate(warnings)));  
					toolTipConfig.setMaxWidth(mainController.getWindowWidth());
					toolTipConfig.setShowDelay(100);
					toolTipConfig.setDismissDelay(0);
					
					toolTip = new ToolTip(null, toolTipConfig);
					toolTip.showAt(image.getAbsoluteLeft() + image.getOffsetWidth(), 
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
		
		image.addClickHandler(new ClickHandler() 
		{
			@Override
			public void onClick(ClickEvent event)
			{
				mainController.runViewer(String.valueOf(model.get("id")));
			}
		});
		
		return image;
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
