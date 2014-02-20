package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalDoubleCell;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

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

	private RpcProxy<FilterPagingLoadConfig, PagingLoadResult<PDBSearchResult>> proxy;
	private final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<PDBSearchResult>> remoteLoader;
	private PagingToolBar pagingToolbar;

	private int resultsPerPage = 25;
	
	/**
	 * Constructor
	 */
	public SearchGridPanel(final String uniProtId){
		
		store = new ListStore<PDBSearchResult>(props.key());

		proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<PDBSearchResult>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig,
					AsyncCallback<PagingLoadResult<PDBSearchResult>> callback) {
				CrkWebServiceProvider.getServiceController().getListOfPDBsHavingAUniProt(loadConfig, uniProtId);
				
			}
		};

		remoteLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<PDBSearchResult>>(proxy) {
			@Override
			protected FilterPagingLoadConfig newLoadConfig() {
				return new FilterPagingLoadConfigBean();
			}
		};

		remoteLoader.setRemoteSort(true);
		remoteLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, PDBSearchResult, PagingLoadResult<PDBSearchResult>>(store));

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

		resultsGrid = new Grid<PDBSearchResult>(store, cm) {
			@Override
			protected void onAfterFirstAttach() {
				super.onAfterFirstAttach();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						remoteLoader.load();
					}
				});
			}
		};
		
		resultsGrid.setLoader(remoteLoader);
		resultsGrid.getView().setAutoExpandColumn(pdbCol);
		resultsGrid.setBorders(false);
		resultsGrid.getView().setStripeRows(true);
		resultsGrid.getView().setColumnLines(true);

		NumericFilter<PDBSearchResult, Double> resFilter = 
				new NumericFilter<PDBSearchResult, Double>(props.resolution(), new DoublePropertyEditor());
		NumericFilter<PDBSearchResult, Double> rFreeFilter = 
				new NumericFilter<PDBSearchResult, Double>(props.rfreeValue(), new DoublePropertyEditor());
		
		StringFilter<PDBSearchResult> pdbFilter = new StringFilter<PDBSearchResult>(props.pdbCode());
		StringFilter<PDBSearchResult> titleFilter = new StringFilter<PDBSearchResult>(props.title());
		StringFilter<PDBSearchResult> expMethodFilter = new StringFilter<PDBSearchResult>(props.expMethod());
		StringFilter<PDBSearchResult> spaceGroupFilter = new StringFilter<PDBSearchResult>(props.spaceGroup());

		GridFilters<PDBSearchResult> filters = new GridFilters<PDBSearchResult>(remoteLoader);
		filters.initPlugin(resultsGrid);
		filters.addFilter(pdbFilter);
		filters.addFilter(titleFilter);
		filters.addFilter(expMethodFilter);
		filters.addFilter(spaceGroupFilter);
		filters.addFilter(resFilter);
		filters.addFilter(rFreeFilter);

		pagingToolbar = new PagingToolBar(resultsPerPage);
		pagingToolbar.bind(remoteLoader);

		this.add(resultsGrid, new VerticalLayoutData(1, 1));
		this.add(pagingToolbar, new VerticalLayoutData(1, -1));

	}
}
