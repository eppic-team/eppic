package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

/**
 * Panel used to store pdb identifier.
 * @author AS
 *
 */
public class PDBIdentifierPanel extends LayoutContainer
{
	private Label informationLabel;
	private Label pdbNameLabel;
	private Label spaceGroupLabel;
	
	public PDBIdentifierPanel()
	{
		this.setHeight(25);
		this.addStyleName("eppic-pdb-identifier-label");
	}
	
	/**
	 * Sets values of pdb identifier.
	 * @param pdbName pdb name
	 * @param spaceGroup space group
	 * @param expMethod method
	 * @param resolution resolution
	 * @param inputType type of the input - this information is used to determine whether link to pdb description should be added
	 */
	public void setPDBText(String pdbName,
			  			   String spaceGroup,
			  			   String expMethod,
			  			   double resolution,
			  			   int inputType)
	{
		this.removeAll();
		
		informationLabel = new Label(AppPropertiesManager.CONSTANTS.info_panel_pdb_identifier() + ": ");
		this.add(informationLabel);
		
		if(inputType == InputType.PDBCODE.getIndex())
		{
			pdbNameLabel = new LinkWithTooltip(EscapedStringGenerator.generateEscapedString(pdbName),
											   AppPropertiesManager.CONSTANTS.pdb_identifier_panel_label_hint(),
											   ApplicationContext.getWindowData(), 
											   0, 
											   ApplicationContext.getSettings().getPdbLinkUrl() + pdbName);
		}
		else
		{
			pdbNameLabel = new Label(EscapedStringGenerator.generateEscapedString(pdbName));
		}
		
		this.add(pdbNameLabel);
		
		if (expMethod!=null) {
			String labelStr = " (";
			if (!expMethod.equals("X-RAY DIFFRACTION")) {
				labelStr += EscapedStringGenerator.generateEscapedString(expMethod);
			}
			if (resolution>0) {
				if (!expMethod.equals("X-RAY DIFFRACTION")) labelStr+=" - "; 					
				labelStr+= resolution+"Å";//String.format("%4.2fÅ",resolution); // gwt compiler doesn't like String.format, anyway works without it
			}
			if(spaceGroup != null && 
					(expMethod.equals("X-RAY DIFFRACTION") || 
					 expMethod.equals("NEUTRON DIFFRACTION") || 
					 expMethod.equals("ELECTRON CRYSTALLOGRAPHY")))
			{
				if (resolution>0 || !expMethod.equals("X-RAY DIFFRACTION")) labelStr+=" - ";
				labelStr += EscapedStringGenerator.generateEscapedString(spaceGroup);
			}
			labelStr += ")";
			if (!labelStr.equals(" ()")) {
				spaceGroupLabel = new Label(labelStr);
				spaceGroupLabel.addStyleName("eppic-pdb-spacegroup-label");
				this.add(spaceGroupLabel);
			}
			
		} else {
			// if no exp method defined but there is a space group and a resol>0, then we hope this is x-ray or at least some kind of xtalography 
			// this happens for instance in phenix PDB files (no exp method, but both space group and resol are present)
			if (spaceGroup!=null && resolution>0) {
				String labelStr = " ("+resolution+"Å - " + EscapedStringGenerator.generateEscapedString(spaceGroup) + ")";
				spaceGroupLabel = new Label(labelStr);
				spaceGroupLabel.addStyleName("eppic-pdb-spacegroup-label");
				this.add(spaceGroupLabel);				
			}
		}
		
		this.layout(true);
	}
}
