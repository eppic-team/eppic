package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.shared.model.Residue;
import ch.systemsx.sybit.crkwebui.shared.model.ResidueType;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScore;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;

import eppic.model.ScoringMethod;

/**
 * Panel used to display the residues summary for one structure.
 * @author srebniak_a; biyani_n
 *
 */
public class ResiduesSummaryPanel extends CssFloatLayoutContainer
{
	
	private FlexTable summaryTable;
	
	private double entropyRatioValue = Double.NaN;
	private double entropyZscore = Double.NaN;	
	private int coreSize = 0;
	private int rimSize = 0;
	
	private static final NumberFormat number = NumberFormat.getFormat("0.00");
	
	private int structure;
	
	public ResiduesSummaryPanel(int side) 
	{
		this.structure = side;
    	this.setScrollMode(ScrollMode.AUTO);
		
    	summaryTable = new FlexTable();
    	summaryTable.setCellPadding(0);
    	summaryTable.setCellSpacing(0);
    	summaryTable.addStyleName("eppic-residues-summary-table");
		
		this.add(summaryTable);
		
		fillTableHeadings();
	}
	
	/**
	 * Method to fill in the details of the panel
	 * @param pdbScoreItem
	 * @param selectedInterfaceId
	 * @param residues
	 */
	public void fillResultsSummary(PdbInfo pdbScoreItem,
			 int selectedInterfaceId,
			 List<Residue> residues){
		calculateData(pdbScoreItem, selectedInterfaceId, residues);
		
		summaryTable.setWidget(0, 1, new Label(Integer.toString(coreSize)));
		summaryTable.setWidget(1, 1, new Label(Integer.toString(rimSize)));
		summaryTable.setWidget(2, 1, new Label(number.format(entropyRatioValue)));
		summaryTable.setWidget(3, 1, new Label(number.format(entropyZscore)));
		
	}

	/**
	 * Sets content of tables.
	 */
	private void fillTableHeadings(){
		
		LabelWithTooltip coresHead = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_cores(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_cores_hint());
		coresHead.addStyleName("eppic-residues-summary-fields");
		summaryTable.setWidget(0, 0, coresHead);
		
		LabelWithTooltip rimHead = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_rims(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_rims_hint());
		rimHead.addStyleName("eppic-residues-summary-fields");
		summaryTable.setWidget(1, 0, rimHead);
		
		LabelWithTooltip crHead = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_final(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_final_hint());
		crHead.addStyleName("eppic-residues-summary-fields");
		summaryTable.setWidget(2, 0, crHead);
		
		LabelWithTooltip csHead = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_final(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_final_hint());
		csHead.addStyleName("eppic-residues-summary-fields");
		summaryTable.setWidget(3, 0, csHead);
	}
	
	private void calculateData(PdbInfo pdbScoreItem,
			int selectedInterfaceId,
			List<Residue> residues) {

		int interfId = selectedInterfaceId;
		
		entropyRatioValue = Double.NaN;
		entropyZscore = Double.NaN;
		
		coreSize = 0;
		rimSize = 0;

		for (InterfaceScore scoreItem : pdbScoreItem.getInterface(interfId).getInterfaceScores())
		{
			if(scoreItem.getMethod().equals(ScoringMethod.EPPIC_CORERIM))
			{
				if(structure == 1)
				{
					entropyRatioValue = scoreItem.getScore1();
				}
				else
				{
					entropyRatioValue = scoreItem.getScore2();
				}
			}
			else if (scoreItem.getMethod().equals(ScoringMethod.EPPIC_CORESURFACE))
			{
				if (structure == 1)
				{
					entropyZscore = scoreItem.getScore1();
				} 
				else 
				{
					entropyZscore = scoreItem.getScore2();					
				}

			}
		}

		for(Residue interfResItem : residues) {

			if ((interfResItem.getRegion() == ResidueType.CORE_EVOLUTIONARY.getRegion()) ||
					(interfResItem.getRegion() == ResidueType.CORE_GEOMETRY.getRegion()))
			{
				coreSize++;
			}
			else if (interfResItem.getRegion() == ResidueType.RIM.getRegion()) 
			{
				rimSize++;
			}
		}

	}

	/**
	 * Cleans content of residues summary grid.
	 */
	public void cleanResiduesGrid()
	{
		summaryTable.setWidget(0, 1, new Label(" "));
		summaryTable.setWidget(1, 1, new Label(" "));
		summaryTable.setWidget(2, 1, new Label(" "));
		summaryTable.setWidget(3, 1, new Label(" "));
	}
	
	
}
