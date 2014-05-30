package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.search.gui.cells.PdbCodeCell;
import ch.systemsx.sybit.crkwebui.client.search.gui.cells.PdbDataDoubleCell;
import ch.systemsx.sybit.crkwebui.client.search.gui.cells.SequenceClusterTypeCell;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import ch.systemsx.sybit.crkwebui.shared.model.SequenceClusterType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;

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
	ValueProvider<PDBSearchResult, SequenceClusterType> sequenceClusterType();
	ValueProvider<PDBSearchResult, String> pdbCode();	  
	ValueProvider<PDBSearchResult, String> title();
	ValueProvider<PDBSearchResult, String> spaceGroup();
	ValueProvider<PDBSearchResult, Double> cellA();
	ValueProvider<PDBSearchResult, Double> cellB();
	ValueProvider<PDBSearchResult, Double> cellC();
	ValueProvider<PDBSearchResult, Double> cellAlpha();
	ValueProvider<PDBSearchResult, Double> cellBeta();
	ValueProvider<PDBSearchResult, Double> cellGamma();
	ValueProvider<PDBSearchResult, Double> resolution();
	ValueProvider<PDBSearchResult, Double> rfreeValue();
	ValueProvider<PDBSearchResult, String> expMethod();
	ValueProvider<PDBSearchResult, Integer> crystalFormId();
    }

    private static final PDBSearchResultProperties props = GWT.create(PDBSearchResultProperties.class);

    private List<ColumnConfig<PDBSearchResult, ?>> resultsConfig;
    private ListStore<PDBSearchResult> store;
    private ColumnModel<PDBSearchResult> cm;

    private Grid<PDBSearchResult> resultsGrid;
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

	filterTextPanel = createFilterPanel();

	this.add(filterTextPanel, new VerticalLayoutData(1, -1, new Margins(10,0,15,0)));
	this.add(resultsGrid, new VerticalLayoutData(1, 1));

    }

    /**
     * Sets content of grid.
     * @param values list of items to add to the grid
     * @param pdBCode 
     */
    public void fillGrid(List<PDBSearchResult> values, String pdBCode)
    {
	moveOriginaltoTop(values, pdBCode);
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
	highlightOriginal(pdBCode);
	resultsGrid.reconfigure(store, cm);
    }

    private void highlightOriginal(final String pdBCode) {
	resultsGrid.getView().setViewConfig(new GridViewConfig<PDBSearchResult>() {
	    @Override
	    public String getColStyle(PDBSearchResult model, ValueProvider valueProvider, int rowIndex, int colIndex) {
		return "";
	    }

	    @Override
	    public String getRowStyle(PDBSearchResult model, int rowIndex) {
		if(model.getPdbCode().equals(pdBCode))
		    return "searchOriginal";
		return "";
	    }
	});
    }

    private void moveOriginaltoTop(List<PDBSearchResult> values, String pdBCode) {
	for(int i = 0; i < values.size(); i++) 
	    if(values.get(i).getPdbCode().equals(pdBCode)) {
		PDBSearchResult p = values.get(i);
		values.remove(i);
		values.add(0,p);
		break;
	    }
    }




    private List<ColumnConfig<PDBSearchResult, ?>> createColumnConfigs(){
	List<ColumnConfig<PDBSearchResult, ?>> resultsConfig;

	ColumnConfig<PDBSearchResult, SequenceClusterType> sequenceClusterTypeCol = new ColumnConfig<PDBSearchResult, SequenceClusterType>(props.sequenceClusterType());
	ColumnConfig<PDBSearchResult, String> pdbCol = new ColumnConfig<PDBSearchResult, String>(props.pdbCode());
	ColumnConfig<PDBSearchResult, String> titleCol = new ColumnConfig<PDBSearchResult, String>(props.title());
	ColumnConfig<PDBSearchResult, String> spaceGroupCol = new ColumnConfig<PDBSearchResult, String>(props.spaceGroup());
	ColumnConfig<PDBSearchResult, Double> cellACol = new ColumnConfig<PDBSearchResult, Double>(props.cellA());
	ColumnConfig<PDBSearchResult, Double> cellBCol = new ColumnConfig<PDBSearchResult, Double>(props.cellB());
	ColumnConfig<PDBSearchResult, Double> cellCCol = new ColumnConfig<PDBSearchResult, Double>(props.cellC());
	ColumnConfig<PDBSearchResult, Double> cellAlphaCol = new ColumnConfig<PDBSearchResult, Double>(props.cellAlpha());
	ColumnConfig<PDBSearchResult, Double> cellBetaCol = new ColumnConfig<PDBSearchResult, Double>(props.cellBeta());
	ColumnConfig<PDBSearchResult, Double> cellGammaCol = new ColumnConfig<PDBSearchResult, Double>(props.cellGamma());
	ColumnConfig<PDBSearchResult, String> expMethodCol = new ColumnConfig<PDBSearchResult, String>(props.expMethod());
	ColumnConfig<PDBSearchResult, Double> resolutionCol = new ColumnConfig<PDBSearchResult, Double>(props.resolution());
	ColumnConfig<PDBSearchResult, Double> rFreeCol = new ColumnConfig<PDBSearchResult, Double>(props.rfreeValue());
	ColumnConfig<PDBSearchResult, Integer> crystalFormIdCol = new ColumnConfig<PDBSearchResult, Integer>(props.crystalFormId());

	sequenceClusterTypeCol.setCell(new SequenceClusterTypeCell());
	pdbCol.setCell(new PdbCodeCell());
	cellACol.setCell(new PdbDataDoubleCell());
	cellBCol.setCell(new PdbDataDoubleCell());
	cellCCol.setCell(new PdbDataDoubleCell());
	cellAlphaCol.setCell(new PdbDataDoubleCell());
	cellBetaCol.setCell(new PdbDataDoubleCell());
	cellGammaCol.setCell(new PdbDataDoubleCell());
	resolutionCol.setCell(new PdbDataDoubleCell());
	rFreeCol.setCell(new PdbDataDoubleCell());

	fillColumnSettings(sequenceClusterTypeCol, "sequenceClusterType");
	fillColumnSettings(pdbCol, "pdbCode");
	fillColumnSettings(titleCol, "title");
	fillColumnSettings(spaceGroupCol, "spaceGroup");
	fillColumnSettings(cellACol, "cellA");
	fillColumnSettings(cellBCol,"cellB");
	fillColumnSettings(cellCCol, "cellC");
	fillColumnSettings(cellAlphaCol, "cellAlpha");
	fillColumnSettings(cellBetaCol, "cellBeta");		
	fillColumnSettings(cellGammaCol, "cellGamma");
	fillColumnSettings(expMethodCol, "expMethod");
	fillColumnSettings(resolutionCol, "res");
	fillColumnSettings(rFreeCol, "rfree");
	fillColumnSettings(crystalFormIdCol, "crystalFormId");

	sequenceClusterTypeCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	expMethodCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	spaceGroupCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	cellACol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	cellBCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	cellCCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	cellAlphaCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	cellBetaCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	cellGammaCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	resolutionCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	rFreeCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	crystalFormIdCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);

	resultsConfig = new ArrayList<ColumnConfig<PDBSearchResult, ?>>();
	resultsConfig.add(sequenceClusterTypeCol);
	resultsConfig.add(pdbCol);
	resultsConfig.add(titleCol);
	resultsConfig.add(expMethodCol);
	resultsConfig.add(spaceGroupCol);
	resultsConfig.add(cellACol);
	resultsConfig.add(cellBCol);
	resultsConfig.add(cellCCol);
	resultsConfig.add(cellAlphaCol);
	resultsConfig.add(cellBetaCol);
	resultsConfig.add(cellGammaCol);
	resultsConfig.add(resolutionCol);
	resultsConfig.add(rFreeCol);
	resultsConfig.add(crystalFormIdCol);

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
	resultsGrid.addStyleName("eppic-default-font");
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

    private void setGridVisibility(boolean visibility){
	resultsGrid.setVisible(visibility);
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
