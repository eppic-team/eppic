package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.ClustersGridView;
import ch.systemsx.sybit.crkwebui.client.results.gui.grids.contextmenus.ResultsPanelContextMenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.SummaryColumnConfig;
import com.sencha.gxt.widget.core.client.grid.SummaryRenderer;
import com.sencha.gxt.widget.core.client.grid.SummaryType;
import com.sencha.gxt.widget.core.client.tips.QuickTip;

public class ResultsClustersGrid extends Grid<InterfaceItemModel>{

	private static InterfaceItemModelProperties props = GWT.create(InterfaceItemModelProperties.class);
	
	private static ListStore<InterfaceItemModel> resultsStore = new ListStore<InterfaceItemModel>(props.key());
	private static ColumnModel<InterfaceItemModel> resultsColumnModel= new ColumnModel<InterfaceItemModel>(createColumnConfig());
	private static ClustersGridView view = new ClustersGridView();
	
	//Columns to be used later
	private static ColumnConfig<InterfaceItemModel, String> warningsColumn;
	private static SummaryColumnConfig<InterfaceItemModel, Integer> clusterIdColumn;
	
	private int panelWidth;

	public ResultsClustersGrid(int panelWidth){
		super(resultsStore, resultsColumnModel, view);
		this.panelWidth = panelWidth;
		init();
	}
	
	/**
	 * intitializes the grid
	 */
	private void init(){
		this.setWidth(panelWidth);
		
		this.setBorders(false);
		this.setContextMenu(new ResultsPanelContextMenu());
		
		this.getView().setEmptyText(AppPropertiesManager.CONSTANTS.results_grid_empty_text());
		
		//Hide cluster id column
		this.getColumnModel().getColumn(0).setHidden(true);
		
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
		
		Scheduler.get().scheduleFinally(new ScheduledCommand() {			
			@Override
			public void execute() {
				view.groupBy(clusterIdColumn);
				
			}
		});
	}
	
	/**
	 * Creates configurations of the columns for results grid.
	 * @return list of columns configurations for results grid
	 */
	private static List<ColumnConfig<InterfaceItemModel, ?>> createColumnConfig() 
	{
		List<ColumnConfig<InterfaceItemModel, ?>> configs = new ArrayList<ColumnConfig<InterfaceItemModel, ?>>();
		
		clusterIdColumn = new SummaryColumnConfig<InterfaceItemModel, Integer>(props.clusterId());
		configs.add(clusterIdColumn);
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
	
	private static SummaryColumnConfig<InterfaceItemModel, String> getWarningsColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> column = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.warningsImagePath());
		column.setCell(new WarningsCell(resultsStore));
		fillColumnSettings(column, "warnings");
		return column;
	}

	private static SummaryColumnConfig<InterfaceItemModel, String> getDetailsColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> column = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.detailsButtonText());
		column.setCell(new DetailsButtonCell());
		fillColumnSettings(column, "details");
		column.setResizable(false);
		column.setSortable(false);
		column.setFixed(true);
		return column;
	}
	
	private static SummaryColumnConfig<InterfaceItemModel, String> getFinalCallColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> column = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.finalCallName());
		column.setCell(new MethodCallCell(resultsStore, "finalCallName"));
		fillColumnSettings(column, "finalCallName");
		column.setColumnTextClassName("eppic-results-final-call");
		return column;
	}
	
	private static SummaryColumnConfig<InterfaceItemModel, ?> getMethodsColumn(
			ValueProvider<InterfaceItemModel, String> vp,
			String type) {
		SummaryColumnConfig<InterfaceItemModel, String> column = new SummaryColumnConfig<InterfaceItemModel, String>(vp);
		column.setCell(new MethodCallCell(resultsStore, type));
		fillColumnSettings(column, type);
		return column;
	}

	private static SummaryColumnConfig<InterfaceItemModel, String> getSizesColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> sizesColumn = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.sizes());
		fillColumnSettings(sizesColumn, "sizes");
		return sizesColumn;
	}

	private static SummaryColumnConfig<InterfaceItemModel, String> getOperatorColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> operatorColumn = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.operatorType());
		operatorColumn.setCell(new OperatorTypeCell(resultsStore));
		fillColumnSettings(operatorColumn, "operatorType");
		return operatorColumn;
	}

	private static SummaryColumnConfig<InterfaceItemModel, Double> getAreaColumn() {
		SummaryColumnConfig<InterfaceItemModel, Double> areaColumn = 
				new SummaryColumnConfig<InterfaceItemModel, Double>(props.area());
		
		areaColumn.setSummaryType(new SummaryType.AvgSummaryType<Double>());
		areaColumn.setSummaryRenderer(new SummaryRenderer<InterfaceItemModel>() {

			@Override
			public SafeHtml render(
					Number value,
					Map<ValueProvider<? super InterfaceItemModel, ?>, Number> data) {
				return SafeHtmlUtils.fromTrustedString(NumberFormat.getFormat("0.00").format(value));
			}
		});
		
		fillColumnSettings(areaColumn, "area");
		areaColumn.setCell(new TwoDecimalDoubleCell());
		
		return areaColumn;
	}

	private static SummaryColumnConfig<InterfaceItemModel, String> getChainsColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> chainColumn = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.name());
		fillColumnSettings(chainColumn, "name");
		return chainColumn;
	}

	private static SummaryColumnConfig<InterfaceItemModel, Integer> getIdColumn() {
		SummaryColumnConfig<InterfaceItemModel, Integer> idColumn = 
				new SummaryColumnConfig<InterfaceItemModel, Integer>(props.id());
		fillColumnSettings(idColumn, "id");
		return idColumn;
	}
	
	private static SummaryColumnConfig<InterfaceItemModel, String> getThumbnailColumn(){
		SummaryColumnConfig<InterfaceItemModel, String> thumbnailColumn = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.thumbnailUrl());

		thumbnailColumn.setSummaryType(new SummaryType.CountSummaryType<String>());
		thumbnailColumn.setSummaryRenderer(new SummaryRenderer<InterfaceItemModel>() {

			@Override
			public SafeHtml render(
					Number value,
					Map<ValueProvider<? super InterfaceItemModel, ?>, Number> data) {
				return SafeHtmlUtils.fromTrustedString(
						value.intValue() > 1 ? "(" + value.intValue() + " Interfaces)" : "(1 Interface)");
			}
		});
		
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
