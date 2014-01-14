package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.FormPanel;

public class StructurePanel extends FramedPanel
{
	private ResiduesPanel residuesPanel;
	private ResiduesSummaryPanel residuesSummaryPanel;
	
	public StructurePanel(int structureNr)
	{	
		this.getHeader().getElement().applyStyles("textAlign:center; background:none;");
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		this.setWidget(mainContainer);
		
		mainContainer.setScrollMode(ScrollMode.AUTOX);
		
		residuesSummaryPanel = new ResiduesSummaryPanel(structureNr);
		residuesSummaryPanel.setHeight(80);

		mainContainer.add(residuesSummaryPanel, new VerticalLayoutData(-1, -1, new Margins(0)));
		
		FormPanel breakPanel = createBreakPanel();
		mainContainer.add(breakPanel, new VerticalLayoutData(-1, -1, new Margins(0)));
		
		residuesPanel = new ResiduesPanel();
		mainContainer.add(residuesPanel, new VerticalLayoutData(-1, 1, new Margins(0)));

	}

	/**
	 * Creates panel used as break between row.
	 * @return break panel
	 */
	private FormPanel createBreakPanel()
	{
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setHeight(20);
		return breakPanel;
	}
	
	/**
	 * Fills content of structure panel
	 * @param pdbScoreItem result of calculations
	 * @param selectedInterface interface for which residues are to be displayed
	 * @param interfaceResidueItems residues to display
	 */
	public void fillStructurePanel(PDBScoreItem pdbScoreItem,
								   int selectedInterface,
								   List<InterfaceResidueItem> interfaceResidueItems)
	{
		residuesPanel.fillResiduesGrid(interfaceResidueItems);
		residuesPanel.applyFilter(false);
		
		residuesSummaryPanel.fillResultsSummary(pdbScoreItem,
					 selectedInterface,
					 interfaceResidueItems);
	}
	
	/**
	 * Fills header of structure panels.
	 * @param chainName name of the structure
	 */
	public void fillHeader(String chainName) 
	{
		this.setHeadingHtml(StyleGenerator.defaultFontStyle(
				AppPropertiesManager.CONSTANTS.interfaces_residues_panel_structure() + " " + 
									 EscapedStringGenerator.generateEscapedString(chainName)));
	}
	
	public void increaseActivePage()
	{
		residuesPanel.increaseActivePage();
	}
	
	public void decreaseActivePage()
	{
		residuesPanel.decreaseActivePage();
	}
	
	/**
	 * Resizes interfaces residues panels.
	 */
	public void resizeResiduesPanels() 
	{
		residuesPanel.resizeGrid();
	}
	
	/**
	 * Cleans data of residues filter.
	 */
	public void cleanData()
	{
		residuesPanel.cleanResiduesGrid();
		residuesSummaryPanel.cleanResiduesGrid();
	}
	
	/**
	 * Retrieves panel containing interface residues for structure.
	 * @return panel containing interface residues for structure
	 */
	public ResiduesPanel getResiduesPanel() 
	{
		return residuesPanel;
	}
	
	/**
	 * Retrieves panel containing interface residues summary for structure.
	 * @return panel containing interface residues summary for structure
	 */
	public ResiduesSummaryPanel getResiduesSummaryPanel() 
	{
		return residuesSummaryPanel;
	}
}
