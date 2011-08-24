package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;
import ch.systemsx.sybit.crkwebui.client.model.InterfaceResidueSummaryModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.AggregationRowConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * This panel is used to display the residues for one structure
 * @author srebniak_a
 *
 */
public class ResiduesSummaryPanel extends ContentPanel
{
	private List<ColumnConfig> residuesConfigs;
	private ListStore<InterfaceResidueSummaryModel> residuesStore;
	private ColumnModel residuesColumnModel;
	private Grid<InterfaceResidueSummaryModel> residuesGrid;
	private List<Integer> initialColumnWidth;
	
	private MainController mainController;
	
	private int structure;
	
	public ResiduesSummaryPanel(
						 String header, 
						 final MainController mainController,
						 int width,
						 int structure) 
	{
		this.mainController = mainController;
		this.structure = structure;
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.getHeader().setVisible(false);
		this.setScrollMode(Scroll.AUTO);

		residuesConfigs = createColumnConfig();

		residuesStore = new ListStore<InterfaceResidueSummaryModel>();
		residuesColumnModel = new ColumnModel(residuesConfigs);
		
		residuesGrid = new Grid<InterfaceResidueSummaryModel>(residuesStore, residuesColumnModel);
		residuesGrid.setBorders(false);
		residuesGrid.setStripeRows(true);
		residuesGrid.setColumnLines(false);
		residuesGrid.setHideHeaders(true);
		
		residuesGrid.disableTextSelection(false);
		residuesGrid.getView().setForceFit(true);
		this.add(residuesGrid);
	}
	
	private List<ColumnConfig> createColumnConfig() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		InterfaceResidueSummaryModel model = new InterfaceResidueSummaryModel();

		String columnOrder = mainController.getSettings().getGridProperties()
				.get("residues_summary_columns");

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
					.get("residues_summary_" + columnName + "_add");
			if (customAdd != null) {
				if (!customAdd.equals("yes")) {
					addColumn = false;
				}
			}

			if (addColumn) {
				boolean displayColumn = true;

				String customVisibility = mainController.getSettings()
						.getGridProperties()
						.get("residues_summary_" + columnName + "_visible");
				if (customVisibility != null) {
					if (!customVisibility.equals("yes")) {
						displayColumn = false;
					}
				}

				int columnWidth = 75;
				String customColumnWidth = mainController.getSettings()
						.getGridProperties()
						.get("residues_summary_" + columnName + "_width");
				if (customColumnWidth != null) {
					columnWidth = Integer.parseInt(customColumnWidth);
				}
				
				String customRenderer = mainController.getSettings()
						.getGridProperties()
						.get("residues_summary_" + columnName + "_renderer");

				GridCellRenderer<BaseModel> renderer = null;
				if ((customRenderer != null) && (!customRenderer.equals(""))) {
					renderer = GridCellRendererFactory.createGridCellRenderer(
							customRenderer, mainController);
				}

				String header = columnName;
				String customHeader = mainController.getSettings()
						.getGridProperties()
						.get("residues_summary_" + columnName + "_header");
				if (customHeader != null) {
					header = customHeader;
				}
				
				String tootlip = mainController.getSettings()
						.getGridProperties()
						.get("residues_summary_" + columnName + "_tooltip");

				if (columnName.equals("METHODS")) {
					for (String method : mainController.getSettings()
							.getScoresTypes()) {
						ColumnConfig column = new ColumnConfig();
						column.setId(method);
						column.setHeader(method);
						column.setWidth(columnWidth);
						initialColumnWidth.add(columnWidth);
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
				} else {
					ColumnConfig column = new ColumnConfig();
					column.setId(columnName);
					column.setHeader(header);
					column.setWidth(columnWidth);
					initialColumnWidth.add(columnWidth);
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
		}

		return configs;

	}

	public void fillResiduesGrid() 
	{
		residuesStore.removeAll();
		
		List<InterfaceResidueSummaryModel> interfaceSummaryItems = new ArrayList<InterfaceResidueSummaryModel>();

		NumberFormat number = NumberFormat.getFormat("0.00");
		
		Map<String, String> coreMethodValues = new HashMap<String, String>();
		Map<String, String> rimMethodValues = new HashMap<String, String>();
		Map<String, String> ratioMethodValues = new HashMap<String, String>();
		
		for (InterfaceScoreItem scoreItem : mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getInterfaceScores()) 
		{
			String coreValue = "";
			String rimValue = "";
			String ratioValue = "";
			
			if(structure == 1)
			{
				coreValue = number.format(scoreItem.getUnweightedCore1Scores()) + 
							" (" + 
							number.format(scoreItem.getWeightedCore1Scores()) + 
							")";
				
				rimValue = number.format(scoreItem.getUnweightedRim1Scores()) + 
						   " (" + 
						   number.format(scoreItem.getWeightedRim1Scores()) + 
						   ")";
				
				ratioValue = number.format(scoreItem.getUnweightedRatio1Scores()) + 
							 " (" + 
							 number.format(scoreItem.getWeightedRatio1Scores()) + 
							 ")";
				
			}
			else
			{
				coreValue = number.format(scoreItem.getUnweightedCore2Scores()) + 
							" (" + 
							number.format(scoreItem.getWeightedCore2Scores()) + 
							")";
				
				rimValue = number.format(scoreItem.getUnweightedRim2Scores()) + 
						   " (" + 
						   number.format(scoreItem.getWeightedRim2Scores()) + 
						   ")";
				
				ratioValue = number.format(scoreItem.getUnweightedRatio2Scores()) + 
							 " (" + 
							 number.format(scoreItem.getWeightedRatio2Scores()) + 
							 ")";
			}
			
			coreMethodValues.put(scoreItem.getMethod(), coreValue);
			rimMethodValues.put(scoreItem.getMethod(), rimValue);
			ratioMethodValues.put(scoreItem.getMethod(), ratioValue);
		}
	
		InterfaceResidueSummaryModel model = new InterfaceResidueSummaryModel();
		model.setTitle(MainController.CONSTANTS.interfaces_residues_aggergation_total_cores());
		
		double asa = 0;
		double bsa = 0;
		
		if(structure == 1)
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaC1();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaC1();
		}
		else
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaC2();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaC2();
		}
		
