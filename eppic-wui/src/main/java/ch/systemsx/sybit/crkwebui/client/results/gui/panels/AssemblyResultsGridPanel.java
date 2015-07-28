package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectAssemblyResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAssembliesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfacesOfAssemblyDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowThumbnailEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UncheckClustersRadioEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalDoubleCell;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.EppicLabel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectAssemblyResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssembliesHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDetailsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowInterfacesOfAssemblyDataHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowThumbnailHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.UncheckClustersRadioHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.WindowHideHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.managers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModelProperties;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.AssemblyMethodCallCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.AssemblyOperatorTypeCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.AssemblyWarningsCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.DetailsButtonCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.InterfacesButtonCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.ThumbnailCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.AssemblyClustersGridView;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.AssemblyFinalCallSummaryRenderer;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.AssemblyMethodsSummaryRenderer;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.ClustersGridView;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.MethodSummaryType;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsPanel;
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
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
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
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

public class AssemblyResultsGridPanel extends VerticalLayoutContainer
{
	private VerticalLayoutContainer panelContainer;
	 
	private ListStore<AssemblyItemModel> resultsStore;
	private ColumnModel<AssemblyItemModel> resultsColumnModel;
	private Grid<AssemblyItemModel> resultsGrid;
	PdbInfo resultsData;
	
	private int panelWidth;
	
	private static final AssemblyItemModelProperties props = GWT.create(AssemblyItemModelProperties.class);
	
	ColumnConfig<AssemblyItemModel, String> thumbnailColumn;
	ColumnConfig<AssemblyItemModel, String> warningsColumn;
	SummaryColumnConfig<AssemblyItemModel, Integer> clusterIdColumn;
	
	public static ToolBar assembliesToolBar;
	public static HTML assemblies_toolbar_link;
	
	public AssemblyResultsGridPanel(int width)
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
		
		resultsStore = new ListStore<AssemblyItemModel>(props.key());
		List<ColumnConfig<AssemblyItemModel, ?>> resultsConfigs = createColumnConfig();
		resultsColumnModel = new ColumnModel<AssemblyItemModel>(resultsConfigs);
		//clustersView = createClusterView();
		
		resultsGrid = createResultsGrid();		
		panelContainer.add(createSelectorToolBar(), new VerticalLayoutData(1,-1));
		panelContainer.add(resultsGrid, new VerticalLayoutData(-1,-1));
		
		this.add(panelContainer, new VerticalLayoutData(1,-1));
		
		//Panel to display if no interfaces found
		//noInterfaceFoundPanel = new VerticalLayoutContainer();
		
