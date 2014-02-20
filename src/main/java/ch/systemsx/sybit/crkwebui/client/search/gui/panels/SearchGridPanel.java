package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalDoubleCell;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
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
	private final Grid<PDBSearchResult> resultsGrid;

	/**
	 * Constructor
	 */
	public SearchGridPanel(){

		this.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				resizeGrid();

			}
		});

		store = new ListStore<PDBSearchResult>(props.key());

		ColumnConfig<PDBSearchResult, String> pdbCol = new ColumnConfig<PDBSearchResult, String>(props.pdbCode(), 50, "PDB Code");
		ColumnConfig<PDBSearchResult, String> titleCol = new ColumnConfig<PDBSearchResult, String>(props.title(), 200, "Title");
		ColumnConfig<PDBSearchResult, String> spaceGroupCol = new ColumnConfig<PDBSearchResult, String>(props.spaceGroup(), 80, "Space Group");
		ColumnConfig<PDBSearchResult, String> expMethodCol = new ColumnConfig<PDBSearchResult, String>(props.expMethod(), 100, "Experimental Method");
		ColumnConfig<PDBSearchResult, Double> resolutionCol = new ColumnConfig<PDBSearchResult, Double>(props.resolution(), 50, "Resolution");
		ColumnConfig<PDBSearchResult, Double> rFreeCol = new ColumnConfig<PDBSearchResult, Double>(props.rfreeValue(), 50, "R-Free");
		resolutionCol.setCell(new TwoDecimalDoubleCell());
		rFreeCol.setCell(new TwoDecimalDoubleCell());

		resultsConfig = new ArrayList<ColumnConfig<PDBSearchResult, ?>>();
		resultsConfig.add(pdbCol);
		resultsConfig.add(titleCol);
		resultsConfig.add(expMethodCol);
		resultsConfig.add(spaceGroupCol);
		resultsConfig.add(resolutionCol);
		resultsConfig.add(rFreeCol);

		cm = new ColumnModel<PDBSearchResult>(resultsConfig);

		resultsGrid = new Grid<PDBSearchResult>(store, cm);

		resultsGrid.getView().setAutoExpandColumn(pdbCol);
		resultsGrid.setBorders(false);
		resultsGrid.getView().setStripeRows(true);
		resultsGrid.getView().setColumnLines(true);
		resultsGrid.getView().setForceFit(true);

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

		this.add(resultsGrid, new VerticalLayoutData(1, 1));

	}

	/**
	 * Sets content of grid.
	 * @param values list of items to add to the grid
	 */
	public void fillGrid(List<PDBSearchResult> values)
	{
		store.clear();

		if (values != null) {
			store.addAll(values);
		}
		
		resultsGrid.reconfigure(store, cm);
	}

	/**
	 * Adjusts size of the grid
	 */
	public void resizeGrid()
	{		
		resultsGrid.clearSizeCache();
		resultsGrid.getView().refresh(true);
	}
}
