package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class GridCellRendererFactory 
{
	public static GridCellRenderer<BeanModel> createGridCellRenderer(String rendererName,
																	 MainController mainController)
	{
		GridCellRenderer<BeanModel> renderer = new DefaultCellRenderer();
		
		if(rendererName !=  null)
		{
			if(rendererName.equals("details"))
			{
				renderer = new DetailsButtonCellRenderer(mainController);
			}
			else if(rendererName.equals("methods"))
			{
				renderer = new MethodCellRenderer(mainController);
			}
		}
		
		return renderer;
	}
}