		initializeEventsListeners();
	}
	
	private ToolBar createSelectorToolBar(){
		assembliesToolBar = new ToolBar();
		
		ComboBox<String> viewerSelectorBox = createViewerTypeCombobox();
		viewerSelectorBox.setStyleName("eppic-default-label");
		assembliesToolBar.add(new HTML(AppPropertiesManager.CONSTANTS.results_grid_viewer_combo_label()+":&nbsp;"));
		assembliesToolBar.add(viewerSelectorBox);
		assembliesToolBar.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
		//assembliesToolBar.add(new HTML("<a href='/ewui/#interfaces/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>View All Interfaces</a>"));	
		
		assemblies_toolbar_link = new HTML("<a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>View All Interfaces</a>");
		assembliesToolBar.add(assemblies_toolbar_link);
		
		return assembliesToolBar;
	}
	
	/**
	 * Creates configurations of the columns for results grid.
	 * @return list of columns configurations for results grid
	 */
	private List<ColumnConfig<AssemblyItemModel, ?>> createColumnConfig() 
	{
		List<ColumnConfig<AssemblyItemModel, ?>> configs = new ArrayList<ColumnConfig<AssemblyItemModel, ?>>();
				

		configs.add(getIdColumn()); // the id value (same as identifier)
		//configs.add(getCompositionColumn());
		thumbnailColumn = getThumbnailColumn();
		configs.add(thumbnailColumn);
		configs.add(getIdentifierColumn()); //what is displayed in the table (same as id)
		configs.add(getMmSizeColumn());
		configs.add(getStoichiometryColumn());
		configs.add(getSymmetryColumn());
		configs.add(getPredictionColumn());
		configs.add(getDetailsColumn());

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
	private void fillColumnSettings(ColumnConfig<AssemblyItemModel, ?> column, String type){
		column.setColumnHeaderClassName("eppic-default-font");
		column.setWidth(Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("assembly_results_"+type+"_width")));
		column.setHeader(EscapedStringGenerator.generateSafeHtml(
				ApplicationContext.getSettings().getGridProperties().get("assembly_results_"+type+"_header")));
		
		String tooltip = ApplicationContext.getSettings().getGridProperties().get("assembly_results_"+type+"_tooltip");
		if(tooltip != null)
			column.setToolTip(EscapedStringGenerator.generateSafeHtml(tooltip));
		
		column.setColumnTextClassName("eppic-results-grid-common-cells");
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		column.setMenuDisabled(true);
	}

	private SummaryColumnConfig<AssemblyItemModel, Integer> getIdColumn() {
		SummaryColumnConfig<AssemblyItemModel, Integer> idColumn = 
				new SummaryColumnConfig<AssemblyItemModel, Integer>(props.assemblyId());
		fillColumnSettings(idColumn, "id");
		return idColumn;
	}
	
	private ColumnConfig<AssemblyItemModel, String> getIdentifierColumn() {
		ColumnConfig<AssemblyItemModel, String> identifierColumn = 
				new ColumnConfig<AssemblyItemModel, String>(props.identifier());
		fillColumnSettings(identifierColumn, "identifier");
		return identifierColumn;
	}
	
	private ColumnConfig<AssemblyItemModel, String> getCompositionColumn() {
		ColumnConfig<AssemblyItemModel, String> compositionColumn = 
				new ColumnConfig<AssemblyItemModel, String>(props.composition());
		fillColumnSettings(compositionColumn, "composition");
		return compositionColumn;
	}
	
	private ColumnConfig<AssemblyItemModel, String> getMmSizeColumn() {
		ColumnConfig<AssemblyItemModel, String> sizeColumn = 
				new ColumnConfig<AssemblyItemModel, String>(props.mmSize());
		fillColumnSettings(sizeColumn, "mmsize"); 
		return sizeColumn;
	}
	
	private SummaryColumnConfig<AssemblyItemModel, String> getSymmetryColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> symmetryColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.symmetry());
		fillColumnSettings(symmetryColumn, "symmetry");
		return symmetryColumn;
	}
	
	private SummaryColumnConfig<AssemblyItemModel, String> getStoichiometryColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> stioColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.stoichiometry());
		fillColumnSettings(stioColumn, "stoichiometry");
		return stioColumn;
	}
	
	private SummaryColumnConfig<AssemblyItemModel, String> getPredictionColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> predictionColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.prediction());
		fillColumnSettings(predictionColumn, "prediction");
		return predictionColumn;
	}
	
	private SummaryColumnConfig<AssemblyItemModel, String> getDetailsColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> column = 
				//new SummaryColumnConfig<AssemblyItemModel, String>(props.detailsButtonText());
				new SummaryColumnConfig<AssemblyItemModel, String>(props.detailsButtonText()); 
		column.setCell(new InterfacesButtonCell());
		fillColumnSettings(column, "details"); 
		column.setResizable(false);
		column.setSortable(false);
		return column; 
	}

	
	private SummaryColumnConfig<AssemblyItemModel, String> getThumbnailColumn(){
		SummaryColumnConfig<AssemblyItemModel, String> thumbnailColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.thumbnailUrl());

		thumbnailColumn.setSummaryType(new SummaryType.CountSummaryType<String>());
		thumbnailColumn.setSummaryRenderer(new SummaryRenderer<AssemblyItemModel>() {

			@Override
			public SafeHtml render(
					Number value,
					Map<ValueProvider<? super AssemblyItemModel, ?>, Number> data) {
				return SafeHtmlUtils.fromTrustedString(
						//value.intValue() > 1 ? "(" + value.intValue() + " Interfaces)" : "(1 Interface)");
						value.intValue() > 1 ? "(" + value.intValue() + " Assemblies)" : "(1 Assembly)");
			}
		});
		
		thumbnailColumn.setCell(new ThumbnailCell());
		fillColumnSettings(thumbnailColumn, "thumbnail");
		thumbnailColumn.setResizable(false);

		return thumbnailColumn;
	}


	
	/**
	 * Creates grid storing results of calculations for each of the interfaces.
	 * @return interfaces grid
	 */
	private Grid<AssemblyItemModel> createResultsGrid()
	{
		final Grid<AssemblyItemModel> resultsGrid = new Grid<AssemblyItemModel>(resultsStore, resultsColumnModel);
		resultsGrid.setBorders(false);
		//resultsGrid.setView(clustersView);
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
				AssemblyItemModel model = resultsGrid.getSelectionModel().getSelectedItem();
				if(model != null)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ShowInterfacesOfAssemblyDataEvent(resultsData));
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
	
	
	public void fillResultsGrid(PdbInfo resultsData)
	{
		//Window.alert("in fillResultsGrid");
		this.resultsData = resultsData;
		resultsStore.clear();

		List<AssemblyItemModel> data = new ArrayList<AssemblyItemModel>();
		
		//List<InterfaceCluster> interfaceClusters = resultsData.getInterfaceClusters();
		List<Assembly> assemblies = resultsData.getAssemblies(); 
		//Window.alert("assemblies size " + assemblies.size());
		if (assemblies != null)
		{
			for(Assembly assembly : assemblies)
			{
				
					AssemblyItemModel model = new AssemblyItemModel();
					model.setAssemblyId(assembly.getId()); //not actually visible
					model.setIdentifier(assembly.getIdentifierString());
					model.setPdbCode(resultsData.getPdbCode());
					
					String thumbnailUrl = ApplicationContext.getSettings().getResultsLocation() +
							ApplicationContext.getPdbInfo().getJobId() + 
							"/" + ApplicationContext.getPdbInfo().getTruncatedInputName() +
							EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX +
							"." + assembly.getId() + ".75x75.png";
					if(ApplicationContext.getPdbInfo().getJobId().length() == 4)
						thumbnailUrl = ApplicationContext.getSettings().getResultsLocation() +
							ApplicationContext.getPdbInfo().getJobId().toLowerCase() + 
							"/" + ApplicationContext.getPdbInfo().getTruncatedInputName() +
							EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX + 
							"." + assembly.getId() + ".75x75.png";
					model.setThumbnailUrl(thumbnailUrl);
					model.setMmSize(assembly.getMmSizeString());
					model.setStoichiometry(assembly.getStoichiometryString());
					model.setSymmetry(assembly.getSymmetryString());
					model.setComposition(assembly.getCompositionString());
					model.setPrediction(assembly.getPredictionString());
					data.add(model);
			}
		}
		resultsStore.addAll(data);
		
	}
	
	
	private void refreshResultsGrid(){
		resultsGrid.getView().refresh(true);
	}
	
	/**
	 * 
	 */
	/*private void onClustersRadioValueChange(boolean value){
		if (value) {
			clustersView.groupBy(clusterIdColumn);
		} else{
			clustersView.groupBy(null);
			resultsStore.addSortInfo(0, new StoreSortInfo<AssemblyItemModel>(props.interfaceId(), SortDir.ASC));
			//Hide cluster id column
			resultsGrid.getColumnModel().getColumn(0).setHidden(true);
			resultsGrid.getView().refresh(true);
		}
	}*/

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
		store.add(AppPropertiesManager.CONSTANTS.viewer_pse());

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
	/*public void setClustersRadioValue(boolean value){
		clustersViewButton.setValue(value);
		onClustersRadioValueChange(value);
	}*/
	
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
		
		//not sure if this is needed or where it is called
		EventBusManager.EVENT_BUS.addHandler(ShowAssembliesEvent.TYPE, new ShowAssembliesHandler() {
					
			@Override
			public void onShowAssemblies(ShowAssembliesEvent event) {
				refreshResultsGrid();	
				//displayResultView(event.getPdbScoreItem(), ResultsPanel.ASSEMBLIES_VIEW);
				//displayResultView(event.getPdbScoreItem(), ResultsPanel.ASSEMBLIES_VIEW);
			}
		});
		
		/*EventBusManager.EVENT_BUS.addHandler(UncheckClustersRadioEvent.TYPE, new UncheckClustersRadioHandler() {
			
			@Override
			public void onUncheckClustersRadio(UncheckClustersRadioEvent event) {
				setClustersRadioValue(false);
				
			}
		});*/
		
		/*EventBusManager.EVENT_BUS.addHandler(ShowViewerEvent.TYPE, new ShowViewerHandler() 
		{
			@Override
			public void onShowViewer(ShowViewerEvent event) 
			{
				ViewerRunner.runViewer(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().getInterfaceId()));
			}
		});*/ 
		
		EventBusManager.EVENT_BUS.addHandler(ShowThumbnailEvent.TYPE, new ShowThumbnailHandler() {
			
			@Override
			public void onShowThumbnail(ShowThumbnailEvent event)
			{
				for(ColumnConfig<AssemblyItemModel, ?> column : resultsGrid.getColumnModel().getColumns())
				{
					if(column.equals(thumbnailColumn))
					{
						column.setHidden(event.isHideThumbnail());
						resizeContent(panelWidth);
					}
				}
			}
		}); 
		
		//to handle when "enter" is pressed
		/*EventBusManager.EVENT_BUS.addHandler(ShowInterfacesOfAssemblyDataEvent.TYPE, new ShowInterfacesOfAssemblyDataHandler() {
			
			@Override
			public void onShowInterfacesOfAssembly(
					ShowInterfacesOfAssemblyDataEvent event) {
				//Window.alert("AssemblyResultsGridPanel.java onShowInterfacesOfAssembly - enter pressed!");
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfacesOfAssemblyDataEvent(resultsData));
				
			}
			
		});*/
		
		/* this is a bad idea - links the other event to this event!!
		EventBusManager.EVENT_BUS.addHandler(ShowDetailsEvent.TYPE, new ShowDetailsHandler() 
		{
			@Override
			public void onShowDetails(ShowDetailsEvent event) 
			{
				Window.alert("AssemblyResultsGridPanel.java onShowDetails");
				//EventBusManager.EVENT_BUS.fireEvent(new ShowInterfaceResiduesEvent((Integer)resultsGrid.getSelectionModel().getSelectedItem().getInterfaceId()));
			}
		});*/
		
		/*EventBusManager.EVENT_BUS.addHandler(SelectResultsRowEvent.TYPE, new SelectResultsRowHandler() {
			
			@Override
			public void onSelectResultsRow(SelectResultsRowEvent event) {
				Window.alert("handling event");
				resultsGrid.getSelectionModel().select(event.getRowIndex(), false);
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfacesOfAssemblyDataEvent(resultsData));			
			}
		}); */
		
		EventBusManager.EVENT_BUS.addHandler(SelectAssemblyResultsRowEvent.TYPE, new SelectAssemblyResultsRowHandler() {
						
			@Override
			public void onSelectAssemblyResultsRow(SelectAssemblyResultsRowEvent event) {
				resultsGrid.getSelectionModel().select(event.getRowIndex(), false);							
				int assemblyID = resultsGrid.getSelectionModel().getSelectedItem().getAssemblyId();
				String pdbCode = resultsGrid.getSelectionModel().getSelectedItem().getPdbCode();
				PdbInfo newResultsData = resultsData;
				newResultsData = resultsData;
				List<InterfaceCluster> interfaceClusters = resultsData.getAssemblyById(assemblyID).getInterfaceClusters();
				newResultsData.setInterfaceClusters(interfaceClusters);
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfacesOfAssemblyDataEvent(newResultsData));		
				History.newItem("interfaces/" + pdbCode + "/"+assemblyID);		
				ResultsPanel.headerPanel.pdbIdentifierPanel.informationLabel.setHTML(EscapedStringGenerator.generateEscapedString(
								AppPropertiesManager.CONSTANTS.info_panel_interface_pdb_identifier() + ": "));
				ResultsPanel.headerPanel.pdbIdentifierPanel.pdbNameLabel.setHTML("Assembly " + assemblyID + " in " + pdbCode);
				
			}
		}); 
		
	}
	
}
