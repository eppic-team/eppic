package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import model.InterfaceScoreItemKey;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class MethodCellRenderer implements GridCellRenderer<BeanModel>
{
	private MainController mainController;
	
	public MethodCellRenderer(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	public Object render(final BeanModel model, 
						 String property, 
						 ColumnData config, 
						 final int rowIndex,  
						 final int colIndex, 
						 ListStore<BeanModel> store, 
						 Grid<BeanModel> grid) 
	{  
		InterfaceScoreItemKey interfaceScoreItemKey = new InterfaceScoreItemKey();
		interfaceScoreItemKey.setInterfaceId(rowIndex + 1);
		interfaceScoreItemKey.setMethod(property);
		
		///TODO
		for(InterfaceScoreItemKey k : mainController.getPdbScoreItem().getInterfaceScores().keySet())
		{
			if(interfaceScoreItemKey.equals(k))
			{
				String color = "black";
				String value = mainController.getPdbScoreItem().getInterfaceScores().get(k).getCall();
				
				if(value == null)
				{
					return value;
				}
				else if(value.equals("bio"))
				{
					color = "green";
				}
				else if(value.equals("xtal"))
				{
					color = "red";
				}
				else
				{
					return value;
				}
				
				return "<span style='font-weight: bold;color:" + color + "'>" + value + "</span>";  
			}
		}
		
		return "";
	}  

}
