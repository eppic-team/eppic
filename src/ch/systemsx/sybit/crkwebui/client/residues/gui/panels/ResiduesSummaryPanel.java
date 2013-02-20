package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.renderers.GridCellRendererFactoryImpl;
import ch.systemsx.sybit.crkwebui.client.commons.gui.util.GridColumnConfigGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.gui.util.GridResizer;
import ch.systemsx.sybit.crkwebui.client.residues.data.InterfaceResidueSummaryModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueType;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Panel used to display the residues summary for one structure.
 * @author srebniak_a
 *
 */
public class ResiduesSummaryPanel extends ContentPanel
{
	private List<ColumnConfig> residuesConfigs;
	private ListStore<InterfaceResidueSummaryModel> residuesStore;
	private ColumnModel residuesColumnModel;
	private Grid<InterfaceResidueSummaryModel> residuesGrid;
	private GridResizer gridResizer;
	private List<Integer> initialColumnWidth;
	
	private MemoryProxy proxy;
	private BaseListLoader loader;
	private boolean useBufferedView = false;
	
	private int structure;
	
	public ResiduesSummaryPanel(
						 String header, 
						 int structure) 
	{
		if(GXT.isIE8)
		{
			useBufferedView = true;
		}
		
		this.structure = structure;
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.getHeader().setVisible(false);
		this.setScrollMode(Scroll.NONE);

		residuesConfigs = createColumnConfig();
		
		proxy = new MemoryProxy(null);
		loader = new BaseListLoader(proxy);

		residuesStore = new ListStore<InterfaceResidueSummaryModel>(loader);
		residuesColumnModel = new ColumnModel(residuesConfigs);
		
		residuesGrid = createResiduesSummaryGrid();
		this.add(residuesGrid);
		
		gridResizer = new GridResizer(residuesGrid, initialColumnWidth, useBufferedView, false);
	}
	
	/**
	 * Creates columns configurations for residues summary grid.
	 * @return columns configurations for residues summary grid
	 */
	private List<ColumnConfig> createColumnConfig() 
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs(
				   GridCellRendererFactoryImpl.getInstance(),
				   "residues_summary",
				   new InterfaceResidueSummaryModel());

		if(configs != null)
		{
			initialColumnWidth = new ArrayList<Integer>();
			
			for(ColumnConfig columnConfig : configs)
			{
				initialColumnWidth.add(columnConfig.getWidth());
			}
		}
		
