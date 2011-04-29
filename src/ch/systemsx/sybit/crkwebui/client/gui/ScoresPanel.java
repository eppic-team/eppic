package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.InterfaceScoreItem;
import model.InterfaceScoreItemKey;
import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * This panel is used to display the detailed information about calculated scores
 * @author srebniak_a
 *
 */
public class ScoresPanel extends LayoutContainer 
{
	private MainController mainController;

	private Grid<BeanModel> scoresGrid;
	private List<ColumnConfig> scoresConfigs;
	private GroupingStore<BeanModel> scoresStore;
	private ColumnModel scoresColumnModel;
	private List<Integer> initialColumnWidth;

	public ScoresPanel(MainController mainController) 
	{
		this.mainController = mainController;
		this.setLayout(new RowLayout(Orientation.VERTICAL));

		scoresConfigs = createColumnConfig();

		scoresStore = new GroupingStore<BeanModel>();
		scoresStore.groupBy("method");

		scoresColumnModel = new ColumnModel(scoresConfigs);

		scoresColumnModel.addHeaderGroup(0, 0, new HeaderGroupConfig(
				MainController.CONSTANTS.scores_panel_column_weighted(), 1, 7));

		scoresColumnModel.addHeaderGroup(1, 0, new HeaderGroupConfig(
				MainController.CONSTANTS.scores_panel_column_structure1(), 1, 3));
		scoresColumnModel.addHeaderGroup(1, 3, new HeaderGroupConfig(
				MainController.CONSTANTS.scores_panel_column_structure2(), 1, 3));

		scoresColumnModel.addHeaderGroup(0, 7, new HeaderGroupConfig(
				MainController.CONSTANTS.scores_panel_column_unweighted(), 1, 7));

		scoresColumnModel.addHeaderGroup(1, 7, new HeaderGroupConfig(
				MainController.CONSTANTS.scores_panel_column_structure1(), 1, 3));
		scoresColumnModel.addHeaderGroup(1, 10, new HeaderGroupConfig(
				MainController.CONSTANTS.scores_panel_column_structure2(), 1, 3));

		scoresGrid = new Grid<BeanModel>(scoresStore, scoresColumnModel);
		// scoresGrid.getView().setForceFit(true);
		scoresGrid.setBorders(true);
		scoresGrid.setStripeRows(true);
		scoresGrid.setColumnLines(true);
		scoresGrid.disableTextSelection(false);

		GroupingView view = new GroupingView();
		view.setShowGroupedColumn(false);
		view.setForceFit(false);
		view.setGroupRenderer(new GridGroupRenderer() {
			public String render(GroupColumnData data) {
				String f = scoresColumnModel.getColumnById(data.field)
						.getHeader();
				String l = data.models.size() == 1 ? "Item" : "Items";
				return f + ": " + data.group + " (" + data.models.size() + " "
						+ l + ")";
			}
		});

		scoresGrid.setView(view);

		this.add(scoresGrid, new RowData(1, 1, new Margins(0)));

	}

	public void fillGrid(PDBScoreItem resultsData, int selectedInterface) 
	{
		scoresStore.removeAll();

		List<BeanModel> data = new ArrayList<BeanModel>();

		//TODO
		for (InterfaceScoreItemKey key : resultsData.getInterfaceScores()
				.keySet()) {
			if (key.getInterfaceId() == selectedInterface) {
				
				InterfaceScoreItem interfaceScoreItem = resultsData
						.getInterfaceScores().get(key);

				if (interfaceScoreItem != null)
				{
					BeanModelFactory beanModelFactory = BeanModelLookup.get()
							.getFactory(InterfaceScoreItem.class);
					BeanModel scoresModel = beanModelFactory
							.createModel(interfaceScoreItem);
					
					// ScoresModel scoresModel = new ScoresModel("");
					// scoresModel.set("unweightedrim1",
					// interfaceScoreItem.getUnweightedRim1Scores());
					// scoresModel.set("weightedrim1",
					// interfaceScoreItem.getWeightedRim1Scores());
					// scoresModel.set("unweightedcore1",
					// interfaceScoreItem.getUnweightedCore1Scores());
					// scoresModel.set("weightedcore1",
					// interfaceScoreItem.getWeightedCore1Scores());
					// scoresModel.set("unweightedrim2",
					// interfaceScoreItem.getUnweightedRim2Scores());
					// scoresModel.set("weightedrim2",
					// interfaceScoreItem.getWeightedRim2Scores());
					// scoresModel.set("unweightedcore2",
					// interfaceScoreItem.getUnweightedCore2Scores());
					// scoresModel.set("weightedcore2",
					// interfaceScoreItem.getWeightedCore2Scores());
					// scoresModel.set("unweightedscore",
					// interfaceScoreItem.getUnweightedFinalScores());
					// scoresModel.set("weightedscore",
					// interfaceScoreItem.getWeightedFinalScores());
					//
					// scoresModel.set("weightedrat1",
					// interfaceScoreItem.getWeightedRatio1Scores());
					// scoresModel.set("unweightedrat1",
					// interfaceScoreItem.getUnweightedRatio1Scores());
					// scoresModel.set("weightedrat2",
					// interfaceScoreItem.getWeightedRatio2Scores());
					// scoresModel.set("unweightedrat2",
					// interfaceScoreItem.getUnweightedRatio2Scores());
					//
					// scoresModel.set("method",
					// interfaceScoreItem.getMethod());

					data.add(scoresModel);
				}
			}
		}
		
		scoresStore.add(data);
		scoresGrid.reconfigure(scoresStore, scoresColumnModel);
	}

