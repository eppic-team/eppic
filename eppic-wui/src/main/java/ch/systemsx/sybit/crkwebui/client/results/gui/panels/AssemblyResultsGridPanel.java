package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectAssemblyResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAssembliesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAssemblyViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAssemblyViewerInNewTabEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDiagramViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfacesOfAssemblyDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowThumbnailEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectAssemblyResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssembliesHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssemblyViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAssemblyViewerInNewTabHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDiagramViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowThumbnailHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.WindowHideHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.PopupRunner;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.managers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModelProperties;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.AssemblyDiagramCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.AssemblyMethodCallCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.AssemblyThumbnailCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.InterfacesLinkCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.SubscriptTypeCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.AssemblyMethodSummaryType;
import ch.systemsx.sybit.crkwebui.client.results.gui.grid.util.AssemblyMethodsSummaryRenderer;
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.AssemblyScore;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
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
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import eppic.DataModelAdaptor;
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
	ColumnConfig<AssemblyItemModel, String> diagramColumn;
	ColumnConfig<AssemblyItemModel, String> warningsColumn;
	SummaryColumnConfig<AssemblyItemModel, Integer> clusterIdColumn;
	
	public static ToolBar assembliesToolBar;
	public static HTML assemblies_toolbar_link;
	public static ComboBox<String> viewerSelectorBox;
	
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
		
		resultsGrid = createResultsGrid();		
		panelContainer.add(createSelectorToolBar(), new VerticalLayoutData(1,-1));
		panelContainer.add(resultsGrid, new VerticalLayoutData(-1,-1));
		
		this.add(panelContainer, new VerticalLayoutData(1,-1));
		
		initializeEventsListeners();
	}
	
	private ToolBar createSelectorToolBar(){
		assembliesToolBar = new ToolBar();
		
		viewerSelectorBox = createViewerTypeCombobox();
		viewerSelectorBox.setStyleName("eppic-default-label");

		assembliesToolBar.add(new HTML(AppPropertiesManager.CONSTANTS.results_grid_viewer_combo_label()+":&nbsp;"));
		assembliesToolBar.add(viewerSelectorBox);
		assembliesToolBar.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
		
		//assemblies_toolbar_link = new HTML("<a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getSelectedJobId()+"'>View All Interfaces</a>");
		assemblies_toolbar_link = new HTML("<a href='" + GWT.getHostPageBaseURL() + "#assembly/"+ApplicationContext.getSelectedJobId()+"'>View All Interfaces</a>");
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
		diagramColumn = getDiagramColumn();
		configs.add(diagramColumn);		
		configs.add(getIdentifierColumn()); //what is displayed in the table (same as id)
		configs.add(getMmSizeColumn());
		configs.add(getStoichiometryColumn());
		configs.add(getSymmetryColumn());
		configs.add(getPredictionColumn());
		configs.add(getNumInterfacesColumn());
		//configs.add(getDetailsColumn());

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
		
		//this was the old way of getting column widths
		//column.setWidth(Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("assembly_results_"+type+"_width")));
		
		//the sum of total column width of all the columns as configured in grid.properties
		float TOTAL_COLUMN_WIDTH = 535;
		
		//standard screen size by default
		float ACTUAL_SCREEN_SIZE = 800;

		try {
			ACTUAL_SCREEN_SIZE = Integer.parseInt(this.width.replace("px", "")); //actual size of the screen in user's browser
		}catch(Exception e) {}
		
		float preconfiguredColumnSize = 0;
		float columnWidth = 0;

		try{
			preconfiguredColumnSize = Float.parseFloat(ApplicationContext.getSettings().getGridProperties().get("assembly_results_"+type+"_width"));
		}catch (Exception e) {}
		if (ACTUAL_SCREEN_SIZE > TOTAL_COLUMN_WIDTH)
			columnWidth = (preconfiguredColumnSize / TOTAL_COLUMN_WIDTH) * ACTUAL_SCREEN_SIZE;
		
		int columnWidthInt = Math.round(columnWidth);
		column.setWidth(columnWidthInt);
		
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
	
	@SuppressWarnings("unused")
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
	
	private ColumnConfig<AssemblyItemModel, String> getNumInterfacesColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> numInterfacesColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.numInterfaces());
		numInterfacesColumn.setCell(new InterfacesLinkCell());
		fillColumnSettings(numInterfacesColumn, "numinterfaces"); 
		numInterfacesColumn.setResizable(false);
		numInterfacesColumn.setSortable(false);
		return numInterfacesColumn; 
	}	
	
	
	
	private SummaryColumnConfig<AssemblyItemModel, String> getSymmetryColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> symmetryColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.symmetry());
		fillColumnSettings(symmetryColumn, "symmetry");
		return symmetryColumn;
	}
	
	/*private SummaryColumnConfig<AssemblyItemModel, String> getStoichiometryColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> stioColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.stoichiometry());
		fillColumnSettings(stioColumn, "stoichiometry");
		return stioColumn;
	}*/
	
	private SummaryColumnConfig<AssemblyItemModel, String> getStoichiometryColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> stioColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.stoichiometry());
		stioColumn.setCell(new SubscriptTypeCell());		
		stioColumn.setSummaryType(new AssemblyMethodSummaryType.FinalCallSummaryType());
		//stioColumn.setSummaryRenderer(new AssemblyMethodsSummaryRenderer());
		fillColumnSettings(stioColumn, "stoichiometry");
		//predictionColumn.setColumnTextClassName("eppic-results-final-call");
		return stioColumn;
	}
	
	private SummaryColumnConfig<AssemblyItemModel, String> getPredictionColumn() {
		SummaryColumnConfig<AssemblyItemModel, String> predictionColumn = 
				new SummaryColumnConfig<AssemblyItemModel, String>(props.prediction());
		predictionColumn.setCell(new AssemblyMethodCallCell(resultsStore,ScoringMethod.EPPIC_FINAL));		
		predictionColumn.setSummaryType(new AssemblyMethodSummaryType.FinalCallSummaryType());
		predictionColumn.setSummaryRenderer(new AssemblyMethodsSummaryRenderer());
		fillColumnSettings(predictionColumn, "prediction");
		predictionColumn.setColumnTextClassName("eppic-results-final-call");
		return predictionColumn;
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
						value.intValue() > 1 ? "(" + value.intValue() + " Assemblies)" : "(1 Assembly)");
			}
		});
		
		thumbnailColumn.setCell(new AssemblyThumbnailCell());
		fillColumnSettings(thumbnailColumn, "thumbnail");
		thumbnailColumn.setResizable(false);

		return thumbnailColumn;
	}

	
	private SummaryColumnConfig<AssemblyItemModel, String> getDiagramColumn(){
		int numinterfaces = 0;
		List<InterfaceCluster> clusters = ApplicationContext.getPdbInfo().getInterfaceClusters();
		for(InterfaceCluster ic : clusters){
			numinterfaces=+ ic.getInterfaces().size();
		}
		
		SummaryColumnConfig<AssemblyItemModel, String> diagramColumn = null;
		if(numinterfaces != 0)
			diagramColumn = new SummaryColumnConfig<AssemblyItemModel, String>(props.diagramUrl());
		else
			diagramColumn = new SummaryColumnConfig<AssemblyItemModel, String>(null);

		diagramColumn.setSummaryType(new SummaryType.CountSummaryType<String>());
		diagramColumn.setSummaryRenderer(new SummaryRenderer<AssemblyItemModel>() {

			@Override
			public SafeHtml render(
					Number value,
					Map<ValueProvider<? super AssemblyItemModel, ?>, Number> data) {
				return SafeHtmlUtils.fromTrustedString(
						value.intValue() > 1 ? "(" + value.intValue() + " Assemblies)" : "(1 Assembly)");
			}
		});
		
		diagramColumn.setCell(new AssemblyDiagramCell());
		fillColumnSettings(diagramColumn, "diagram");
		diagramColumn.setResizable(false);

		return diagramColumn;
	}	

	
	/**
	 * Creates grid storing results of calculations for each of the interfaces.
	 * @return interfaces grid
	 */
	private Grid<AssemblyItemModel> createResultsGrid()
	{
		final Grid<AssemblyItemModel> resultsGrid = new Grid<AssemblyItemModel>(resultsStore, resultsColumnModel);
		resultsGrid.setBorders(false);
		resultsGrid.getView().setStripeRows(true);
		resultsGrid.getView().setColumnLines(false);
		resultsGrid.getView().setForceFit(true);
		
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

		
		return resultsGrid;
	}
	
	
	public void fillResultsGrid(PdbInfo resultsData)
	{
		this.resultsData = resultsData;
		resultsStore.clear();

		List<AssemblyItemModel> data = new ArrayList<AssemblyItemModel>();
		
		List<Assembly> assemblies = resultsData.getAssemblies(); 

		if (assemblies != null)
		{
			for(Assembly assembly : assemblies)
			{
				// issue #102: as a temporary fix for 3.0, we'll simply skip the invalid assemblies (which can only come from PDB annotation, not from EPPIC)
				if (!assembly.isTopologicallyValid()) continue;

				AssemblyItemModel model = new AssemblyItemModel();
				model.setAssemblyId(assembly.getId()); //not actually visible
				model.setIdentifier(assembly.getIdentifierString());
				model.setPdbCode(resultsData.getPdbCode());
				model.setPdb1Assembly(false);
				for (AssemblyScore as : assembly.getAssemblyScores()) {
					// if pdb1 is present (with bio) then we set the field in model (see issue #100)
					if (as.getMethod()!=null && as.getMethod().equals(DataModelAdaptor.PDB_BIOUNIT_METHOD_PREFIX +"1") && 
							as.getCallName()!=null && as.getCallName().equals("bio") ) {
						model.setPdb1Assembly(true);	
					}					
				}
				
				String jobId = ApplicationContext.getPdbInfo().getJobId();
				// not sure why we need lower casing here, perhaps if input is upper case? - JD 2017-02-04
				// what's for sure is that if we lower case for user job ids then the urls are wrong - JD 2017-02-04
				if(jobId.length() == 4) jobId = jobId.toLowerCase(); 
				String truncatedInputName = ApplicationContext.getPdbInfo().getTruncatedInputName();

				String thumbnailUrl = 
						ApplicationContext.getSettings().getResultsLocationForJob(jobId) + 
						"/" + truncatedInputName +
						EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX +
						"." + assembly.getId() + ".75x75.png";
				
				model.setThumbnailUrl(thumbnailUrl);

				String diagramUrl = 
						ApplicationContext.getSettings().getResultsLocationForJob(jobId) + 
						"/" + truncatedInputName +
						EppicParams.ASSEMBLIES_DIAGRAM_FILES_SUFFIX +
						"." + assembly.getId() + ".75x75.png";
				
				model.setDiagramUrl(diagramUrl);

				model.setMmSize(assembly.getMmSizeString());
				//testing only
				/*String stio = assembly.getStoichiometryString();
					if (stio.indexOf("2") != -1)
						stio = "A(2)";
					model.setStoichiometry(stio);*/
				model.setStoichiometry(assembly.getStoichiometryString());
				model.setSymmetry(assembly.getSymmetryString());
				model.setComposition(assembly.getCompositionString());
				model.setPrediction(assembly.getPredictionString());
				//model.setNumInterfaces(assembly.getInterfaces().size()+"");
				if(assembly.getInterfaces().size() == 0)
					model.setNumInterfaces("0 Interfaces");
				else if(assembly.getInterfaces().size() == 1)
					//model.setNumInterfaces("<a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getSelectedJobId()+"/"+assembly.getId() +"'>"+ assembly.getInterfaces().size() + " Interface</a>");
					model.setNumInterfaces("<a href='" + GWT.getHostPageBaseURL() + "#assembly/"+ApplicationContext.getSelectedJobId()+"/"+assembly.getId() +"'>"+ assembly.getInterfaces().size() + " Interface</a>");
				else 
					//model.setNumInterfaces("<a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getSelectedJobId()+"/"+assembly.getId() +"'>"+ assembly.getInterfaces().size() + " Interfaces</a>");
					model.setNumInterfaces("<a href='" + GWT.getHostPageBaseURL() + "#assembly/"+ApplicationContext.getSelectedJobId()+"/"+assembly.getId() +"'>"+ assembly.getInterfaces().size() + " Interfaces</a>");

				data.add(model);
			}
		}
		resultsStore.addAll(data);
		
	}
	
	
	private void refreshResultsGrid(){
		resultsGrid.getView().refresh(true);
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

		
		store.add(AppPropertiesManager.CONSTANTS.viewer_jmol());
		store.add(AppPropertiesManager.CONSTANTS.viewer_local());
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
		
		//to set the default.
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
									   "<li>mmCIF file downloadable to a local molecular viewer</li>" +
									   "<li>Browser embedded NGL viewer (no need for local viewer)</li>" +
									   "</ul></div>";
		return viewerTypeDescription;
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
		
		//not sure if this is needed or where it is called
		EventBusManager.EVENT_BUS.addHandler(ShowAssembliesEvent.TYPE, new ShowAssembliesHandler() {
					
			@Override
			public void onShowAssemblies(ShowAssembliesEvent event) {
				refreshResultsGrid();	
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowAssemblyViewerEvent.TYPE, new ShowAssemblyViewerHandler() {
			
			@Override
			public void onShowAssemblyViewer(ShowAssemblyViewerEvent event) 
			{
				ViewerRunner.runViewerAssembly(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().getAssemblyId()));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowAssemblyViewerInNewTabEvent.TYPE, new ShowAssemblyViewerInNewTabHandler() {
			
			@Override
			public void onShowAssemblyViewerInNewTab(
					ShowAssemblyViewerInNewTabEvent event) {
				ViewerRunner.runViewerAssemblyInNewTab(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().getAssemblyId()));
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowDiagramViewerEvent.TYPE, new ShowDiagramViewerHandler() {
			
			@Override
			public void onShowDiagramViewer(ShowDiagramViewerEvent event) 
			{
				PopupRunner.popupAssemblyDiagram(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().getAssemblyId()));
			} 
		});		
		
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
				History.newItem("interfaces/" + pdbCode + "/" + assemblyID);	
				PDBIdentifierPanel.informationLabel.setHTML(EscapedStringGenerator.generateEscapedString(
								AppPropertiesManager.CONSTANTS.info_panel_interface_pdb_identifier() + ": "));
				PDBIdentifierPanel.pdbNameLabel.setHTML("Assembly " + assemblyID + " in ");// + pdbCode);
				PDBIdentifierPanel.pdbNameLabel.setHTML("<a target='_blank' href='http://www.pdb.org/pdb/explore/explore.do?structureId="+pdbCode+"'>"+pdbCode+"</a>");
				InformationPanel.assemblyInfoPanel.setHeadingHtml("General Information");			
			}
		}); 
		
	}
	
}
