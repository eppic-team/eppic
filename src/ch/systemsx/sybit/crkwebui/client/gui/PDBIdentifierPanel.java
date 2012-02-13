package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

public class PDBIdentifierPanel extends LayoutContainer
{
	private MainController mainController;
	
	private Label informationLabel;
	private Label pdbNameLabel;
	private Label spaceGroupLabel;
	
	public PDBIdentifierPanel(MainController mainController)
	{
		this.setHeight(20);
		this.addStyleName("pdb-identifier-label");
		this.mainController = mainController;
	}
	
	public void setPDBText(String pdbName,
			  			   String spaceGroup,
			  			   int inputType)
	{
		this.removeAll();
		
		informationLabel = new Label(MainController.CONSTANTS.info_panel_pdb_identifier() + ": ");
		this.add(informationLabel);
		
		if(inputType == InputType.PDBCODE.getIndex())
		{
			pdbNameLabel = new LinkWithTooltip(pdbName,
											   MainController.CONSTANTS.pdb_identifier_panel_label_hint(),
											   mainController, 
											   0, 
											   mainController.getSettings().getPdbLinkUrl() + pdbName);
		}
		else
		{
			pdbNameLabel = new Label(pdbName);
		}
		
		this.add(pdbNameLabel);
		
		if(spaceGroup != null)
		{
			spaceGroupLabel = new Label(" (" +
										spaceGroup +
										")");
			
			this.add(spaceGroupLabel);
		}
		
		this.layout(true);
	}
}
