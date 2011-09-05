package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BaseModel;
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

public class StopIconRenderer implements GridCellRenderer<BaseModel> 
{
	private MainController mainController;
	
	private ToolTip toolTip;
	private boolean refreshTooltip = true;

	public StopIconRenderer(MainController mainController) 
	{                                
		this.mainController = mainController;
	}

	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) 
	{
		String status = model.get("status");
		
		if((status != null) && (status.equals("Running")))
		{
			String source = "resources/images/gxt/icons/stop_icon.PNG";
			
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
						toolTipConfig.setMouseOffset(new int[] {0, 0});  
						toolTipConfig.setText(MainController.CONSTANTS.myjobs_grid_stop_tooltip());  
						
						toolTip = new ToolTip(null, toolTipConfig);
						toolTip.showAt(image.getAbsoluteLeft() + image.getOffsetWidth(), 
									   image.getAbsoluteTop());
						
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
					BaseModel selectedItem = grid.getSelectionModel().getSelectedItem();
					mainController.stopJob(String.valueOf(selectedItem.get("jobid")));
				}
			});
			
			return image;
		}
		else
		{
			return "";
		}
	}
}
