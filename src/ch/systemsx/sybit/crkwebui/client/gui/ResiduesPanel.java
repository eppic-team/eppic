package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.grid.filters.Filter;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;

public class ResiduesPanel extends FormPanel
{
	private List<ColumnConfig> residuesConfigs;
	private ListStore<BeanModel> residuesStore;
	private ColumnModel residuesColumnModel;
	private Grid<BeanModel> residuesGrid;

	private MainController mainController;
	
	public ResiduesPanel(final InterfacesResiduesPanel parentPanel,
						 String header, 
						 MainController mainController) 
	{
		this.mainController = mainController;
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setPadding(0);
		this.getHeader().setVisible(false);

		residuesConfigs = createColumnConfig();

		residuesStore = new ListStore<BeanModel>();
		
		residuesStore.addFilter(new StoreFilter<BeanModel>() {
			
			@Override
			public boolean select(Store<BeanModel> store,
								  BeanModel parent,
								  BeanModel item, 
								  String property) 
			{
				if(parentPanel.isShowAll())
				{
					return true;
				}
				else if(item.get(property).equals("ABC"))
				{
					return false;
				}
				
				return true;
			}
		});
		
		residuesColumnModel = new ColumnModel(residuesConfigs);

		residuesColumnModel.addHeaderGroup(0, 0, new HeaderGroupConfig(header,
				1, residuesColumnModel.getColumnCount()));

		residuesGrid = new Grid<BeanModel>(residuesStore, residuesColumnModel);
		residuesGrid.setBorders(true);
		residuesGrid.setStripeRows(true);
		residuesGrid.setColumnLines(true);
		residuesGrid.getView().setForceFit(true);

		this.add(residuesGrid);

	}

	private List<ColumnConfig> createColumnConfig() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(
				InterfaceResidueItem.class);
		BeanModel model = beanModelFactory
				.createModel(new InterfaceResidueItem());

		// String list = "";
		//
		// for(int i=0; i<model.getPropertyNames().size(); i++)
		// {
		// list = list + "," + model.getProperties().values().;
		// }
		//
		String columnOrder = mainController.getSettings().getGridProperties()
				.get("residues_columns");

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
					.get("residues_" + columnName + "_add");
			if (customAdd != null) {
				if (!customAdd.equals("yes")) {
					addColumn = false;
				}
			}

			if (addColumn) {
				boolean displayColumn = true;

				String customVisibility = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_visible");
				if (customVisibility != null) {
					if (!customVisibility.equals("yes")) {
						displayColumn = false;
					}
				}

				int columnWidth = 75;
				String customColumnWidth = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_width");
				if (customColumnWidth != null) {
					columnWidth = Integer.parseInt(customColumnWidth);
				}

				String customRenderer = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_renderer");

				GridCellRenderer<BeanModel> renderer = null;
				if ((customRenderer != null) && (!customRenderer.equals(""))) {
					renderer = GridCellRendererFactory.createGridCellRenderer(
							customRenderer, mainController);
				}

				String header = columnName;
				String customHeader = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_header");
				if (customHeader != null) {
					header = customHeader;
				}

				if (columnName.equals("METHODS")) {
					for (String method : mainController.getSettings()
							.getScoresTypes()) {
						ColumnConfig column = new ColumnConfig();
						column.setId(method);
						column.setHeader(method);
						column.setWidth(columnWidth);
						column.setAlignment(HorizontalAlignment.CENTER);
						column.setHidden(!displayColumn);

						if (renderer != null) {
							column.setRenderer(renderer);
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

					if (renderer != null) {
						column.setRenderer(renderer);
					}

					configs.add(column);
				}
			}
		}

		return configs;

	}

	public void fillResiduesGrid(List<InterfaceResidueItem> residueValues) {
		residuesStore.removeAll();

		List<BeanModel> data = new ArrayList<BeanModel>();

		if (residueValues != null) {
			for (InterfaceResidueItem residueValue : residueValues) {
				BeanModelFactory beanModelFactory = BeanModelLookup.get()
						.getFactory(InterfaceResidueItem.class);
				BeanModel model = beanModelFactory.createModel(residueValue);

				for (String method : mainController.getSettings()
						.getScoresTypes()) {
					if (residueValue.getInterfaceResidueMethodItems()
							.containsKey(method)) {
						model.set(method, residueValue
								.getInterfaceResidueMethodItems().get(method)
								.getScore());
					}
				}

				data.add(model);
			}
		}

		residuesStore.add(data);
		residuesGrid.reconfigure(residuesStore, residuesColumnModel);
	}
	
	public void applyFilter()
	{
		residuesStore.applyFilters("residueType");
	}
}
