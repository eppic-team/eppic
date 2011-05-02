package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
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
 * This renderer is used to display interfaces thumbnails
 * @author srebniak_a
 *
 */
public class ThumbnailCellRenderer implements GridCellRenderer<BeanModel> 
{
	private MainController mainController;
	
	private ToolTip toolTip;
	private boolean refreshTooltip = true;

	public ThumbnailCellRenderer(MainController mainController) {
		this.mainController = mainController;
	}
	
	public Object render(final BeanModel model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<BeanModel> store, final Grid<BeanModel> grid) 
	{
		String url = mainController.getSettings().getResultsLocation();
		
		String source = url + 
						mainController.getSelectedJobId() + 
						"/" +
						mainController.getPdbScoreItem().getPdbName() +
						"." +
						model.get("id") +
						".75x75.png";
		
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
					ToolTipConfig tipConfig = new ToolTipConfig();
					tipConfig.setText(MainController.CONSTANTS.results_grid_thumbnail_tooltip_text());
					tipConfig.setDismissDelay(0);
					tipConfig.setShowDelay(1000);
					toolTip = new ToolTip(null, tipConfig);
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
				mainController.runViewer(String.valueOf(model.get("id")));
			}
		});
		
		return image;
//		return "<img src=\"" + 
//				url + 
//				mainController.getSelectedJobId() + 
//				"/" +
//				mainController.getPdbScoreItem().getPdbName() +
//				"." +
//				model.get("id") +
//				".75x75.png" +
//				"\"/>";
	}
}