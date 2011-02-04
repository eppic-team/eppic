package ch.systemsx.sybit.crkwebui.client.gui;

import model.PdbScore;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class InterfacesWindow extends Dialog 
{
	private InterfacesPanel interfacesPanel;
	
	public InterfacesWindow(PdbScore resultsData)
	{
		this.setSize(1000, 800);  
		this.setPlain(true);  
		this.setModal(true);  
		this.setBlinkModal(true);  
		this.setHeading("Interfaces");  
		this.setLayout(new FitLayout());
		this.setHideOnButtonClick(true);
		
		interfacesPanel = new InterfacesPanel(resultsData);
		this.add(interfacesPanel);
	}
}
