package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;
import ch.systemsx.sybit.crkwebui.client.model.GridColumnSettings;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class GridColumnConfigGenerator
{

	public static List<ColumnConfig> createColumnConfigs(MainController mainController,
														 String gridName,
														 BaseModel model)
	{
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		String columnOrder = mainController.getSettings().getGridProperties()
				.get(gridName + "_columns");

		String[] columns = null;

		if (columnOrder == null) {
			columns = new String[model.getPropertyNames().size()];
			
			Iterator<String> fieldsIterator = model.getPropertyNames()
					.iterator();

			int i = 0;

			while (fieldsIterator.hasNext()) {
				columns[i] = fieldsIterator.next();
				i++;
			}
		} else {
			columns = columnOrder.split(",");
		}

		for (String columnName : columns) {
			boolean addColumn = true;

			String customAdd = mainController.getSettings().getGridProperties()
					.get(gridName + "_" + columnName + "_add");
			if ((customAdd != null) && (!customAdd.equals("yes")))
			{
				addColumn = false;
			}

			if (addColumn) 
			{
				GridColumnSettings gridColumnSettings = fillGridColumnSettings(mainController.getSettings().getGridProperties(), 
																			   gridName, 
																			   columnName, 
																			   null);
				
				if (columnName.equals("METHODS")) 
				{
					for (SupportedMethod method : mainController.getSettings().getScoresTypes()) 
					{
						GridColumnSettings methodGridColumnSettings = fillGridColumnSettings(mainController.getSettings().getGridProperties(), 
								   gridName, 
								   method.getName(), 
								   gridColumnSettings);
						
						ColumnConfig column = createColumnConfig(methodGridColumnSettings,
								 								 method.getName(),
								 								 mainController);
						
						configs.add(column);
					}
				}
				else
				{
					ColumnConfig column = createColumnConfig(gridColumnSettings,
															 columnName,
															 mainController);

					configs.add(column);
				}
			}

		}
		
		return configs;
	}
	
	private static GridColumnSettings fillGridColumnSettings(Map<String, String> gridProperties,
														String gridName,
														String columnName,
														GridColumnSettings gridColumnSetttingsInput)
	{
		GridColumnSettings gridColumnSettings = new GridColumnSettings();
		
		if(gridColumnSetttingsInput != null)
		{
			gridColumnSettings.setColumnWidth(gridColumnSetttingsInput.getColumnWidth());
			gridColumnSettings.setDisplayColumn(gridColumnSetttingsInput.isDisplayColumn());
			gridColumnSettings.setResizable(gridColumnSetttingsInput.isResizable());
			gridColumnSettings.setDisableColumnContextMenu(gridColumnSetttingsInput.isDisableColumnContextMenu());
			gridColumnSettings.setRenderer(gridColumnSetttingsInput.getRenderer());
			gridColumnSettings.setHeader(gridColumnSetttingsInput.getHeader());
			gridColumnSettings.setTooltip(gridColumnSetttingsInput.getTooltip());
		}
			
		
		String customVisibility = gridProperties
				.get(gridName + "_" + columnName + "_visible");
		if (customVisibility != null) 
		{
			boolean displayColumn = true;
			
			if (!customVisibility.equals("yes")) {
				displayColumn = false;
			}
			
			gridColumnSettings.setDisplayColumn(displayColumn);
		}
		
		String customColumnWidth = gridProperties
				.get(gridName + "_" + columnName + "_width");
		if (customColumnWidth != null) 
		{
			int columnWidth = Integer.parseInt(customColumnWidth);
			gridColumnSettings.setColumnWidth(columnWidth);
		}

		String renderer = gridProperties
				.get(gridName + "_" + columnName + "_renderer");
		if(renderer != null)
		{
			gridColumnSettings.setRenderer(renderer);
		}

		String header = columnName;
		String customHeader = gridProperties
				.get(gridName + "_" + columnName + "_header");
		if (customHeader != null) 
		{
			header = customHeader;
		}
		
		gridColumnSettings.setHeader(header);
		
		String customIsResizable = gridProperties
				.get(gridName + "_" + columnName + "_resizable");
		if (customIsResizable != null) 
		{
			boolean isResizable = true;
			
			if (!customIsResizable.equals("yes")) {
				isResizable = false;
			}
			
			gridColumnSettings.setResizable(isResizable);
		}
		
		String customDisbaleColumnContextMenu = gridProperties
				.get(gridName + "_" + columnName + "_disablemenu");
		if (customDisbaleColumnContextMenu != null) 
		{
			boolean disableColumnContextMenu = false;
			
			if (customDisbaleColumnContextMenu.equals("yes")) {
				disableColumnContextMenu = true;
			}
			
			gridColumnSettings.setDisableColumnContextMenu(disableColumnContextMenu);
		}
		
		String tootlip = gridProperties
				.get(gridName + "_" + columnName + "_tooltip");
		if(tootlip != null)
		{
			gridColumnSettings.setTooltip(tootlip);
		}
		
		return gridColumnSettings;
	}
	
	private static ColumnConfig createColumnConfig(GridColumnSettings gridColumnSettings,
												   String columnName,
												   MainController mainController)
	{
		ColumnConfig column = new ColumnConfig();
		column.setId(columnName);
		column.setHeader(gridColumnSettings.getHeader());
		column.setWidth(gridColumnSettings.getColumnWidth());
		column.setAlignment(HorizontalAlignment.CENTER);
		column.setHidden(!gridColumnSettings.isDisplayColumn());
		
		column.setResizable(gridColumnSettings.isResizable());
		column.setMenuDisabled(gridColumnSettings.isDisableColumnContextMenu());

		if (gridColumnSettings.getRenderer() != null) {
			
			GridCellRenderer<BaseModel> renderer = null;
			if ((gridColumnSettings.getRenderer() != null) && (!gridColumnSettings.getRenderer().equals(""))) {
				renderer = GridCellRendererFactory.createGridCellRenderer(
						gridColumnSettings.getRenderer(), mainController);
			}
			
			column.setRenderer(renderer);
		}
		
		if (gridColumnSettings.getTooltip() != null) {
			column.setToolTip(gridColumnSettings.getTooltip());
		}
		
		return column;
	}
}
