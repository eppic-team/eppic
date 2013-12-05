package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;

/**
 * Panel used to store pdb identifier.
 * @author AS
 *
 */
public class PDBIdentifierPanel extends HorizontalLayoutContainer
{
	private HorizontalLayoutContainer informationLabel;
	private LinkWithTooltip pdbNameLabel;
	private LabelWithTooltip warningLabel;
	
	public PDBIdentifierPanel()
	{
		this.addStyleName("eppic-pdb-identifier-label");
		
		informationLabel = new HorizontalLayoutContainer();
		informationLabel.setWidth(235);
		informationLabel.add(new HTML(
				EscapedStringGenerator.generateSafeHtml(
				EscapedStringGenerator.generateEscapedString(
						AppPropertiesManager.CONSTANTS.info_panel_pdb_identifier() + ": "))));
		this.add(informationLabel, new HorizontalLayoutData(-1,-1));
		
		pdbNameLabel = new LinkWithTooltip();
		this.add(pdbNameLabel);
		
		warningLabel = new LabelWithTooltip();
		warningLabel.addStyleName("eppic-header-warning");
		this.add(warningLabel);
		
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
		
		if(inputType == InputType.PDBCODE.getIndex())
		{
			pdbNameLabel.setLinkData(EscapedStringGenerator.generateEscapedString(pdbName),
								AppPropertiesManager.CONSTANTS.pdb_identifier_panel_label_hint(),
								 ApplicationContext.getSettings().getPdbLinkUrl() + pdbName);
		}
		else
		{
			pdbNameLabel.setLinkData(EscapedStringGenerator.generateEscapedString(pdbName),"","");
		}
		
		// TODO we should try to set a constant "always-low-res exp method=ELECTRON MICROSCOPY". 
		// It's not ideal that the name is hard-coded
		if (expMethod!=null && expMethod.equals("ELECTRON MICROSCOPY")) {
			warningLabel.setData(AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_lowRes(),
					AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_lowRes_hint());			
		}
		else if(ApplicationContext.getSettings().getResolutionCutOff() > 0 && 
				resolution > ApplicationContext.getSettings().getResolutionCutOff() && resolution > 0) {			
			warningLabel.setData(AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_lowRes(),
					AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_lowRes_hint());
		}else if(ApplicationContext.getSettings().getRfreeCutOff() > 0 && 
				rfreeValue > ApplicationContext.getSettings().getRfreeCutOff() && rfreeValue > 0){
			warningLabel.setData(AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_highRfree(),
					AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_highRfree_hint());
		}else{
			warningLabel.setData("","");
		}
		
	}
}
