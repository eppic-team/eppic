package ch.systemsx.sybit.crkwebui.client.commons.gui.labels;

import com.google.gwt.user.client.ui.HTML;

public class EppicLabel extends HTML {
	
	/**
	 * Creates a blank Label
	 */
	public EppicLabel(){
		super();
		this.addStyleName("eppic-default-font");
	}
	
	/**
	 * Creates a label with text 
	 * @param text
	 */
	public EppicLabel(String text){
		super();
		this.addStyleName("eppic-default-font");
		setLabelText(text);
	}
	
	/**
	 * Sets the text of the label
	 * @param text
	 */
	public void setLabelText(String text){
		this.setHTML(text);
	}

}
