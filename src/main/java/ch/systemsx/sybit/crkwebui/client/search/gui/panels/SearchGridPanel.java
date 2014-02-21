package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.info.PopUpInfo;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.search.gui.cells.PdbCodeCell;
import ch.systemsx.sybit.crkwebui.client.search.gui.cells.PdbDataDoubleCell;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class SearchGridPanel extends VerticalLayoutContainer
{
	/**
	 * Interface used to project Residue class to Grid
	 * @author biyani_n
	 *
	 */
	public interface PDBSearchResultProperties extends PropertyAccess<PDBSearchResult> {

		@Path("uid")
		ModelKeyProvider<PDBSearchResult> key();

		ValueProvider<PDBSearchResult, String> pdbCode();	  
		ValueProvider<PDBSearchResult, String> title();
		ValueProvider<PDBSearchResult, String> spaceGroup();
		ValueProvider<PDBSearchResult, Double> resolution();
		ValueProvider<PDBSearchResult, Double> rfreeValue();
		ValueProvider<PDBSearchResult, String> expMethod();
	}

	private static final PDBSearchResultProperties props = GWT.create(PDBSearchResultProperties.class);

	private List<ColumnConfig<PDBSearchResult, ?>> resultsConfig;
	private ListStore<PDBSearchResult> store;
	private ColumnModel<PDBSearchResult> cm;
	
	private Grid<PDBSearchResult> resultsGrid;
	private ToolBar selectorToolBar;
	private VerticalLayoutContainer filterTextPanel;
	
	private String selectedPdbCode;

	/**
	 * Constructor
	 */
	public SearchGridPanel(){

		this.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				resizeGrid(event.getWidth());

			}
		});

		store = new ListStore<PDBSearchResult>(props.key());

		resultsConfig = createColumnConfigs();

		cm = new ColumnModel<PDBSearchResult>(resultsConfig);

		createGrid();

		selectorToolBar = createSelectorToolBar();
		filterTextPanel = createFilterPanel();
		
		this.add(filterTextPanel, new VerticalLayoutData(1, -1, new Margins(10,0,15,0)));
		this.add(selectorToolBar, new VerticalLayoutData(1, -1));
		this.add(resultsGrid, new VerticalLayoutData(1, 1));

	}

	/**
	 * Sets content of grid.
	 * @param values list of items to add to the grid
	 */
	public void fillGrid(List<PDBSearchResult> values)
	{
		store.clear();
		selectedPdbCode = null;

		if (values != null) {
			store.addAll(values);
		}
		
		if(store.getAll().size() <= 0){
			setGridVisibility(false);
		} else{
			setGridVisibility(true);
		}
		
		resultsGrid.reconfigure(store, cm);
	}

	
	private List<ColumnConfig<PDBSearchResult, ?>> createColumnConfigs(){
		List<ColumnConfig<PDBSearchResult, ?>> resultsConfig;
		
		ColumnConfig<PDBSearchResult, String> pdbCol = new ColumnConfig<PDBSearchResult, String>(props.pdbCode());
		ColumnConfig<PDBSearchResult, String> titleCol = new ColumnConfig<PDBSearchResult, String>(props.title());
		ColumnConfig<PDBSearchResult, String> spaceGroupCol = new ColumnConfig<PDBSearchResult, String>(props.spaceGroup());
		ColumnConfig<PDBSearchResult, String> expMethodCol = new ColumnConfig<PDBSearchResult, String>(props.expMethod());
		ColumnConfig<PDBSearchResult, Double> resolutionCol = new ColumnConfig<PDBSearchResult, Double>(props.resolution());
		ColumnConfig<PDBSearchResult, Double> rFreeCol = new ColumnConfig<PDBSearchResult, Double>(props.rfreeValue());
		
		pdbCol.setCell(new PdbCodeCell());
		resolutionCol.setCell(new PdbDataDoubleCell());
		rFreeCol.setCell(new PdbDataDoubleCell());
		
		fillColumnSettings(pdbCol, "pdbCode");
		fillColumnSettings(titleCol, "title");
		fillColumnSettings(spaceGroupCol, "spaceGroup");
		fillColumnSettings(expMethodCol, "expMethod");
		fillColumnSettings(resolutionCol, "res");
		fillColumnSettings(rFreeCol, "rfree");
		
		expMethodCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		spaceGroupCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		resolutionCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rFreeCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		resultsConfig = new ArrayList<ColumnConfig<PDBSearchResult, ?>>();
		resultsConfig.add(pdbCol);
		resultsConfig.add(titleCol);
		resultsConfig.add(expMethodCol);
		resultsConfig.add(spaceGroupCol);
		resultsConfig.add(resolutionCol);
		resultsConfig.add(rFreeCol);
		
		return resultsConfig;
	}
	
	/**
	 * Fills in the column with following settings:
	 * width - taken from grid.properties
	 * header - taken from grid.properties
	 * @param column
	 * @param type
	 */
	private void fillColumnSettings(ColumnConfig<?, ?> column, String type){
		column.setColumnHeaderClassName("eppic-default-font");
		column.setWidth(Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("search_"+type+"_width")));
		column.setHeader(EscapedStringGenerator.generateSafeHtml(
				ApplicationContext.getSettings().getGridProperties().get("search_"+type+"_header")));
		
	}
	
	private void createGrid(){
		resultsGrid = new Grid<PDBSearchResult>(store, cm);

		resultsGrid.setBorders(false);
		resultsGrid.getView().setStripeRows(true);
		resultsGrid.getView().setColumnLines(true);
		resultsGrid.getView().setForceFit(true);
		resultsGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		resultsGrid.addStyleName("eppic-default-font");
		
		resultsGrid.addCellClickHandler(new CellClickHandler() {
			@Override
			public void onCellClick(CellClickEvent event) {
				selectedPdbCode = store.get(event.getRowIndex()).getPdbCode();	
			}
		});
		
		resultsGrid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
			
			@Override
			public void onCellClick(CellDoubleClickEvent event) {
				selectedPdbCode = store.get(event.getRowIndex()).getPdbCode();
				if(selectedPdbCode != null)
				{
					History.newItem("id/" + selectedPdbCode.toLowerCase() );
				}
			}
		});

		resultsGrid.getSelectionModel().addSelectionHandler(new SelectionHandler<PDBSearchResult>() {
			@Override
			public void onSelection(SelectionEvent<PDBSearchResult> event) {
				if(event.getSelectedItem() != null )
				{
						selectedPdbCode = event.getSelectedItem().getPdbCode();
				}	
			}
		});

		new KeyNav(resultsGrid)
		{
			@Override
            public void onEnter(NativeEvent event)
			{
				if(selectedPdbCode != null)
				{
					History.newItem("id/" + selectedPdbCode.toLowerCase() );
				}
			}
		};

		NumericFilter<PDBSearchResult, Double> resFilter = 
				new NumericFilter<PDBSearchResult, Double>(props.resolution(), new DoublePropertyEditor());
		NumericFilter<PDBSearchResult, Double> rFreeFilter = 
				new NumericFilter<PDBSearchResult, Double>(props.rfreeValue(), new DoublePropertyEditor());

		StringFilter<PDBSearchResult> pdbFilter = new StringFilter<PDBSearchResult>(props.pdbCode());
		StringFilter<PDBSearchResult> titleFilter = new StringFilter<PDBSearchResult>(props.title());
		StringFilter<PDBSearchResult> expMethodFilter = new StringFilter<PDBSearchResult>(props.expMethod());
		StringFilter<PDBSearchResult> spaceGroupFilter = new StringFilter<PDBSearchResult>(props.spaceGroup());

		GridFilters<PDBSearchResult> filters = new GridFilters<PDBSearchResult>();
		filters.initPlugin(resultsGrid);
		filters.setLocal(true);
		filters.addFilter(pdbFilter);
		filters.addFilter(titleFilter);
		filters.addFilter(expMethodFilter);
		filters.addFilter(spaceGroupFilter);
		filters.addFilter(resFilter);
		filters.addFilter(rFreeFilter);
	}
	
	private VerticalLayoutContainer createFilterPanel(){
		VerticalLayoutContainer filterPanel = new VerticalLayoutContainer();
		filterPanel.addStyleName("eppic-search-panel-filter-panel");
		
		HTML header = new HTML(AppPropertiesManager.CONSTANTS.search_panel_filter_header());
		header.addStyleName("eppic-filter-panel-header");
		
		HTML text = new HTML(AppPropertiesManager.CONSTANTS.search_panel_filter_text());
		
		filterPanel.add(header);
		filterPanel.add(text);
		
		return filterPanel;
	}
	
	private ToolBar createSelectorToolBar(){
		ToolBar tb = new ToolBar();

		TextButton loadButton = new TextButton(AppPropertiesManager.CONSTANTS.search_panel_load_button());
		loadButton.addStyleName("eppic-default-font");
		loadButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				if(selectedPdbCode != null){
					History.newItem("id/" + selectedPdbCode.toLowerCase() );
				} else{
					PopUpInfo.show(AppPropertiesManager.CONSTANTS.search_panel_load_button_no_selection_title(),
							AppPropertiesManager.CONSTANTS.search_panel_load_button_no_selection_text());
				}
			}
		});
		
		tb.add(loadButton);
		
		return tb;
	}
	
	private void setGridVisibility(boolean visibility){
		resultsGrid.setVisible(visibility);
		selectorToolBar.setVisible(visibility);
		filterTextPanel.setVisible(visibility);
	}
	
	/**
	 * Adjusts size of the grid
	 */
	public void resizeGrid(int width)
	{		
		this.setWidth(width);
		resultsGrid.clearSizeCache();
		resultsGrid.getView().refresh(true);
	}
}
