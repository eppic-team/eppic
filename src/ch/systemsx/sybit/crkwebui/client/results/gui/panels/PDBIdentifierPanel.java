package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
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
	private Label warningLabel;
	
	public PDBIdentifierPanel()
	{
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
			  			   double rfreeValue,
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
		
		if(ApplicationContext.getSettings().getResolutionCutOff() > 0 && 
				resolution > ApplicationContext.getSettings().getResolutionCutOff() && resolution > 0){			
			warningLabel = new LabelWithTooltip(
					AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_lowRes(),
					AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_lowRes_hint(), 
					ApplicationContext.getWindowData(), 
					100);
		}else if(ApplicationContext.getSettings().getRfreeCutOff() > 0 && 
				rfreeValue > ApplicationContext.getSettings().getRfreeCutOff() && rfreeValue > 0){
			warningLabel = new LabelWithTooltip(
					AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_highRfree(),
					AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_highRfree_hint(), 
					ApplicationContext.getWindowData(), 
					100);
		}else
			warningLabel = null;
		
		if(warningLabel != null){
			warningLabel.addStyleName("eppic-header-warning");
			this.add(warningLabel);
		}
		
		this.layout(true);
	}
}
