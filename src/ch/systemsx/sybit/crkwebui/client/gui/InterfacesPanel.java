package ch.systemsx.sybit.crkwebui.client.gui;

import model.PdbScore;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class InterfacesPanel extends FormPanel 
{
	private StructurePanel firstStructure;
	private StructurePanel secondStructure;
	
	public InterfacesPanel(PdbScore resultsData)
	{
		FormData formData = new FormData("100%");  
	//	infoPanel.setFrame(true);  
		this.getHeader().setVisible(false);  
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
		firstStructure = new StructurePanel(resultsData, "Structure 1");
		secondStructure = new StructurePanel(resultsData, "Structure 2");
		   
	 	this.add(firstStructure, new RowData(0.45, 1, new Margins(0)));
	 	
	 	FormPanel breakPanel = new FormPanel();
	 	breakPanel.setBodyBorder(false);
	 	breakPanel.setBorders(false);
	 	breakPanel.getHeader().setVisible(false);
	 	this.add(breakPanel, new RowData(0.1, 1, new Margins(0)));
	 	
	 	this.add(secondStructure, new RowData(0.45, 1, new Margins(0)));  
	}
}