	private List<ColumnConfig> createColumnConfig() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(
				InterfaceScoreItem.class);
		BeanModel model = beanModelFactory
				.createModel(new InterfaceScoreItem());

		String columnOrder = mainController.getSettings().getGridProperties()
				.get("scores_columns");

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

		for (String columnName : columns) {
			boolean addColumn = true;

			String customAdd = mainController.getSettings().getGridProperties()
					.get("scores_" + columnName + "_add");
			if (customAdd != null) {
				if (!customAdd.equals("yes")) {
					addColumn = false;
				}
			}

			if (addColumn) {
				boolean displayColumn = true;

				String customVisibility = mainController.getSettings()
						.getGridProperties()
						.get("scores_" + columnName + "_visible");
				if (customVisibility != null) {
					if (!customVisibility.equals("yes")) {
						displayColumn = false;
					}
				}

				int columnWidth = 75;
				String customColumnWidth = mainController.getSettings()
						.getGridProperties()
						.get("scores_" + columnName + "_width");
				if (customColumnWidth != null) {
					columnWidth = Integer.parseInt(customColumnWidth);
				}
				
				initialColumnWidth.add(columnWidth);

				String customRenderer = mainController.getSettings()
						.getGridProperties()
						.get("scores_" + columnName + "_renderer");

				GridCellRenderer<BeanModel> renderer = null;
				if ((customRenderer != null) && (!customRenderer.equals(""))) {
					renderer = GridCellRendererFactory.createGridCellRenderer(
							customRenderer, mainController);
				}

				String header = columnName;
				String customHeader = mainController.getSettings()
						.getGridProperties()
						.get("scores_" + columnName + "_header");
				if (customHeader != null) {
					header = customHeader;
				}
				
				String tootlip = mainController.getSettings()
						.getGridProperties()
						.get("scores_" + columnName + "_tooltip");

				ColumnConfig column = new ColumnConfig();
				column.setId(columnName);
				column.setHeader(header);
				column.setWidth(columnWidth);
				column.setAlignment(HorizontalAlignment.CENTER);

				column.setHidden(!displayColumn);

				if (renderer != null) {
					column.setRenderer(renderer);
				}
				
				if (tootlip != null) {
					column.setToolTip(tootlip);
				}

				configs.add(column);
			}
		}

		return configs;
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
		
		int scoresGridWidthOfAllVisibleColumns = 0;
		
		for(int i=0; i<scoresGrid.getColumnModel().getColumnCount(); i++)
		{
			if(!scoresGrid.getColumnModel().getColumn(i).isHidden())
			{
				scoresGridWidthOfAllVisibleColumns += initialColumnWidth.get(i);
			}
		}
		
		if (scoresGridWidthOfAllVisibleColumns < mainController.getWindowWidth() - limit) 
		{
			scoresGrid.getView().setForceFit(true);
		} 
		else 
		{
			scoresGrid.getView().setForceFit(false);

			int nrOfColumn = scoresGrid.getColumnModel().getColumnCount();

			for (int i = 0; i < nrOfColumn; i++) {
				scoresGrid.getColumnModel().getColumn(i)
						.setWidth(initialColumnWidth.get(i));
			}
		}

		scoresGrid.getView().refresh(true);
		scoresGrid.getView().layout();
		scoresGrid.repaint();

		this.layout();
	}
}
