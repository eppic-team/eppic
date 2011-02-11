package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.InterfaceItem;
import model.InterfaceScoreItemKey;
import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class OverviewPanel extends FormPanel {
	private MainController mainController;

	private List<ColumnConfig> resultsConfigs;
	private ListStore<BeanModel> resultsStore;
	private ColumnModel resultsColumnModel;
	private Grid<BeanModel> resultsGrid;
	private GridCellRenderer<BeanModel> methodRenderer;
	// private GridCellRenderer<BeanModel> detailsButtonRenderer;

	private PDBScoreItem resultsData;

	private InfoPanel infoPanel;
	private ScoresPanel scoresPanel;
	private FormPanel scoresPanelWrapper;

	public OverviewPanel(MainController mainController,
			final PDBScoreItem resultsData) {
		this.mainController = mainController;
		this.resultsData = resultsData;
		this.setBorders(true);
		this.setBodyBorder(false);
		this.getHeader().setVisible(false);
		this.setLayout(new RowLayout(Orientation.VERTICAL));

		// detailsButtonRenderer = new GridCellRenderer<BeanModel>()
		// {
		//
		// private boolean init;
		//
		// public Object render(final BeanModel model, String property,
		// ColumnData config, final int rowIndex,
		// final int colIndex, ListStore<BeanModel> store, Grid<BeanModel> grid)
		// {
		// if (!init)
		// {
		// init = true;
		// grid.addListener(Events.ColumnResize, new
		// Listener<GridEvent<BeanModel>>()
		// {
		//
		// public void handleEvent(GridEvent<BeanModel> be)
		// {
		// for (int i = 0; i < be.getGrid().getStore().getCount(); i++)
		// {
		// if (be.getGrid().getView().getWidget(i, be.getColIndex()) != null
		// && be.getGrid().getView().getWidget(i, be.getColIndex()) instanceof
		// BoxComponent)
		// {
		// ((BoxComponent) be.getGrid().getView().getWidget(i,
		// be.getColIndex())).setWidth(be.getWidth() - 10);
		// }
		// }
		// }
		// });
		// }
		//
		// Button detailsButton = new Button("Details", new
		// SelectionListener<ButtonEvent>() {
		//
		// @Override
		// public void componentSelected(ButtonEvent ce)
		// {
		// InterfacesWindow iw = new InterfacesWindow(null);
		// iw.show();
		// }
		// });
		//
		// detailsButton.setWidth(grid.getColumnModel().getColumnWidth(colIndex)
		// - 10);
		// detailsButton.setToolTip("Show interfaces details");
		//
		// return detailsButton;
		// }
		// };

		createInfoPanel();

		resultsConfigs = createColumnConfig();

		resultsStore = new ListStore<BeanModel>();

		resultsColumnModel = new ColumnModel(resultsConfigs);

		// if (widget) {
		// Slider s = new Slider();
		// s.setWidth(100);
		//
		// FlexTable tbl = new FlexTable();
		// tbl.setWidth("100%");
		// tbl.setHTML(0, 0, " ");
		// tbl.setHTML(0, 1,
		// "<span style='white-space: nowrap;font-size: 11px'>Slide Me:  </span>");
		// tbl.setWidget(0, 2, s);
		// tbl.setHTML(0, 3, " ");
		// tbl.getCellFormatter().setWidth(0, 0, "50%");
		// tbl.getCellFormatter().setWidth(0, 3, "50%");
		// cm.addHeaderGroup(1, 0, new HeaderGroupConfig(tbl, 1, 2));
		// } else {
		// cm.addHeaderGroup(1, 0, new HeaderGroupConfig("Stock Information", 1,
		// 2));
		// }

		resultsGrid = new Grid<BeanModel>(resultsStore, resultsColumnModel);
		// resultsGrid.setStyleAttribute("borderTop", "none");
		resultsGrid.getView().setForceFit(true);
		resultsGrid.setBorders(true);
		resultsGrid.setStripeRows(true);
		resultsGrid.setColumnLines(true);

		scoresPanelWrapper = new FormPanel();
		scoresPanelWrapper.setLayout(new FitLayout());
		scoresPanelWrapper.setBodyBorder(false);
		scoresPanelWrapper.setBorders(false);
		scoresPanelWrapper.getHeader().setVisible(false);
		scoresPanelWrapper.setPadding(0);

		resultsGrid.addListener(Events.CellClick, new Listener<GridEvent>() {
			@Override
			public void handleEvent(GridEvent be) {
				updateScoresPanel(be.getRowIndex());
			}
		});

		this.add(resultsGrid, new RowData(1, 0.65, new Margins(0)));

		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 0.05, new Margins(0)));

		// scoresPanel.setVisible(false);
		this.add(scoresPanelWrapper, new RowData(1, 0.3, new Margins(0)));
	}

	public void updateScoresPanel(int selectedRow) {
		// int interfaceId = selectedRow + 1;
		// History.newItem("interface" + interfaceId + "/" +
		// resultsData.getJobId());

		if (scoresPanel == null) {
			createScoresPanel();
			scoresPanelWrapper.add(scoresPanel);
			scoresPanelWrapper.layout();
		}

		scoresPanel.fillResultsGrid(resultsData, selectedRow + 1);
	}

	private List<ColumnConfig> createColumnConfig() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(
				InterfaceItem.class);
		BeanModel model = beanModelFactory.createModel(new InterfaceItem());

		// String list = "";
		//
		// for(int i=0; i<model.getPropertyNames().size(); i++)
		// {
		// list = list + "," + model.getProperties().values().;
		// }
		//
		String columnOrder = mainController.getSettings().getGridProperties()
				.get("results_columns");

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
					.get("results_" + columnName + "_add");
			if (customAdd != null) {
				if (!customAdd.equals("yes")) {
					addColumn = false;
				}
			}

			if (addColumn) {
				boolean displayColumn = true;

				String customVisibility = mainController.getSettings()
						.getGridProperties()
						.get("results_" + columnName + "_visible");
				if (customVisibility != null) {
					if (!customVisibility.equals("yes")) {
						displayColumn = false;
					}
				}

				int columnWidth = 75;
				String customColumnWidth = mainController.getSettings()
						.getGridProperties()
						.get("results_" + columnName + "_width");
				if (customColumnWidth != null) {
					columnWidth = Integer.parseInt(customColumnWidth);
				}

				String customRenderer = mainController.getSettings()
						.getGridProperties()
						.get("results_" + columnName + "_renderer");

				GridCellRenderer<BeanModel> renderer = null;
				if ((customRenderer != null) && (!customRenderer.equals(""))) {
					renderer = GridCellRendererFactory.createGridCellRenderer(
							customRenderer, mainController);
				}

				String header = columnName;
				String customHeader = mainController.getSettings()
						.getGridProperties()
						.get("results_" + columnName + "_header");
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

		// column.setId("id");
		// column.setHeader("id");
		// column.setWidth(50);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// column = new ColumnConfig();
		// column.setId("interface");
		// column.setHeader("Interface");
		// column.setWidth(100);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// column = new ColumnConfig();
		// column.setId("area");
		// column.setHeader("Area");
		// column.setWidth(100);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// column = new ColumnConfig();
		// column.setId("size1");
		// column.setHeader("Size 1");
		// column.setWidth(75);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// column = new ColumnConfig();
		// column.setId("size2");
		// column.setHeader("Size 2");
		// column.setWidth(75);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// column = new ColumnConfig();
		// column.setId("n1");
		// column.setHeader("n1");
		// column.setWidth(75);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// column = new ColumnConfig();
		// column.setId("n2");
		// column.setHeader("n2");
		// column.setWidth(75);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// for(String method : mainController.getSettings().getScoresTypes())
		// {
		// column = new ColumnConfig();
		// column.setId(method);
		// column.setHeader(method);
		// column.setWidth(75);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		// }
		//
		// column = new ColumnConfig();
		// column.setId("final");
		// column.setHeader("Final");
		// column.setWidth(75);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);
		//
		// column = new ColumnConfig();
		// column.setId("details");
		// column.setHeader("");
		// column.setWidth(50);
		// column.setRenderer(detailsButtonRenderer);
		// column.setAlignment(HorizontalAlignment.CENTER);
		// configs.add(column);

		return configs;
	}

	public void setResults(PDBScoreItem resultsData) {
		fillResultsGrid(resultsData);
	}

	public void fillResultsGrid(PDBScoreItem resultsData) {
		resultsStore.removeAll();

		List<BeanModel> data = new ArrayList<BeanModel>();

		List<InterfaceItem> interfaceItems = resultsData.getInterfaceItems();

		if (interfaceItems != null) {
			for (InterfaceItem interfaceItem : interfaceItems) {

				BeanModelFactory beanModelFactory = BeanModelLookup.get()
						.getFactory(InterfaceItem.class);
				BeanModel model = beanModelFactory.createModel(interfaceItem);

				for (String method : mainController.getSettings()
						.getScoresTypes()) {
					// /TODO
					InterfaceScoreItemKey interfaceScoreItemKey = new InterfaceScoreItemKey();
					interfaceScoreItemKey.setInterfaceId(interfaceItem.getId());
					interfaceScoreItemKey.setMethod(method);

					for (InterfaceScoreItemKey k : mainController
							.getPdbScoreItem().getInterfaceScores().keySet()) {
						if (interfaceScoreItemKey.equals(k)) {
							model.set(method, mainController.getPdbScoreItem()
									.getInterfaceScores().get(k).getCall());
						}
					}

				}
				// Window.alert(String.valueOf(interfaceItem));
				// ResultsModel resultsModel = new ResultsModel("");
				// resultsModel.set("id", interfaceItem.getId());
				// resultsModel.set("interface", interfaceItem.getName());
				// resultsModel.set("area", interfaceItem.getArea());
				// resultsModel.set("size1", interfaceItem.getSize1());
				// resultsModel.set("size2", interfaceItem.getSize2());
				// resultsModel.set("n1", interfaceItem.getNumHomologs1());
				// resultsModel.set("n2", interfaceItem.getNumHomologs2());
				// resultsModel.set("entropy", "Bio");

				data.add(model);
			}
		}

		resultsStore.add(data);
		resultsGrid.reconfigure(resultsStore, resultsColumnModel);
	}

	private void createInfoPanel() {
		infoPanel = new InfoPanel(resultsData);
		this.add(infoPanel, new RowData(1, -1, new Margins(0)));
	}

	private void createScoresPanel() {
		scoresPanel = new ScoresPanel(resultsData, mainController);
		// this.add(scoresPanel, new RowData(1, 0.3, new Margins(0)));
	}
}
