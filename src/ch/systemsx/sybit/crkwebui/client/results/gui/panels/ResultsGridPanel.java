package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowThumbnailEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerSelectorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UncheckClustersRadioEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalDoubleCell;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDetailsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowThumbnailHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.UncheckClustersRadioHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.WindowHideHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.managers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModelProperties;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.DetailsButtonCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.MethodCallCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.OperatorTypeCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.ThumbnailCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.WarningsCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.ClustersGridView;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.SummaryColumnConfig;
import com.sencha.gxt.widget.core.client.grid.SummaryRenderer;
import com.sencha.gxt.widget.core.client.grid.SummaryType;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;

public class ResultsGridPanel extends VerticalLayoutContainer
{
	private VerticalLayoutContainer panelContainer;
	
	private ListStore<InterfaceItemModel> resultsStore;
	private ColumnModel<InterfaceItemModel> resultsColumnModel;
	private Grid<InterfaceItemModel> resultsGrid;
	private ClustersGridView clustersView;
	
	private CheckBox clustersViewButton;
	
	private VerticalLayoutContainer noInterfaceFoundPanel;
	
	private int panelWidth;
	
	private static final InterfaceItemModelProperties props = GWT.create(InterfaceItemModelProperties.class);
	
	//Columns to be used later
	ColumnConfig<InterfaceItemModel, String> thumbnailColumn;
	ColumnConfig<InterfaceItemModel, String> warningsColumn;
	SummaryColumnConfig<InterfaceItemModel, Integer> clusterIdColumn;
	
	public ResultsGridPanel(int width)
	{
		
		this.panelWidth = width;
		this.setWidth(panelWidth);
		
		FramedPanel gridPanel = new FramedPanel();
		
		gridPanel.getHeader().setVisible(false);
		gridPanel.setBorders(true);
		gridPanel.setBodyBorder(false);
		
		panelContainer = new VerticalLayoutContainer();
		panelContainer.setScrollMode(ScrollMode.AUTO);
		gridPanel.setWidget(panelContainer);
		
		resultsStore = new ListStore<InterfaceItemModel>(props.key());
		List<ColumnConfig<InterfaceItemModel, ?>> resultsConfigs = createColumnConfig();
		resultsColumnModel = new ColumnModel<InterfaceItemModel>(resultsConfigs);
		clustersView = createClusterView();
		
		resultsGrid = createResultsGrid();		
		panelContainer.add(createSelectorToolBar(), new VerticalLayoutData(1,-1));
		panelContainer.add(resultsGrid, new VerticalLayoutData(-1,-1));
		
		this.add(panelContainer, new VerticalLayoutData(1,-1));
		
		//Panel to display if no interfaces found
		noInterfaceFoundPanel = new VerticalLayoutContainer();
		
		initializeEventsListeners();
	}
	
