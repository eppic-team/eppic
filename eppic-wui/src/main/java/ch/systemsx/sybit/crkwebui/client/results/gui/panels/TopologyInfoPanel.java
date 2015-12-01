package ch.systemsx.sybit.crkwebui.client.results.gui.panels;


import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.managers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.FieldSet;

public class TopologyInfoPanel extends FieldSet {
	
	private static final int PANEL_WIDTH = 370;
	
	public static HTML assembly_info;
	
	public TopologyInfoPanel(PdbInfo pdbInfo){
		
		this.setHeadingHtml("Topology");
		 
		this.setBorders(true);
		this.setWidth(PANEL_WIDTH);
		
		this.addStyleName("eppic-rounded-border");
		this.addStyleName("eppic-info-panel");
		
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		
		HorizontalLayoutContainer imagesContainer = new HorizontalLayoutContainer();  
		imagesContainer.setHeight(75);
    
		ImageWithTooltip mockupImage1 = new ImageWithTooltip("resources/icons/mockup3.png", null, "tooltip 1");
    	HTML spacer2 = new HTML("<div style='width:10px'></div>");
		ImageWithTooltip mockupImage2 = new ImageWithTooltip("resources/icons/mockup4.png", null, "tooltip 2");
		
		mockupImage1.addClickHandler(new ClickHandler() {		
			@Override
			public void onClick(ClickEvent event) {
				ViewerRunner.runViewer(ApplicationContext.getSelectedAssemblyId()+"");
			}
			
		}); 
		
		mockupImage2.addClickHandler(new ClickHandler() {		
			@Override
			public void onClick(ClickEvent event) {
				Window.alert("clicking on image 2");
			}
		
		});		
		
		imagesContainer.add(mockupImage1);
		imagesContainer.add(spacer2);
		imagesContainer.add(mockupImage2);
		mainContainer.add(imagesContainer);
		//HTML spacer3 = new HTML("<div style='height:5px'></div>");
		//mainContainer.add(spacer3);
		
    	HorizontalLayoutContainer linkContainer = new HorizontalLayoutContainer();
    	HTML link = new HTML("<a href='www.google.com'>The link goes here</a>");
   
    	linkContainer.add(link);
		
		mainContainer.add(linkContainer);
		
    	this.setWidget(mainContainer);

    	
		
	}

}
