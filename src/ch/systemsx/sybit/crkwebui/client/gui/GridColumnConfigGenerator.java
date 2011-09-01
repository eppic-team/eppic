package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;

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
				boolean displayColumn = true;

				String customVisibility = mainController.getSettings()
						.getGridProperties()
						.get(gridName + "_" + columnName + "_visible");
				if (customVisibility != null) {
					if (!customVisibility.equals("yes")) {
						displayColumn = false;
					}
				}

				int columnWidth = 75;
				String customColumnWidth = mainController.getSettings()
						.getGridProperties()
						.get(gridName + "_" + columnName + "_width");
				if (customColumnWidth != null) {
					columnWidth = Integer.parseInt(customColumnWidth);
				}

				String customRenderer = mainController.getSettings()
						.getGridProperties()
						.get(gridName + "_" + columnName + "_renderer");

				GridCellRenderer<BaseModel> renderer = null;
				if ((customRenderer != null) && (!customRenderer.equals(""))) {
					renderer = GridCellRendererFactory.createGridCellRenderer(
							customRenderer, mainController);
				}

				String header = columnName;
				String customHeader = mainController.getSettings()
						.getGridProperties()
						.get(gridName + "_" + columnName + "_header");
				if (customHeader != null) {
					header = customHeader;
				}
				
				boolean isResizable = true;

				String customIsResizable = mainController.getSettings()
						.getGridProperties()
						.get(gridName + "_" + columnName + "_resizable");
				if (customIsResizable != null) {
					if (!customIsResizable.equals("yes")) {
						isResizable = false;
					}
				}
				
				String tootlip = mainController.getSettings()
						.getGridProperties()
						.get(gridName + "_" + columnName + "_tooltip");

				if (columnName.equals("METHODS")) {
					for (String method : mainController.getSettings()
							.getScoresTypes()) {
						ColumnConfig column = new ColumnConfig();
						column.setId(method);
						column.setHeader(method);
						column.setWidth(columnWidth);
						column.setAlignment(HorizontalAlignment.CENTER);
						column.setHidden(!displayColumn);
						
						column.setResizable(isResizable);

						if (renderer != null) {
							column.setRenderer(renderer);
						}
						
						if (tootlip != null) {
							column.setToolTip(tootlip);
						}

						configs.add(column);
					}
				} else {
					ColumnConfig column = new ColumnConfig();
					column.setId(columnName);
					column.setHeader(header);
					column.setWidth(columnWidth);
					column.setAlignment(HorizontalAlignment.CENTER);
					column.setHidden(!displayColumn);
					
					column.setResizable(isResizable);

					if (renderer != null) {
						column.setRenderer(renderer);
					}
					
					if (tootlip != null) {
						column.setToolTip(tootlip);
					}

					configs.add(column);
				}
			}

		}
		
		return configs;
	}
}
