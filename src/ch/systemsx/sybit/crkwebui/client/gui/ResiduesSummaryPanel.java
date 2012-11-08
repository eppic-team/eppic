package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.gui.util.GridColumnConfigGenerator;
import ch.systemsx.sybit.crkwebui.client.gui.util.GridUtil;
import ch.systemsx.sybit.crkwebui.client.model.InterfaceResidueSummaryModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueType;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
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
	private List<Integer> initialColumnWidth;
	
	private int structure;
	
	private boolean useBufferedView = false;
	
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

		residuesStore = new ListStore<InterfaceResidueSummaryModel>();
		residuesColumnModel = new ColumnModel(residuesConfigs);
		
		residuesGrid = new Grid<InterfaceResidueSummaryModel>(residuesStore, residuesColumnModel);
		residuesGrid.setBorders(false);
		residuesGrid.setStripeRows(true);
		residuesGrid.setColumnLines(false);
		residuesGrid.setHideHeaders(true);
		residuesGrid.getSelectionModel().setLocked(true);
		residuesGrid.setLoadMask(true);
		
		residuesGrid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) 
			{
				return "eppic-summary-grid-row";
			}
		});
		
		residuesGrid.disableTextSelection(false);
		residuesGrid.getView().setForceFit(false);
		this.add(residuesGrid);
	}
	
	/**
	 * Creates columns configurations for residues summary grid.
	 * @return columns configurations for residues summary grid
	 */
	private List<ColumnConfig> createColumnConfig() 
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs(
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
			
			if (interfResItem.getAssignment() == InterfaceResidueType.CORE.getAssignment()) coreSize++;
			else if (interfResItem.getAssignment() == InterfaceResidueType.RIM.getAssignment()) rimSize++;
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
		
		residuesStore.add(interfaceSummaryItems);
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
		int scoresGridWidthOfAllVisibleColumns = GridUtil.calculateWidthOfVisibleColumns(residuesGrid, initialColumnWidth) + 10;
		
		if(useBufferedView)
		{
			scoresGridWidthOfAllVisibleColumns += 20;
		}

//		int assignedWidth = (int)((mainController.getMainViewPort().getInterfacesResiduesWindow().getWindowWidth() - 30) * 0.48);
		
		int nrOfColumn = residuesGrid.getColumnModel().getColumnCount();
		
		if (GridUtil.checkIfForceFit(scoresGridWidthOfAllVisibleColumns, 
									 assignedWidth)) 
		{
			float gridWidthMultiplier = (float)assignedWidth / scoresGridWidthOfAllVisibleColumns;
			
			for (int i = 0; i < nrOfColumn; i++) 
			{
				residuesGrid.getColumnModel().setColumnWidth(i, (int)(initialColumnWidth.get(i) * gridWidthMultiplier), true);
			}
		} 
		else 
		{
			for (int i = 0; i < nrOfColumn; i++) 
			{
				residuesGrid.getColumnModel().getColumn(i).setWidth(initialColumnWidth.get(i));
			}
			
			assignedWidth = scoresGridWidthOfAllVisibleColumns;
		}
		
		residuesGrid.setWidth(assignedWidth);
		this.setWidth(assignedWidth + 10);
		
//		if(useBufferedView)
		{
			residuesGrid.getView().refresh(true);
		}
		
		this.layout();
	}
}