	private ToolBar createSelectorToolBar(){
		ToolBar toolBar = new ToolBar();
		
		clustersViewButton = new CheckBox();
		clustersViewButton.setHTML(AppPropertiesManager.CONSTANTS.results_grid_clusters_label());
		new ToolTip(clustersViewButton, new ToolTipConfig(AppPropertiesManager.CONSTANTS.results_grid_clusters_tooltip()));
		clustersViewButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				onClustersRadioValueChange(event.getValue());
				
			}
		});
		clustersViewButton.setValue(false);
		toolBar.add(clustersViewButton);
		
		TextButton changeViewerButton = new TextButton(
				AppPropertiesManager.CONSTANTS.results_grid_selector_button());
		changeViewerButton.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerSelectorEvent());
				
			}
		});
		
		toolBar.add(new FillToolItem());
		toolBar.add(changeViewerButton);
		
		return toolBar;
	}
	
	/**
	 * Creates configurations of the columns for results grid.
	 * @return list of columns configurations for results grid
	 */
	private List<ColumnConfig<InterfaceItemModel, ?>> createColumnConfig() 
	{
		List<ColumnConfig<InterfaceItemModel, ?>> configs = new ArrayList<ColumnConfig<InterfaceItemModel, ?>>();
		
		clusterIdColumn = new SummaryColumnConfig<InterfaceItemModel, Integer>(props.clusterId());
		configs.add(clusterIdColumn);
		thumbnailColumn = getThumbnailColumn();
		configs.add(thumbnailColumn);
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
	private void fillColumnSettings(ColumnConfig<InterfaceItemModel, ?> column, String type){
		column.setColumnHeaderClassName("eppic-default-font");
		column.setWidth(Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_"+type+"_width")));
		column.setHeader(EscapedStringGenerator.generateSafeHtml(
				ApplicationContext.getSettings().getGridProperties().get("results_"+type+"_header")));
		
		String tooltip = ApplicationContext.getSettings().getGridProperties().get("results_"+type+"_tooltip");
		if(tooltip != null)
			column.setToolTip(EscapedStringGenerator.generateSafeHtml(tooltip));
		
		column.setColumnTextClassName("eppic-results-grid-common-cells");
		column.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
	}
	
	private SummaryColumnConfig<InterfaceItemModel, String> getWarningsColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> column = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.warningsImagePath());
		column.setCell(new WarningsCell(resultsStore));
		fillColumnSettings(column, "warnings");
		return column;
	}

	private SummaryColumnConfig<InterfaceItemModel, String> getDetailsColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> column = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.detailsButtonText());
		column.setCell(new DetailsButtonCell());
		fillColumnSettings(column, "details");
		column.setResizable(false);
		column.setSortable(false);
		return column;
	}
	
	private SummaryColumnConfig<InterfaceItemModel, String> getFinalCallColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> column = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.finalCallName());
		column.setCell(new MethodCallCell(resultsStore, "finalCallName"));
		fillColumnSettings(column, "finalCallName");
		column.setColumnTextClassName("eppic-results-final-call");
		return column;
	}
	
	private SummaryColumnConfig<InterfaceItemModel, ?> getMethodsColumn(
			ValueProvider<InterfaceItemModel, String> vp,
			String type) {
		SummaryColumnConfig<InterfaceItemModel, String> column = new SummaryColumnConfig<InterfaceItemModel, String>(vp);
		column.setCell(new MethodCallCell(resultsStore, type));
		fillColumnSettings(column, type);
		return column;
	}

	private SummaryColumnConfig<InterfaceItemModel, String> getSizesColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> sizesColumn = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.sizes());
		fillColumnSettings(sizesColumn, "sizes");
		return sizesColumn;
	}

	private SummaryColumnConfig<InterfaceItemModel, String> getOperatorColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> operatorColumn = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.operatorType());
		operatorColumn.setCell(new OperatorTypeCell(resultsStore));
		fillColumnSettings(operatorColumn, "operatorType");
		return operatorColumn;
	}

	private SummaryColumnConfig<InterfaceItemModel, Double> getAreaColumn() {
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

	private SummaryColumnConfig<InterfaceItemModel, String> getChainsColumn() {
		SummaryColumnConfig<InterfaceItemModel, String> chainColumn = 
				new SummaryColumnConfig<InterfaceItemModel, String>(props.name());
		fillColumnSettings(chainColumn, "name");
		return chainColumn;
	}

	private SummaryColumnConfig<InterfaceItemModel, Integer> getIdColumn() {
		SummaryColumnConfig<InterfaceItemModel, Integer> idColumn = 
				new SummaryColumnConfig<InterfaceItemModel, Integer>(props.id());
		fillColumnSettings(idColumn, "id");
		return idColumn;
	}
	
	private SummaryColumnConfig<InterfaceItemModel, String> getThumbnailColumn(){
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
	 * Creates the cluster view for the grid
	 * @return view
	 */
	private ClustersGridView createClusterView()
	{
		ClustersGridView summary = new ClustersGridView();
		summary.setShowGroupedColumn(false);
		summary.setShowDirtyCells(false);
		summary.setStartCollapsed(true);
		summary.setEnableGroupingMenu(true);
		summary.setEnableNoGroups(true);
		return summary;
	}
	
	/**
	 * Creates grid storing results of calculations for each of the interfaces.
	 * @return interfaces grid
	 */
	private Grid<InterfaceItemModel> createResultsGrid()
	{
		final Grid<InterfaceItemModel> resultsGrid = new Grid<InterfaceItemModel>(resultsStore, resultsColumnModel);
		resultsGrid.setBorders(false);
		resultsGrid.setView(clustersView);
		resultsGrid.getView().setStripeRows(true);
		resultsGrid.getView().setColumnLines(false);
		resultsGrid.getView().setForceFit(true);
		//resultsGrid.setContextMenu(new ResultsPanelContextMenu());
		
		resultsGrid.getView().setEmptyText(AppPropertiesManager.CONSTANTS.no_interfaces_found_text());
		
		//Hide cluster id column
		resultsGrid.getColumnModel().getColumn(0).setHidden(true);
		
		resultsGrid.addStyleName("eppic-results-grid");
		resultsGrid.addStyleName("eppic-default-font");
		
		new KeyNav(resultsGrid)
		{
			@Override
            public void onEnter(NativeEvent event) 
			{
				InterfaceItemModel interfaceItemModel = resultsGrid.getSelectionModel().getSelectedItem();
				if(interfaceItemModel != null)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
				}
			}
		};
		
		QuickTip gridQT = new QuickTip(resultsGrid);
		//Bug-Fix in GXt 3.0.1
		//To fix the issue of blank Tooltips we set the delay
		gridQT.setQuickShowInterval(0);
		gridQT.getToolTipConfig().setShowDelay(0);
		return resultsGrid;
	}
	
	/**
	 * Sets content of results grid.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsGrid(PDBScoreItem resultsData)
	{
		boolean hideWarnings = true;
		
		resultsStore.clear();

		List<InterfaceItemModel> data = new ArrayList<InterfaceItemModel>();

		List<InterfaceItem> interfaceItems = resultsData.getInterfaceItems();

		if (interfaceItems != null)
		{
			for (InterfaceItem interfaceItem : interfaceItems) 
			{
				if((interfaceItem.getWarnings() != null) &&
				   (interfaceItem.getWarnings().size() > 0))
			    {
					hideWarnings = false;
			    }
				
				InterfaceItemModel model = new InterfaceItemModel();

				for(InterfaceScoreItem interfaceScoreItem : interfaceItem.getInterfaceScores())
				{
					if(interfaceScoreItem.getMethod().equals("Geometry"))
					{
						model.setGeometryCall(interfaceScoreItem.getCallName());
					}

					if(interfaceScoreItem.getMethod().equals("Entropy"))
					{
						model.setCoreRimCall(interfaceScoreItem.getCallName());
					}

					if(interfaceScoreItem.getMethod().equals("Z-scores"))
					{
						model.setCoreSurfaceCall(interfaceScoreItem.getCallName());
					}
				}
				
				model.setId(interfaceItem.getId());
				model.setClusterId(interfaceItem.getClusterId());
				model.setName(interfaceItem.getChain1() + "+" + interfaceItem.getChain2());
				model.setArea(interfaceItem.getArea());
				model.setSizes(String.valueOf(interfaceItem.getSize1()) + " + " + String.valueOf(interfaceItem.getSize2()));
				model.setFinalCallName(interfaceItem.getFinalCallName());
				model.setOperator(interfaceItem.getOperator());
				model.setOperatorType(interfaceItem.getOperatorType());
				model.setIsInfinite(interfaceItem.getIsInfinite());
				model.setWarnings(interfaceItem.getWarnings());
				String thumbnailUrl = ApplicationContext.getSettings().getResultsLocation() +
						ApplicationContext.getPdbScoreItem().getJobId() + 
						"/" + ApplicationContext.getPdbScoreItem().getPdbName() +
						"." + interfaceItem.getId() + ".75x75.png";
				model.setThumbnailUrl(thumbnailUrl);

				data.add(model);
			}
		}
		
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

		resultsGrid.reconfigure(resultsStore, resultsColumnModel);
		
		if(resizeGrid)
		{
			resizeContent(panelWidth);
		}
		
		if(resultsStore.getAll().isEmpty()){
			this.remove(panelContainer);
			this.remove(noInterfaceFoundPanel);
			noInterfaceFoundPanel = new VerticalLayoutContainer();
			HTML noInterfaceFoundLabel = new HTML(AppPropertiesManager.CONSTANTS.no_interfaces_found_text());
			noInterfaceFoundLabel.addStyleName("eppic-results-no-interfaces-found");
			noInterfaceFoundPanel.add(noInterfaceFoundLabel);
			
			if(resultsData.getInputType() == InputType.FILE.getIndex()){
				HTML noInterfaceFoundHint = new HTML(AppPropertiesManager.CONSTANTS.no_interfaces_found_hint());
				noInterfaceFoundHint.addStyleName("eppic-results-no-interfaces-found-hint");
				noInterfaceFoundPanel.add(noInterfaceFoundHint);
			}
			this.add(noInterfaceFoundPanel, new VerticalLayoutData(1,-1));
		}else{
			this.remove(panelContainer);
			this.remove(noInterfaceFoundPanel);
			this.add(panelContainer, new VerticalLayoutData(1,-1));
		}
	}
	
	/**
	 * 
	 */
	private void onClustersRadioValueChange(boolean value){
		if (value) {
			clustersView.groupBy(clusterIdColumn);
		} else{
			clustersView.groupBy(null);
			resultsStore.addSortInfo(0, new StoreSortInfo<InterfaceItemModel>(props.id(), SortDir.ASC));
			//Hide cluster id column
			resultsGrid.getColumnModel().getColumn(0).setHidden(true);
			resultsGrid.getView().refresh(true);
		}
	}

	/**
	 * sets the value of the cluster similar interfaces radio
	 * @param value
	 */
	public void setClustersRadioValue(boolean value){
		clustersViewButton.setValue(value);
		onClustersRadioValueChange(value);
	}
	
	/**
	 * Adjusts size of the results grid based on the current screen size and
	 * initial settings for the columns.
	 */
	public void resizeContent(int width) 
	{
		this.panelWidth = width;
		this.setWidth(panelWidth);		
		resultsGrid.clearSizeCache();
		resultsGrid.getView().refresh(true);
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(WindowHideEvent.TYPE, new WindowHideHandler() 
		{
			@Override
			public void onWindowHide(WindowHideEvent event) 
			{
				if(resultsGrid.isVisible())
				{
					//resultsGrid.focus();
				}
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(UncheckClustersRadioEvent.TYPE, new UncheckClustersRadioHandler() {
			
			@Override
			public void onUncheckClustersRadio(UncheckClustersRadioEvent event) {
				setClustersRadioValue(false);
				
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowViewerEvent.TYPE, new ShowViewerHandler() 
		{
			@Override
			public void onShowViewer(ShowViewerEvent event) 
			{
				ViewerRunner.runViewer(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().getId()));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowThumbnailEvent.TYPE, new ShowThumbnailHandler() {
			
			@Override
			public void onShowThumbnail(ShowThumbnailEvent event)
			{
				for(ColumnConfig<InterfaceItemModel, ?> column : resultsGrid.getColumnModel().getColumns())
				{
					if(column.equals(thumbnailColumn))
					{
						column.setHidden(event.isHideThumbnail());
						resizeContent(panelWidth);
					}
				}
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowDetailsEvent.TYPE, new ShowDetailsHandler() 
		{
			@Override
			public void onShowDetails(ShowDetailsEvent event) 
			{
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfaceResiduesEvent((Integer)resultsGrid.getSelectionModel().getSelectedItem().getId()));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(SelectResultsRowEvent.TYPE, new SelectResultsRowHandler() {
			
			@Override
			public void onSelectResultsRow(SelectResultsRowEvent event) {
				resultsGrid.getSelectionModel().select(event.getRowIndex(), false);
			}
		}); 
	}
	
}
