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
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Cookies;

/**
 * This panel is used to display the results of the calculations
 * @author srebniak_a
 *
 */
public class ResultsPanel extends DisplayPanel 
{
	private Label pdbIdentifier;
	private Label pdbTitle;
	private ContentPanel resultsGridContainer;
	private List<ColumnConfig> resultsConfigs;
	private ListStore<BeanModel> resultsStore;
	private ColumnModel resultsColumnModel;
	private Grid<BeanModel> resultsGrid;
	private List<Integer> initialColumnWidth;

	private PDBScoreItem resultsData;

	private InfoPanel infoPanel;
	private SimpleComboBox<String> viewerTypeComboBox;
	private ScoresPanel scoresPanel;
	private LayoutContainer scoresPanelLocation;

	public ResultsPanel(MainController mainController,
			final PDBScoreItem resultsData) {
		super(mainController);
		this.resultsData = resultsData;
		this.setBorders(true);
		// this.setBodyBorder(false);
		// this.getHeader().setVisible(false);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		// this.setPadding(10);
		this.setStyleAttribute("padding", "10px");

		pdbIdentifier = new Label(MainController.CONSTANTS.info_panel_pdb_identifier() + ": " + resultsData.getPdbName());
		pdbIdentifier.addStyleName("pdb-identifier-label");
		this.add(pdbIdentifier);
		
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 1.1, new Margins(0)));
		
		pdbTitle = new Label(resultsData.getTitle());
		pdbTitle.addStyleName("crk-default-label");
		this.add(pdbTitle);
		
		breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 10, new Margins(0)));
		
		createInfoPanel();

		createViewerTypePanel();

		breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 5, new Margins(0)));
		
		resultsConfigs = createColumnConfig();

		resultsStore = new ListStore<BeanModel>();

		resultsColumnModel = new ColumnModel(resultsConfigs);

		resultsGrid = new Grid<BeanModel>(resultsStore, resultsColumnModel);
		// resultsGrid.setStyleAttribute("borderTop", "none");

		resultsGrid.getView().setForceFit(false);

		resultsGrid.setBorders(false);
		resultsGrid.setStripeRows(true);
		resultsGrid.setColumnLines(true);

		Listener<GridEvent> resultsGridListener = new Listener<GridEvent>() {
			@Override
			public void handleEvent(GridEvent be) {
				updateScoresPanel((Integer) resultsStore
						.getAt(be.getRowIndex()).get("id"));
			}
		};

		resultsGrid.addListener(Events.CellClick, resultsGridListener);

		resultsGridContainer = new ContentPanel();
		resultsGridContainer.getHeader().setVisible(false);
		resultsGridContainer.setBorders(true);
		resultsGridContainer.setBodyBorder(false);
		resultsGridContainer.setLayout(new FitLayout());
		resultsGridContainer.add(resultsGrid);
		this.add(resultsGridContainer, new RowData(1, 0.55, new Margins(0)));
		
//		createResultsGridContainerToolbar();

		breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 10, new Margins(0)));

		scoresPanelLocation = new LayoutContainer();
		scoresPanelLocation.setLayout(new FitLayout());
		// scoresPanelLocation.setBodyBorder(false);
		scoresPanelLocation.setBorders(false);
		// scoresPanelLocation.getHeader().setVisible(false);
		// scoresPanelLocation.setPadding(0);

		this.add(scoresPanelLocation, new RowData(1, 0.45, new Margins(0)));
	}

