package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.AnchorData;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;

public class StructurePanel extends LayoutContainer
{
	private Label header;
	private ResiduesPanel residuesPanel;
	private ResiduesSummaryPanel residuesSummaryPanel;
	
	public StructurePanel(int width,
						  int structureNr)
	{
		this.setScrollMode(Scroll.AUTOX);
		this.setLayout(new AnchorLayout());
		
		header = new Label();
		LayoutContainer structureHeaderPanel = createStructureHeaderPanel(header);
		this.add(structureHeaderPanel);
		
		residuesPanel = new ResiduesPanel(width);
		residuesPanel.setScrollMode(Scroll.NONE);
		this.add(residuesPanel, new AnchorData("none -155"));
		
		FormPanel breakPanel = createBreakPanel();
		this.add(breakPanel);

		residuesSummaryPanel = new ResiduesSummaryPanel("", structureNr);
		residuesSummaryPanel.setHeight(90);
		residuesSummaryPanel.setScrollMode(Scroll.NONE);
		this.add(residuesSummaryPanel);
	}
	
	/**
	 * Creates panel containing nr of the structure.
	 * @param structureHeader panel with header pointing nr of the structure
	 * @return header panel
	 */
	private LayoutContainer createStructureHeaderPanel(Label structureHeader)
	{
		VBoxLayout layout = new VBoxLayout();
	    layout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
	    
		LayoutContainer structureHeaderPanel = new LayoutContainer();
		structureHeaderPanel.setHeight(20);
		structureHeaderPanel.setLayout(layout);
		structureHeaderPanel.add(structureHeader);
		return structureHeaderPanel;
	}

	/**
	 * Creates panel used as break between row.
	 * @return break panel
	 */
	private FormPanel createBreakPanel()
	{
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
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
		
		residuesSummaryPanel.fillResiduesGrid(pdbScoreItem,
					 selectedInterface,
					 interfaceResidueItems);
	}
	
	/**
	 * Fills header of structure panels.
	 * @param chainName name of the structure
	 */
	public void fillHeader(String chainName) 
	{
		header.setText(AppPropertiesManager.CONSTANTS.interfaces_residues_panel_structure() + " " + 
									 EscapedStringGenerator.generateEscapedString(chainName));
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
	 * @param assignedWidth
	 * @param assignedHeight
	 */
	public void resizeResiduesPanels(int assignedWidth, int assignedHeight) 
	{
		residuesPanel.resizeGrid(assignedWidth);
		residuesSummaryPanel.resizeGrid(assignedWidth);
		this.layout(true);
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
