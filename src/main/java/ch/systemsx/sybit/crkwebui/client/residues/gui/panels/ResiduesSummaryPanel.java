package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueType;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.FieldSet;

/**
 * Panel used to display the residues summary for one structure.
 * @author srebniak_a; biyani_n
 *
 */
public class ResiduesSummaryPanel extends HorizontalLayoutContainer
{
	private FlexTable coreRimScoreTable;
	private FlexTable coreSurfaceScoreTable;
	private FlexTable sizesTable;
	
	private double entropyCoreValue = Double.NaN;
	private double entropyRimValue = Double.NaN;
	private double entropyRatioValue = Double.NaN;
	private double entropySurfSamplingMean = Double.NaN;
	private double entropySurfSamplingSd = Double.NaN;
	private double entropyZscore = Double.NaN;
	
	private int coreSize = 0;
	private int rimSize = 0;
	
	private static final NumberFormat number = NumberFormat.getFormat("0.00");
	
	private int structure;
	
	public ResiduesSummaryPanel(int structure) 
	{
		this.structure = structure;
		this.setBorders(false);
		
		sizesTable = new FlexTable();
		sizesTable.addStyleName("eppic-residues-summary-table");
		sizesTable.setWidth("110px");
		FieldSet sizesFS = new FieldSet();
		sizesFS.setHeadingHtml(StyleGenerator.defaultFontStyleString(
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_heading()));
		VerticalLayoutContainer sizesContainer = new VerticalLayoutContainer();
		sizesContainer.setPixelSize(110, 55);
		sizesFS.setWidget(sizesContainer);
		sizesFS.addStyleName("eppic-rounded-border");
		sizesFS.addStyleName("eppic-residues-summary");
		sizesContainer.add(sizesTable);
		this.add(sizesFS, new HorizontalLayoutData(-1,1, new Margins(0, 5, 0, 5)));
		
		coreRimScoreTable = new FlexTable();
		coreRimScoreTable.addStyleName("eppic-residues-summary-table");
		coreRimScoreTable.setWidth("140px");
		FieldSet coreRimScoreFS = new FieldSet();
		coreRimScoreFS.setHeadingHtml(StyleGenerator.defaultFontStyleString(
						AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_heading()));
		VerticalLayoutContainer coreRimScoreContainer = new VerticalLayoutContainer();
		coreRimScoreContainer.setPixelSize(140, 55);
		coreRimScoreFS.setWidget(coreRimScoreContainer);
		coreRimScoreFS.addStyleName("eppic-rounded-border");
		coreRimScoreFS.addStyleName("eppic-residues-summary");
		coreRimScoreContainer.add(coreRimScoreTable);
		this.add(coreRimScoreFS, new HorizontalLayoutData(-1,1, new Margins(0, 5, 0, 0)));
		
		
		coreSurfaceScoreTable = new FlexTable();
		coreSurfaceScoreTable.addStyleName("eppic-residues-summary-table");
		coreSurfaceScoreTable.setWidth("140px");
		FieldSet coreSurfaceScoreFS = new FieldSet();
		coreSurfaceScoreFS.setHeadingHtml(StyleGenerator.defaultFontStyleString(
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_heading()));
		VerticalLayoutContainer coreSurfaceScoreContainer = new VerticalLayoutContainer();
		coreSurfaceScoreContainer.setPixelSize(140, 55);
		coreSurfaceScoreFS.setWidget(coreSurfaceScoreContainer);
		coreSurfaceScoreFS.addStyleName("eppic-rounded-border");
		coreSurfaceScoreFS.addStyleName("eppic-residues-summary");
		coreSurfaceScoreContainer.add(coreSurfaceScoreTable);
		this.add(coreSurfaceScoreFS, new HorizontalLayoutData(-1,1, new Margins(0, 5, 0, 0)));
		
		fillTableHeadings();
	}
	