		return configs;

	}
	
	/**
	 * Creates grid containing summary for specified interface and structure.
	 * @return summary grid
	 */
	private Grid<InterfaceResidueSummaryModel> createResiduesSummaryGrid()
	{
		Grid<InterfaceResidueSummaryModel> residuesGrid = new Grid<InterfaceResidueSummaryModel>(residuesStore, residuesColumnModel);
		residuesGrid.setBorders(false);
		residuesGrid.setStripeRows(true);
		residuesGrid.setColumnLines(false);
		residuesGrid.setHideHeaders(true);
		residuesGrid.getSelectionModel().setLocked(true);
		residuesGrid.setLoadMask(true);
		residuesGrid.disableTextSelection(false);
		
		residuesGrid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) 
			{
				return "eppic-summary-grid-row";
			}
		});
		
		residuesGrid.getView().setForceFit(false);

		return residuesGrid;
	}

	/**
	 * Sets content of residues summary grid.
	 */
	public void fillResiduesGrid(PDBScoreItem pdbScoreItem,
								 int selectedInterfaceId,
								 List<InterfaceResidueItem> interfaceResidueItems) 
	{
		residuesStore.removeAll();
		
		List<InterfaceResidueSummaryModel> interfaceSummaryItems = new ArrayList<InterfaceResidueSummaryModel>();

		NumberFormat number = NumberFormat.getFormat("0.00");
		
		double entropyCoreValue = Double.NaN;
		double entropyRimValue = Double.NaN;
		double entropyRatioValue = Double.NaN;
		double entropySurfSamplingMean = Double.NaN;
		double entropySurfSamplingSd = Double.NaN;
		double entropyZscore = Double.NaN;
		
		int interfId = selectedInterfaceId - 1;
		
		for (InterfaceScoreItem scoreItem : pdbScoreItem.getInterfaceItem(interfId).getInterfaceScores())
		{
			if(scoreItem.getMethod().equals("Entropy"))
			{
				if(structure == 1)
				{
					entropyCoreValue = scoreItem.getUnweightedCore1Scores();
					entropyRimValue = scoreItem.getUnweightedRim1Scores();
					entropyRatioValue = scoreItem.getUnweightedRatio1Scores();
				}
				else
				{
					entropyCoreValue = scoreItem.getUnweightedCore2Scores();
					entropyRimValue = scoreItem.getUnweightedRim2Scores();
					entropyRatioValue = scoreItem.getUnweightedRatio2Scores();
				}
			}
			else if (scoreItem.getMethod().equals("Z-scores"))
			{
				if (structure == 1)
				{
					entropySurfSamplingMean = scoreItem.getUnweightedCore1Scores();
					entropySurfSamplingSd = scoreItem.getUnweightedRim1Scores();
					entropyZscore = scoreItem.getUnweightedRatio1Scores();
				} 
				else 
				{
					entropySurfSamplingMean = scoreItem.getUnweightedCore2Scores();
					entropySurfSamplingSd = scoreItem.getUnweightedRim2Scores();
					entropyZscore = scoreItem.getUnweightedRatio2Scores();					
				}
				
			}
		}
		
		int coreSize = 0;
		int rimSize = 0;
		
		for(InterfaceResidueItem interfResItem : interfaceResidueItems) {
//		for (InterfaceResidueItem interfResItem:mainController.getInterfaceResiduesItemsList().get(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface()).get(structure)) {
			
			if ((interfResItem.getAssignment() == InterfaceResidueType.CORE_EVOLUTIONARY.getAssignment()) ||
				(interfResItem.getAssignment() == InterfaceResidueType.CORE_GEOMETRY.getAssignment()))
			{
				coreSize++;
			}
			else if (interfResItem.getAssignment() == InterfaceResidueType.RIM.getAssignment()) 
			{
				rimSize++;
			}
		}
	
		InterfaceResidueSummaryModel model = new InterfaceResidueSummaryModel();
		model.setTitle(AppPropertiesManager.CONSTANTS.interfaces_residues_aggergation_total_cores()+" ("+coreSize+")");
		
		double asa = 0;
		double bsa = 0;
		
		if(structure == 1)
		{
			asa = pdbScoreItem.getInterfaceItem(interfId).getAsaC1();
			bsa = pdbScoreItem.getInterfaceItem(interfId).getBsaC1();
		}
		else
		{
			asa = pdbScoreItem.getInterfaceItem(interfId).getAsaC2();
			bsa = pdbScoreItem.getInterfaceItem(interfId).getBsaC2();
		}
		
		model.setAsa(asa);
		model.setBsa(bsa);
		model.setEntropyScore(number.format(entropyCoreValue));
		
		interfaceSummaryItems.add(model);
		
		
		model = new InterfaceResidueSummaryModel();
		model.setTitle(AppPropertiesManager.CONSTANTS.interfaces_residues_aggergation_total_rims()+" ("+rimSize+")");
		
		asa = 0;
		bsa = 0;
		
		if(structure == 1)
		{
			asa = pdbScoreItem.getInterfaceItem(interfId).getAsaR1();
			bsa = pdbScoreItem.getInterfaceItem(interfId).getBsaR1();
		}
		else
		{
			asa = pdbScoreItem.getInterfaceItem(interfId).getAsaR2();
			bsa = pdbScoreItem.getInterfaceItem(interfId).getBsaR2();
		}
		
		model.setAsa(asa);
		model.setBsa(bsa);
		model.setEntropyScore(number.format(entropyRimValue));
		
		interfaceSummaryItems.add(model);
		
		model = new InterfaceResidueSummaryModel();
		model.setTitle(AppPropertiesManager.CONSTANTS.interfaces_residues_aggergation_surface());
		model.setEntropyScore(number.format(entropySurfSamplingMean)+", "+number.format(entropySurfSamplingSd));
		
		interfaceSummaryItems.add(model);
		
		
		model = new InterfaceResidueSummaryModel();
		model.setTitle(AppPropertiesManager.CONSTANTS.interfaces_residues_aggergation_ratios());
		model.setEntropyScore(number.format(entropyRatioValue)+", "+number.format(entropyZscore));
		
		interfaceSummaryItems.add(model);
		
//		residuesStore.add(interfaceSummaryItems);
		proxy.setData(interfaceSummaryItems);
		loader.load();
	}
	
	/**
	 * Cleans content of residues summary grid.
	 */
	public void cleanResiduesGrid()
	{
		residuesStore.removeAll();
	}
	
	/**
	 * Adjusts size of the residues summary grid based on the size of the screen
	 * and initial settings for the grid. 
	 * @param assignedWidth width assigned for the grid
	 */
	public void resizeGrid(int assignedWidth) 
	{
		gridResizer.resize(assignedWidth - 2);
		this.setWidth(residuesGrid.getWidth() + 2);
		
		loader.load();
		residuesGrid.getView().refresh(true);
	}
}
