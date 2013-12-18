package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalDoubleCell;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModelProperties;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.DetailsButtonCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.MethodCallCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.OperatorTypeCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.ThumbnailCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.WarningsCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.grids.contextmenus.ResultsPanelContextMenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.tips.QuickTip;

public class ResultsInterfacesGrid extends Grid<InterfaceItemModel>{

	private static InterfaceItemModelProperties props = GWT.create(InterfaceItemModelProperties.class);
	
	private static ListStore<InterfaceItemModel> resultsStore = new ListStore<InterfaceItemModel>(props.key());
	private static ColumnModel<InterfaceItemModel> resultsColumnModel = new ColumnModel<InterfaceItemModel>(createColumnConfig());
	
	private static ColumnConfig<InterfaceItemModel, String> warningsColumn;
	
	private int panelWidth;
	
	public ResultsInterfacesGrid(int panelWidth){
		super(resultsStore, resultsColumnModel, createGridView());
		
		this.panelWidth = panelWidth;
		this.setWidth(panelWidth);
		
		this.setBorders(false);
		this.setContextMenu(new ResultsPanelContextMenu());
		
		this.addStyleName("eppic-results-grid");
		this.addStyleName("eppic-default-font");
		
		new KeyNav(this)
		{
			@Override
            public void onEnter(NativeEvent event) 
			{
				InterfaceItemModel interfaceItemModel = getSelectionModel().getSelectedItem();
				if(interfaceItemModel != null)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
				}
			}
		};
		
