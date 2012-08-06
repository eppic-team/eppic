package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
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
			pdbNameLabel = new LinkWithTooltip(pdbName,
											   AppPropertiesManager.CONSTANTS.pdb_identifier_panel_label_hint(),
											   ApplicationContext.getWindowData(), 
											   0, 
											   ApplicationContext.getSettings().getPdbLinkUrl() + pdbName);
		}
		else
		{
			pdbNameLabel = new Label(pdbName);
		}
		
		this.add(pdbNameLabel);
		
		if (expMethod!=null) {
			String labelStr = " (";
			if (!expMethod.equals("X-RAY DIFFRACTION")) {
				labelStr+=expMethod;
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
				labelStr+=spaceGroup;
			}
			labelStr += ")";
			if (!labelStr.equals(" ()")) {
				spaceGroupLabel = new Label(labelStr);
				spaceGroupLabel.addStyleName("eppic-pdb-spacegroup-label");
				this.add(spaceGroupLabel);
			}
			
		}
		
		this.layout(true);
	}
}