//	public int getResultsGridWidthOfAllColumns() {
//		return resultsGridWidthOfAllColumns;
//	}

	public void updateScoresPanel(int selectedInterface) {
		if (scoresPanel == null) 
		{
			createScoresPanel();
			scoresPanelLocation.add(scoresPanel);
			scoresPanelLocation.layout();
		}

		scoresPanel.fillGrid(resultsData, selectedInterface);
		scoresPanel.resizeGrid();
		scoresPanel.setVisible(true);
	}

	private List<ColumnConfig> createColumnConfig() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(
				InterfaceItem.class);
		BeanModel model = beanModelFactory.createModel(new InterfaceItem());

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

		if (columns != null) {
			initialColumnWidth = new ArrayList<Integer>();
		}

		int i = 0;

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

				boolean isResizable = true;

				String customIsResizable = mainController.getSettings()
						.getGridProperties()
						.get("results_" + columnName + "_resizable");
				if (customIsResizable != null) {
					if (!customIsResizable.equals("yes")) {
						isResizable = false;
					}
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
						column.setFixed(!isResizable);
						column.setResizable(isResizable);

						if (renderer != null) {
							column.setRenderer(renderer);
						}

						initialColumnWidth.add(columnWidth);

						configs.add(column);
					}
				} else {
					ColumnConfig column = new ColumnConfig();
					column.setId(columnName);
					column.setHeader(header);
					column.setWidth(columnWidth);
					column.setAlignment(HorizontalAlignment.CENTER);
					column.setHidden(!displayColumn);
					column.setFixed(!isResizable);
					column.setResizable(isResizable);

					if (renderer != null) {
						column.setRenderer(renderer);
					}

					initialColumnWidth.add(columnWidth);

					configs.add(column);
				}
			}

			i++;
		}
		
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
		this.add(infoPanel, new RowData(1, 75, new Margins(0)));
//		this.add(infoPanel, new RowData(1, 105, new Margins(0)));
	}

	private void createViewerTypePanel() {
		LayoutContainer viewerTypePanelLocation = new LayoutContainer();
		// viewerTypePanelLocation.getHeader().setVisible(false);
		viewerTypePanelLocation.setBorders(false);
		// viewerTypePanelLocation.setBodyBorder(false);
		// viewerTypePanelLocation.setPadding(0);
		viewerTypePanelLocation.setStyleAttribute("padding-top", "10px");

		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);

		viewerTypePanelLocation.setLayout(vBoxLayout);

		FormPanel viewerTypePanel = new FormPanel();
		viewerTypePanel.getHeader().setVisible(false);
		viewerTypePanel.setBorders(false);
		viewerTypePanel.setBodyBorder(false);
		viewerTypePanel.setFieldWidth(100);
		viewerTypePanel.setPadding(0);

		viewerTypeComboBox = new SimpleComboBox<String>();
		viewerTypeComboBox.setId("viewercombo");
		viewerTypeComboBox.setTriggerAction(TriggerAction.ALL);
		viewerTypeComboBox.setEditable(false);
		viewerTypeComboBox.setFireChangeEventOnSetValue(true);
		viewerTypeComboBox.setWidth(100);
		viewerTypeComboBox.add("Local");
		viewerTypeComboBox.add("Jmol");

		String viewerCookie = Cookies.getCookie("crkviewer");
		if (viewerCookie != null) {
			viewerTypeComboBox.setSimpleValue(viewerCookie);
		} else {
			viewerTypeComboBox.setSimpleValue("Jmol");
		}

		mainController.setSelectedViewer(viewerTypeComboBox.getValue()
				.getValue());

		viewerTypeComboBox.setFieldLabel(MainController.CONSTANTS.results_grid_viewer_combo_label());
		viewerTypeComboBox.setLabelStyle("crk-default-label");
		viewerTypeComboBox.addListener(Events.Change,
				new Listener<FieldEvent>() {
					public void handleEvent(FieldEvent be) {
						Cookies.setCookie("crkviewer", viewerTypeComboBox
								.getValue().getValue());
						mainController.setSelectedViewer(viewerTypeComboBox
								.getValue().getValue());
					}
				});

		viewerTypePanel.add(viewerTypeComboBox);
		viewerTypePanelLocation.add(viewerTypePanel);
		this.add(viewerTypePanelLocation, new RowData(1, 35, new Margins(0)));
	}

	private void createScoresPanel() {
		scoresPanel = new ScoresPanel(resultsData, mainController);
	}

	public ListStore<BeanModel> getResultsStore() {
		return resultsStore;
	}

	public String getCurrentViewType() {
		return viewerTypeComboBox.getValue().getValue();
	}

	public void fillResultsPanel(PDBScoreItem resultsData) {
		if (scoresPanel != null) {
			scoresPanel.setVisible(false);
		}

		fillResultsGrid(resultsData);

		infoPanel.fillInfoPanel(resultsData);
		
		pdbIdentifier.setText(MainController.CONSTANTS.info_panel_pdb_identifier() + ": " + resultsData.getPdbName());
		pdbTitle.setText(resultsData.getTitle());
	}

	public void resizeGrid() 
	{
		int limit = 25;
		if(mainController.getMainViewPort().getMyJobsPanel().isExpanded())
		{
			limit += mainController.getMainViewPort().getMyJobsPanel().getWidth();
		}
		else
		{
			limit += 25;
		}
		
		int resultsGridWidthOfAllVisibleColumns = 0;
		
		for(int i=0; i<resultsGrid.getColumnModel().getColumnCount(); i++)
		{
			if(!resultsGrid.getColumnModel().getColumn(i).isHidden())
			{
				resultsGridWidthOfAllVisibleColumns += initialColumnWidth.get(i);
			}
		}
		
		if (resultsGridWidthOfAllVisibleColumns < mainController.getWindowWidth() - limit) 
		{
			resultsGrid.getView().setForceFit(true);
		}
		else 
		{
			resultsGrid.getView().setForceFit(false);

			int nrOfColumn = resultsGrid.getColumnModel().getColumnCount();

			for (int i = 0; i < nrOfColumn; i++) {
				resultsGrid.getColumnModel().getColumn(i)
						.setWidth(initialColumnWidth.get(i));
			}
		}

		resultsGrid.getView().refresh(true);
		resultsGrid.getView().layout();
		resultsGrid.recalculate();
		resultsGrid.repaint();

		this.layout();
	}

	public InfoPanel getInfoPanel() {
		return infoPanel;
	}
	
	public ScoresPanel getScoresPanel()
	{
		return scoresPanel;
	}

	public void resizeScoresGrid() 
	{
		scoresPanel.resizeGrid();
	}
	
