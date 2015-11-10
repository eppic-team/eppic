package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowThumbnailEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
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
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.FinalCallSummaryRenderer;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.MethodSummaryType;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.MethodsSummaryRenderer;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceClusterScore;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScore;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import ch.systemsx.sybit.shared.model.InputType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.SummaryColumnConfig;
import com.sencha.gxt.widget.core.client.grid.SummaryRenderer;
import com.sencha.gxt.widget.core.client.grid.SummaryType;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import eppic.EppicParams;
import eppic.model.ScoringMethod;

public class ResultsGridPanel extends VerticalLayoutContainer
{
	private VerticalLayoutContainer panelContainer;
	
	private ListStore<InterfaceItemModel> resultsStore;
	private ColumnModel<InterfaceItemModel> resultsColumnModel;
	private Grid<InterfaceItemModel> resultsGrid; 
	public static ClustersGridView clustersView; //temp static
	
	public static CheckBox clustersViewButton;
	
	private VerticalLayoutContainer noInterfaceFoundPanel;
	
	private int panelWidth;
	
	public static ToolBar toolBar;
	public static HTML toolbar_link;
	
	private static final InterfaceItemModelProperties props = GWT.create(InterfaceItemModelProperties.class);
	
	//Columns to be used later
	ColumnConfig<InterfaceItemModel, String> thumbnailColumn;
	ColumnConfig<InterfaceItemModel, String> warningsColumn;
	public static SummaryColumnConfig<InterfaceItemModel, Integer> clusterIdColumn;
	
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
		toolBar = new ToolBar();
		
		ComboBox<String> viewerSelectorBox = createViewerTypeCombobox();
		viewerSelectorBox.setStyleName("eppic-default-label");
		toolBar.add(new HTML(AppPropertiesManager.CONSTANTS.results_grid_viewer_combo_label()+":&nbsp;"));
		toolBar.add(viewerSelectorBox);
		toolBar.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
		