	/**
	 * Method to fill in the details of the panel
	 * @param pdbScoreItem
	 * @param selectedInterfaceId
	 * @param interfaceResidueItems
	 */
	public void fillResultsSummary(PDBScoreItem pdbScoreItem,
			 int selectedInterfaceId,
			 List<InterfaceResidueItem> interfaceResidueItems){
		calculateData(pdbScoreItem, selectedInterfaceId, interfaceResidueItems);
		
		coreRimScoreTable.setWidget(1, 0, new Label(number.format(entropyCoreValue)));
		coreRimScoreTable.setWidget(1, 1, new Label(number.format(entropyRimValue)));
		coreRimScoreTable.setWidget(1, 2, new Label(number.format(entropyRatioValue)));
		
		coreSurfaceScoreTable.setWidget(1, 0, new Label(number.format(entropySurfSamplingMean)));
		coreSurfaceScoreTable.setWidget(1, 1, new Label(number.format(entropySurfSamplingSd)));
		coreSurfaceScoreTable.setWidget(1, 2, new Label(number.format(entropyZscore)));
		
		sizesTable.setWidget(1, 0, new Label(Integer.toString(coreSize)));
		sizesTable.setWidget(1, 1, new Label(Integer.toString(rimSize)));
	}

	/**
	 * Sets content of tables.
	 */
	private void fillTableHeadings(){		
		
		LabelWithTooltip crstHeadLbl1 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_avgcore(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_avgcore_hint());
		crstHeadLbl1.addStyleName("eppic-residues-summary-fields");
		coreRimScoreTable.setWidget(0, 0, crstHeadLbl1);
		
		LabelWithTooltip crstHeadLbl2 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_avgrim(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_avgrim_hint());
		crstHeadLbl2.addStyleName("eppic-residues-summary-fields");
		coreRimScoreTable.setWidget(0, 1, crstHeadLbl2);
		
		LabelWithTooltip crstHeadLbl3 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_final(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_corerim_final_hint());
		crstHeadLbl3.addStyleName("eppic-residues-summary-fields");
		coreRimScoreTable.setWidget(0, 2, crstHeadLbl3);
		
		
		LabelWithTooltip csstHeadLbl1 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_mean(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_mean_hint());
		csstHeadLbl1.addStyleName("eppic-residues-summary-fields");
		coreSurfaceScoreTable.setWidget(0, 0, csstHeadLbl1);
		
		LabelWithTooltip csstHeadLbl2 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_sd(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_sd_hint());
		csstHeadLbl2.addStyleName("eppic-residues-summary-fields");
		coreSurfaceScoreTable.setWidget(0, 1, csstHeadLbl2);
		
		LabelWithTooltip csstHeadLbl3 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_final(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_coresurface_final_hint());
		csstHeadLbl3.addStyleName("eppic-residues-summary-fields");
		coreSurfaceScoreTable.setWidget(0, 2, csstHeadLbl3);
		
		LabelWithTooltip sizesHeadLbl1 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_cores(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_cores_hint());
		sizesHeadLbl1.addStyleName("eppic-residues-summary-fields");
		sizesTable.setWidget(0, 0, sizesHeadLbl1);
		
		LabelWithTooltip sizesHeadLbl2 = new LabelWithTooltip(AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_rims(), 
				AppPropertiesManager.CONSTANTS.interfaces_residues_summary_sizes_rims_hint());
		sizesHeadLbl2.addStyleName("eppic-residues-summary-fields");
		sizesTable.setWidget(0, 1, sizesHeadLbl2);
	}
	
	private void calculateData(PDBScoreItem pdbScoreItem,
			int selectedInterfaceId,
			List<InterfaceResidueItem> interfaceResidueItems) {

		int interfId = selectedInterfaceId - 1;
		
		entropyCoreValue = Double.NaN;
		entropyRimValue = Double.NaN;
		entropyRatioValue = Double.NaN;
		entropySurfSamplingMean = Double.NaN;
		entropySurfSamplingSd = Double.NaN;
		entropyZscore = Double.NaN;
		
		coreSize = 0;
		rimSize = 0;

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

		for(InterfaceResidueItem interfResItem : interfaceResidueItems) {

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

	}

	/**
	 * Cleans content of residues summary grid.
	 */
	public void cleanResiduesGrid()
	{
		coreRimScoreTable.setWidget(2, 0, new Label(" "));
		coreRimScoreTable.setWidget(2, 1, new Label(" "));
		coreRimScoreTable.setWidget(2, 2, new Label(" "));
		
		coreSurfaceScoreTable.setWidget(2, 0, new Label(" "));
		coreSurfaceScoreTable.setWidget(2, 1, new Label(" "));
		coreSurfaceScoreTable.setWidget(2, 2, new Label(" "));
		
		sizesTable.setWidget(1, 0, new Label(" "));
		sizesTable.setWidget(1, 1, new Label(" "));
	}
	
	
}