		model.setAsa(asa);
		model.setBsa(bsa);
		
		for (String method : mainController.getSettings().getScoresTypes()) 
		{
			model.set(method, coreMethodValues.get(method)); 
		}
		
		interfaceSummaryItems.add(model);
		
		
		model = new InterfaceResidueSummaryModel();
		model.setTitle(MainController.CONSTANTS.interfaces_residues_aggergation_total_rims());
		
		asa = 0;
		bsa = 0;
		
		if(structure == 1)
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaR1();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaR1();
		}
		else
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaR2();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaR2();
		}
		
		model.setAsa(asa);
		model.setBsa(bsa);
		
		for (String method : mainController.getSettings().getScoresTypes()) 
		{
			model.set(method, coreMethodValues.get(method)); 
		}
		
		interfaceSummaryItems.add(model);
		
		
		model = new InterfaceResidueSummaryModel();
		model.setTitle(MainController.CONSTANTS.interfaces_residues_aggergation_ratios());
		
		for (String method : mainController.getSettings().getScoresTypes()) 
		{
			model.set(method, coreMethodValues.get(method)); 
		}
		
		interfaceSummaryItems.add(model);
		
		residuesStore.add(interfaceSummaryItems);
	}
	
	public void cleanResiduesGrid()
	{
		residuesStore.removeAll();
	}
	
	public void resizeGrid() 
	{
		int scoresGridWidthOfAllVisibleColumns = calculateWidthOfVisibleColumns();
		
		if (checkIfForceFit(scoresGridWidthOfAllVisibleColumns, 
							(int)((mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().getWidth() - 20) * 0.48))) 
		{
			this.setScrollMode(Scroll.NONE);
//			residuesGrid.setAutoHeight(true);
		} 
		else 
		{
			this.setScrollMode(Scroll.AUTOX);
			residuesGrid.setWidth(scoresGridWidthOfAllVisibleColumns);
//			residuesGrid.setAutoHeight(true);
			
			int nrOfColumn = residuesGrid.getColumnModel().getColumnCount();

			for (int i = 0; i < nrOfColumn; i++) {
				residuesGrid.getColumnModel().getColumn(i)
						.setWidth(initialColumnWidth.get(i));
			}
		}
		
		this.layout();
	}
	
	private boolean checkIfForceFit(int scoresGridWidthOfAllVisibleColumns,
									int width)
	{
		if(scoresGridWidthOfAllVisibleColumns < width)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private int calculateWidthOfVisibleColumns()
	{
		int scoresGridWidthOfAllVisibleColumns = 0;
		
		for(int i=0; i<residuesGrid.getColumnModel().getColumnCount(); i++)
		{
			if(!residuesGrid.getColumnModel().getColumn(i).isHidden())
			{
				scoresGridWidthOfAllVisibleColumns += initialColumnWidth.get(i);
			}
		}
		
		return scoresGridWidthOfAllVisibleColumns;
	}
	
	public Grid<InterfaceResidueSummaryModel> getResiduesGrid() 
	{
		return residuesGrid;
	}
}