		toolbar_link = new HTML("<a href='/#id/" + GWT.getHostPageBaseURL() + "'>View All Assemblies</a>");
		toolBar.add(toolbar_link);
		toolBar.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));		
		
		
		clustersViewButton = new CheckBox();
		clustersViewButton.setHTML(AppPropertiesManager.CONSTANTS.results_grid_clusters_label());
		new ToolTip(clustersViewButton, new ToolTipConfig(AppPropertiesManager.CONSTANTS.results_grid_clusters_tooltip()));
		//clustersViewButton.setValue(true);
		clustersViewButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				onClustersRadioValueChange(event.getValue());
				
			}
		});
		clustersViewButton.setValue(false);
		toolBar.add(clustersViewButton);

		
			
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
		clusterIdColumn.setHidden(true);
		thumbnailColumn = getThumbnailColumn();
		configs.add(thumbnailColumn);
		configs.add(getIdColumn());
		configs.add(getChainsColumn());
		configs.add(getAreaColumn());
		configs.add(getOperatorColumn());
		configs.add(getSizesColumn());
		configs.add(getMethodsColumn(props.geometryCall(),ScoringMethod.EPPIC_GEOMETRY));
		configs.add(getMethodsColumn(props.coreRimCall(),ScoringMethod.EPPIC_CORERIM));
		configs.add(getMethodsColumn(props.coreSurfaceCall(),ScoringMethod.EPPIC_CORESURFACE));
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
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		column.setMenuDisabled(true);
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
		column.setCell(new MethodCallCell(resultsStore, ScoringMethod.EPPIC_FINAL));
		column.setSummaryType(new MethodSummaryType.FinalCallSummaryType());
		column.setSummaryRenderer(new FinalCallSummaryRenderer());
		fillColumnSettings(column, "finalCallName");
		column.setColumnTextClassName("eppic-results-final-call");
		return column;
	}
	
	private SummaryColumnConfig<InterfaceItemModel, ?> getMethodsColumn(
			ValueProvider<InterfaceItemModel, String> vp,
			String type) {
		SummaryColumnConfig<InterfaceItemModel, String> column = new SummaryColumnConfig<InterfaceItemModel, String>(vp);
		column.setCell(new MethodCallCell(resultsStore, type));
		
		boolean isSummarySet = false;
		if(type.equals(ScoringMethod.EPPIC_CORERIM)){
			column.setSummaryType(new MethodSummaryType.CoreRimSummaryType());
			isSummarySet = true;
		} else if(type.equals(ScoringMethod.EPPIC_CORESURFACE)){
			column.setSummaryType(new MethodSummaryType.CoreSurfaceSummaryType());
			isSummarySet = true;
		} else if(type.equals(ScoringMethod.EPPIC_GEOMETRY)){
			column.setSummaryType(new MethodSummaryType.GeometrySummaryType());
			isSummarySet = true;
		}
		
		if(isSummarySet) column.setSummaryRenderer(new MethodsSummaryRenderer());
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
				new SummaryColumnConfig<InterfaceItemModel, Integer>(props.interfaceId());
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
		summary.setStartCollapsed(false);
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
		

		new QuickTip(resultsGrid);
		// Since upgrade to GXT 3.1, the "blank tooltip" issue is gone (see CRK-148 in jira 
		// and http://www.sencha.com/forum/showthread.php?194571-Use-of-QuickTip-leads-to-empty-tooltips)
		// Thus the code below (needed to fix the issue in 3.0.1) is not needed anymore
		//QuickTip gridQT = new QuickTip(resultsGrid);
		//Bug-Fix in GXt 3.0.1
		//To fix the issue of blank Tooltips we set the delay
		//gridQT.setQuickShowInterval(0);
		//gridQT.getToolTipConfig().setShowDelay(0);
		
		return resultsGrid;
	}
	
	/**
	 * Sets content of results grid.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsGrid(PdbInfo resultsData)
	{
		boolean hideWarnings = true;
		
		resultsStore.clear();

		List<InterfaceItemModel> data = new ArrayList<InterfaceItemModel>();
		
		List<InterfaceCluster> interfaceClusters = resultsData.getInterfaceClusters();

		if (interfaceClusters != null)
		{
			for(InterfaceCluster cluster:interfaceClusters)
			{
				List<Interface> interfaceItems = cluster.getInterfaces();
				for (Interface interfaceItem : interfaceItems) 
				{
					if((interfaceItem.getInterfaceWarnings() != null) &&
							(interfaceItem.getInterfaceWarnings().size() > 0))
					{
						hideWarnings = false;
					}

					InterfaceItemModel model = new InterfaceItemModel();

					//Set interface scores
					for(InterfaceScore interfaceScore : interfaceItem.getInterfaceScores())
					{
						if(interfaceScore.getMethod().equals(ScoringMethod.EPPIC_GEOMETRY))
						{
							String size1 = String.valueOf(Math.round(interfaceScore.getScore1()));
							String size2 = String.valueOf(Math.round(interfaceScore.getScore2()));
							model.setGeometryCall(interfaceScore.getCallName());
							model.setSizes(size1 + " + " + size2);
						}

						if(interfaceScore.getMethod().equals(ScoringMethod.EPPIC_CORERIM))
						{
							model.setCoreRimCall(interfaceScore.getCallName());
						}

						if(interfaceScore.getMethod().equals(ScoringMethod.EPPIC_CORESURFACE))
						{
							model.setCoreSurfaceCall(interfaceScore.getCallName());
						}

						if(interfaceScore.getMethod().equals(ScoringMethod.EPPIC_FINAL))
						{
							model.setFinalCallName(interfaceScore.getCallName());
							model.setFinalConfidence(interfaceScore.getConfidence());
						}
					}

					//Set interface cluster score
					for(InterfaceClusterScore clusterScore: cluster.getInterfaceClusterScores())
					{
						if(clusterScore.getMethod().equals(ScoringMethod.EPPIC_GEOMETRY))
						{
							model.setClusterGeometryCall(clusterScore.getCallName());
						}

						if(clusterScore.getMethod().equals(ScoringMethod.EPPIC_CORERIM))
						{
							model.setClusterCoreRimCall(clusterScore.getCallName());
						}

						if(clusterScore.getMethod().equals(ScoringMethod.EPPIC_CORESURFACE))
						{
							model.setClusterCoreSurfaceCall(clusterScore.getCallName());
						}

						if(clusterScore.getMethod().equals(ScoringMethod.EPPIC_FINAL))
						{
							model.setClusterFinalCall(clusterScore.getCallName());
							model.setClusterFinalConfidence(clusterScore.getConfidence());
						}
					}
					model.setInterfaceId(interfaceItem.getInterfaceId());
					model.setClusterId(interfaceItem.getClusterId());
					model.setChain1(interfaceItem.getChain1());
					model.setChain2(interfaceItem.getChain2());
					model.setArea(interfaceItem.getArea());
					model.setOperator(interfaceItem.getOperator());
					model.setOperatorType(interfaceItem.getOperatorType());
					model.setInfinite(interfaceItem.isInfinite());
					model.setWarnings(interfaceItem.getInterfaceWarnings());
					String thumbnailUrl = ApplicationContext.getSettings().getResultsLocation() +
							ApplicationContext.getPdbInfo().getJobId() + 
							"/" + ApplicationContext.getPdbInfo().getTruncatedInputName() +
							EppicParams.INTERFACES_COORD_FILES_SUFFIX +
							"." + interfaceItem.getInterfaceId() + ".75x75.png";
					if(ApplicationContext.getPdbInfo().getJobId().length() == 4)
						thumbnailUrl = ApplicationContext.getSettings().getResultsLocation() +
							ApplicationContext.getPdbInfo().getJobId().toLowerCase() + 
							"/" + ApplicationContext.getPdbInfo().getTruncatedInputName() +
							EppicParams.INTERFACES_COORD_FILES_SUFFIX + 
							"." + interfaceItem.getInterfaceId() + ".75x75.png";
					model.setThumbnailUrl(thumbnailUrl);

					data.add(model);
				}
			}
			
			Collections.sort(data, new Comparator<InterfaceItemModel>() {

				@Override
				public int compare(InterfaceItemModel o1, InterfaceItemModel o2) {
					return new Integer(o1.getInterfaceId()).compareTo(o2.getInterfaceId());
				}
			});
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
		if (value) { //check clusters event
			clustersView.groupBy(clusterIdColumn);
			ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML("<table cellpadding=0 cellspacing=0><tr><td width='150px'><span class='eppic-general-info-label-new'>Assemblies</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#id/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getAssemblies().size() + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interfaces</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getNumberOfInterfaces() + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interface clusters</span></td><td><span class='eppic-general-info-label-value-new'>" + ApplicationContext.getPdbInfo().getInterfaceClusters().size()+"</span></td></tr></table>");
			if(ApplicationContext.getSelectedAssemblyId() > 0)
				History.newItem("clusters/" + ApplicationContext.getPdbInfo().getPdbCode() + "/" + ApplicationContext.getSelectedAssemblyId());
			else
				History.newItem("clusters/" + ApplicationContext.getPdbInfo().getPdbCode());
		} else{ //uncheck clusters event
			clustersView.groupBy(null);
			resultsStore.addSortInfo(0, new StoreSortInfo<InterfaceItemModel>(props.interfaceId(), SortDir.ASC));
			//Hide cluster id column
			resultsGrid.getColumnModel().getColumn(0).setHidden(true);
			resultsGrid.getView().refresh(true);
			
			if(ApplicationContext.getSelectedAssemblyId() > 0)
				History.newItem("interfaces/" + ApplicationContext.getPdbInfo().getPdbCode() + "/" + ApplicationContext.getSelectedAssemblyId());
			else
				History.newItem("interfaces/" + ApplicationContext.getPdbInfo().getPdbCode());
			//todo put a method in applicationcontext to get the number of interfaces
			ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML("<table cellpadding=0 cellspacing=0><tr><td width='150px'><span class='eppic-general-info-label-new'>Assemblies</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#id/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getAssemblies().size() + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interfaces</span></td><td><span class='eppic-general-info-label-value-new'>" + ApplicationContext.getNumberOfInterfaces() + "</span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interface clusters</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#clusters/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getInterfaceClusters().size()+"</a></span></td></tr></table>");
		}
	}

	/**
	 * Creates combobox used to select molecular viewer.
	 * @return viewer selector
	 */
	private ComboBox<String> createViewerTypeCombobox()
	{
		ListStore<String> store = new ListStore<String>(new ModelKeyProvider<String>() {
			@Override
			public String getKey(String item) {
				return item;
			}
		});

		store.add(AppPropertiesManager.CONSTANTS.viewer_local());
		store.add(AppPropertiesManager.CONSTANTS.viewer_jmol());
		//store.add(AppPropertiesManager.CONSTANTS.viewer_pse());

		final ComboBox<String> viewerTypeComboBox = new ComboBox<String>(store, new LabelProvider<String>() {
			@Override
			public String getLabel(String item) {
				return item;
			}
		});
		
		viewerTypeComboBox.setId("viewercombo");
		viewerTypeComboBox.setTriggerAction(TriggerAction.ALL);
		viewerTypeComboBox.setEditable(false);
		viewerTypeComboBox.setWidth(100);

		viewerTypeComboBox.setToolTipConfig(createViewerTypeComboBoxToolTipConfig());
		
		String viewerCookie = Cookies.getCookie("crkviewer");
		if (viewerCookie != null) {
			viewerTypeComboBox.setValue(viewerCookie);
		} else {
			viewerTypeComboBox.setValue(AppPropertiesManager.CONSTANTS.viewer_jmol());
		}

		ApplicationContext.setSelectedViewer(viewerTypeComboBox.getValue());

		viewerTypeComboBox.addSelectionHandler(new SelectionHandler<String>() {	
			@Override
			public void onSelection(SelectionEvent<String> event) {
				Cookies.setCookie("crkviewer", event.getSelectedItem());
				ApplicationContext.setSelectedViewer(event.getSelectedItem());
				
			}
		});
		
		return viewerTypeComboBox;
	}
	
	/**
	 * Creates configuration of the tooltip displayed over viewer type selector.
	 * @return configuration of tooltip displayed over viewer type selector
	 */
	private ToolTipConfig createViewerTypeComboBoxToolTipConfig()
	{
		ToolTipConfig viewerTypeComboBoxToolTipConfig = new ToolTipConfig();  
		viewerTypeComboBoxToolTipConfig.setTitleHtml("3D viewer selector");
		viewerTypeComboBoxToolTipConfig.setBodyHtml(generateViewerTypeComboBoxTooltipTemplate());  
		viewerTypeComboBoxToolTipConfig.setShowDelay(0);
		viewerTypeComboBoxToolTipConfig.setDismissDelay(0);
		return viewerTypeComboBoxToolTipConfig;
	}
	
	/**
	 * Generates content of viewer type tooltip.
	 * @return content of viewer type tooltip
	 */
	private String generateViewerTypeComboBoxTooltipTemplate()
	{
		String viewerTypeDescription = "To run selected 3D viewer please click one of the thumbnails on the list below. The following options are provided: " +
									   "<div><ul class=\"eppic-tooltip-list\">" +
									   "<li>PDB file downloadable to a local molecular viewer</li>" +
									   "<li>Browser embedded 3Dmol.js viewer (no need for local viewer)</li>" +
									   "<li>PyMol session file (.pse) to be opened in local PyMol</li>" +
									   "</ul></div>";
		return viewerTypeDescription;
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
				ViewerRunner.runViewer(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().getInterfaceId()));
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
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfaceResiduesEvent((Integer)resultsGrid.getSelectionModel().getSelectedItem().getInterfaceId()));
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
