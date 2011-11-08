package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.Label;

public class PDBIdentifierLabel extends Label{

	public PDBIdentifierLabel()
	{
		this.addStyleName("pdb-identifier-label");
	}
	
	public PDBIdentifierLabel(String pdbName,
							  String spaceGroup)
	{
		this.setText(preparePDBIdentifierText(pdbName, spaceGroup));
		this.addStyleName("pdb-identifier-label");
	}

	public void setPDBText(String pdbName,
			  			   String spaceGroup)
	{
		this.setText(preparePDBIdentifierText(pdbName, spaceGroup));
	}

	private String preparePDBIdentifierText(String pdbName,
			   								String spaceGroup)
	{
		StringBuffer text = new StringBuffer();
		
		text.append(MainController.CONSTANTS.info_panel_pdb_identifier() + 
					": " + 
					pdbName);

		if(spaceGroup != null)
		{
			text.append(" (" +
						spaceGroup +
						")");
		}
		
		return text.toString();
	}
}