//	private void createResultsGridContainerToolbar()
//	{
//		ToolBar resultsGridContainerToolbar = new ToolBar();
//		
//		viewerTypeComboBox = new SimpleComboBox<String>();
//		viewerTypeComboBox.setId("viewercombo");
//		viewerTypeComboBox.setTriggerAction(TriggerAction.ALL);
//		viewerTypeComboBox.setEditable(false);
//		viewerTypeComboBox.setFireChangeEventOnSetValue(true);
//		viewerTypeComboBox.setWidth(100);
//		viewerTypeComboBox.add("Local");
//		viewerTypeComboBox.add("Jmol");
//
//		String viewerCookie = Cookies.getCookie("crkviewer");
//		if (viewerCookie != null) {
//			viewerTypeComboBox.setSimpleValue(viewerCookie);
//		} else {
//			viewerTypeComboBox.setSimpleValue("Jmol");
//		}
//
//		mainController.setSelectedViewer(viewerTypeComboBox.getValue()
//				.getValue());
//
//		viewerTypeComboBox.setFieldLabel("View mode");
//		viewerTypeComboBox.addListener(Events.Change,
//				new Listener<FieldEvent>() {
//					public void handleEvent(FieldEvent be) {
//						Cookies.setCookie("crkviewer", viewerTypeComboBox
//								.getValue().getValue());
//						mainController.setSelectedViewer(viewerTypeComboBox
//								.getValue().getValue());
//					}
//				});
//
//		resultsGridContainerToolbar.add(new FillToolItem());
//		resultsGridContainerToolbar.add(new LabelToolItem("3D Viewer: "));  
//		resultsGridContainerToolbar.add(viewerTypeComboBox);
//		resultsGridContainer.setTopComponent(resultsGridContainerToolbar);
//	}
	
	public Grid<BeanModel> getResultsGrid()
	{
		return resultsGrid;
	}
}