		QuickTip gridQT = new QuickTip(this);
		//Bug-Fix in GXt 3.0.1
		//To fix the issue of blank Tooltips we set the delay
		gridQT.setQuickShowInterval(0);
		gridQT.getToolTipConfig().setShowDelay(0);
		
	}
	
	/**
	 * Creates the view for the grid
	 */
	private static GridView<InterfaceItemModel> createGridView(){
		GridView<InterfaceItemModel> view = new GridView<InterfaceItemModel>();
		view.setStripeRows(true);
		view.setColumnLines(false);
		view.setForceFit(true);
		view.setEmptyText(AppPropertiesManager.CONSTANTS.results_grid_empty_text());
		return view;
	}
	
	/**
	 * Creates configurations of the columns for results grid.
	 * @return list of columns configurations for results grid
	 */
	private static List<ColumnConfig<InterfaceItemModel, ?>> createColumnConfig() 
	{
		List<ColumnConfig<InterfaceItemModel, ?>> configs = new ArrayList<ColumnConfig<InterfaceItemModel, ?>>();
		
		configs.add(getThumbnailColumn());
		configs.add(getIdColumn());
		configs.add(getChainsColumn());
		configs.add(getAreaColumn());
		configs.add(getOperatorColumn());
		configs.add(getSizesColumn());
		configs.add(getMethodsColumn(props.geometryCall(),"Geometry"));
		configs.add(getMethodsColumn(props.coreRimCall(),"Entropy"));
		configs.add(getMethodsColumn(props.coreSurfaceCall(),"Z-scores"));
		configs.add(getFinalCallColumn());
		configs.add(getDetailsColumn());
		warningsColumn = getWarningsColumn();
		configs.add(warningsColumn);

		return configs;
	}
	
	/**
	 * Fills in the column with following settings:
	 * width - taken from grid.properties
	 * header - taken from grid.properties
	 * tooltip - taken from grid.properties
	 * styles, alignment
	 * @param column
	 * @param type
	 */
	private static void fillColumnSettings(ColumnConfig<InterfaceItemModel, ?> column, String type){
		column.setWidth(Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_"+type+"_width")));
		column.setHeader(StyleGenerator.defaultFontStyle(
				ApplicationContext.getSettings().getGridProperties().get("results_"+type+"_header")));
		
		String tooltip = ApplicationContext.getSettings().getGridProperties().get("results_"+type+"_tooltip");
		if(tooltip != null)
			column.setToolTip(EscapedStringGenerator.generateSafeHtml(tooltip));
		
		column.setColumnTextClassName("eppic-results-grid-common-cells");
		column.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
	}
	
	private static ColumnConfig<InterfaceItemModel, String> getWarningsColumn() {
		ColumnConfig<InterfaceItemModel, String> column = 
				new ColumnConfig<InterfaceItemModel, String>(props.warningsImagePath());
		column.setCell(new WarningsCell(resultsStore));
		fillColumnSettings(column, "warnings");
		return column;
	}

	private static ColumnConfig<InterfaceItemModel, String> getDetailsColumn() {
		 ColumnConfig<InterfaceItemModel, String> column = 
				new  ColumnConfig<InterfaceItemModel, String>(props.detailsButtonText());
		column.setCell(new DetailsButtonCell());
		fillColumnSettings(column, "details");
		column.setResizable(false);
		column.setSortable(false);
		column.setFixed(true);
		return column;
	}
	
	private static ColumnConfig<InterfaceItemModel, String> getFinalCallColumn() {
		 ColumnConfig<InterfaceItemModel, String> column = 
				new  ColumnConfig<InterfaceItemModel, String>(props.finalCallName());
		column.setCell(new MethodCallCell(resultsStore, "finalCallName"));
		fillColumnSettings(column, "finalCallName");
		column.setColumnTextClassName("eppic-results-final-call");
		return column;
	}
	
	private static ColumnConfig<InterfaceItemModel, ?> getMethodsColumn(
			ValueProvider<InterfaceItemModel, String> vp,
			String type) {
		 ColumnConfig<InterfaceItemModel, String> column = new  ColumnConfig<InterfaceItemModel, String>(vp);
		column.setCell(new MethodCallCell(resultsStore, type));
		fillColumnSettings(column, type);
		return column;
	}

	private static ColumnConfig<InterfaceItemModel, String> getSizesColumn() {
		 ColumnConfig<InterfaceItemModel, String> sizesColumn = 
				new  ColumnConfig<InterfaceItemModel, String>(props.sizes());
		fillColumnSettings(sizesColumn, "sizes");
		return sizesColumn;
	}

	private static ColumnConfig<InterfaceItemModel, String> getOperatorColumn() {
		 ColumnConfig<InterfaceItemModel, String> operatorColumn = 
				new  ColumnConfig<InterfaceItemModel, String>(props.operatorType());
		operatorColumn.setCell(new OperatorTypeCell(resultsStore));
		fillColumnSettings(operatorColumn, "operatorType");
		return operatorColumn;
	}

	private static ColumnConfig<InterfaceItemModel, Double> getAreaColumn() {
		 ColumnConfig<InterfaceItemModel, Double> areaColumn = 
				new  ColumnConfig<InterfaceItemModel, Double>(props.area());
		
		fillColumnSettings(areaColumn, "area");
		areaColumn.setCell(new TwoDecimalDoubleCell());
		
		return areaColumn;
	}

	private static ColumnConfig<InterfaceItemModel, String> getChainsColumn() {
		 ColumnConfig<InterfaceItemModel, String> chainColumn = 
				new  ColumnConfig<InterfaceItemModel, String>(props.name());
		fillColumnSettings(chainColumn, "name");
		return chainColumn;
	}

	private static ColumnConfig<InterfaceItemModel, Integer> getIdColumn() {
		 ColumnConfig<InterfaceItemModel, Integer> idColumn = 
				new  ColumnConfig<InterfaceItemModel, Integer>(props.id());
		fillColumnSettings(idColumn, "id");
		return idColumn;
	}
	
	private static ColumnConfig<InterfaceItemModel, String> getThumbnailColumn(){
		 ColumnConfig<InterfaceItemModel, String> thumbnailColumn = 
				new  ColumnConfig<InterfaceItemModel, String>(props.thumbnailUrl());

		thumbnailColumn.setCell(new ThumbnailCell());
		fillColumnSettings(thumbnailColumn, "thumbnail");
		thumbnailColumn.setResizable(false);

		return thumbnailColumn;
	}
	
	/**
	 * Sets content of results grid.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsGrid(List<InterfaceItemModel> data, boolean hideWarnings)
	{		
		resultsStore.clear();
		
		resultsStore.addAll(data);
		
		boolean resizeGrid = false;
		int warningsColIndex = 0;
		for(ColumnConfig<InterfaceItemModel, ?> col: resultsColumnModel.getColumns()){
			if(col.equals(warningsColumn))
			{
				warningsColIndex = resultsColumnModel.indexOf(col);
			}
		}

		if(resultsColumnModel.getColumn(warningsColIndex).isHidden() != hideWarnings)
		{
			resizeGrid = true;
		}

		resultsColumnModel.getColumn(warningsColIndex).setHidden(hideWarnings);

		this.reconfigure(resultsStore, resultsColumnModel);
		
		if(resizeGrid)
		{
			resizeContent(panelWidth);
		}
	}
	
	/**
	 * Adjusts size of the results grid
	 */
	public void resizeContent(int panelWidth) 
	{	
		this.panelWidth = panelWidth;
		this.setWidth(panelWidth);
		clearSizeCache();
		getView().refresh(true);
	}
}
