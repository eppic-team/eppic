package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * Renderer used to display icon used to delete job.
 * @author AS
 */
public class DeleteJobCellRenderer implements GridCellRenderer<BaseModel> 
{
	private MainController mainController;
	
	private ToolTip toolTip;
	private boolean refreshTooltip = true;

	public DeleteJobCellRenderer(MainController mainController) 
	{                                
		this.mainController = mainController;
	}

	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) 
	{
		String source = "resources/icons/delete_icon.png";
		
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
					toolTipConfig.setText(AppPropertiesManager.CONSTANTS.myjobs_grid_delete_tooltip());  
					
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
		
		image.addDoubleClickHandler(new DoubleClickHandler() 
		{
			@Override
			public void onDoubleClick(DoubleClickEvent event)
			{
				mainController.deleteJob(String.valueOf(model.get("jobid")));
			}
		});
		
		return image;
	}
}
