package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import model.InterfaceResidueItem;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.table.NumberCellRenderer;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.client.impl.SchedulerImpl;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

public class NumberRenderer implements GridCellRenderer<BeanModel> 
{
	private NumberFormat number = NumberFormat.getFormat("0.00");  
	private NumberCellRenderer<Grid<BeanModel>> numberRenderer = new NumberCellRenderer<Grid<BeanModel>>(number);  
	
	public Object render(final BeanModel model, 
						 String property,
						 ColumnData config, 
						 final int rowIndex, 
						 final int colIndex,
						 ListStore<BeanModel> store, 
						 Grid<BeanModel> grid) 
	{
		return numberRenderer.render(null, property, model.get(property));  
	}
}
