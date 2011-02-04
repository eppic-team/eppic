package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;

public class InputDataPanel extends FormPanel 
{
	private MainController mainController;
	
	private InputDataPanel thisPanel;
	
	public InputDataPanel(MainController mainController)
	{
		this.mainController = mainController;
		
		init();
	}
	
	public void init()
	{
		thisPanel = this;
		
//	    this.setHeading("File Upload");  
//	    this.setFrame(true);  
	    this.getHeader().setVisible(false);
	    this.setBorders(true);
	    this.setBodyBorder(false);
	    this.setAction(GWT.getModuleBaseURL() + "fileUpload");  
	    this.setEncoding(Encoding.MULTIPART);  
	    this.setMethod(Method.POST);  
	    this.setButtonAlign(HorizontalAlignment.CENTER); 
	    this.setWidth(500);  
	    this.setPadding(20);
	  
	    FormLayout layout = new FormLayout();  
	    layout.setLabelWidth(170); 
	  
	    FieldSet generalFieldSet = new FieldSet();  
	    generalFieldSet.setBorders(false);
	    generalFieldSet.setLayout(layout);
	    
	    TextField<String> emailTextField = new TextField<String>();  
	    emailTextField.setName("email");
	    emailTextField.setFieldLabel("Email");  
	    generalFieldSet.add(emailTextField);  
	  
	    FileUploadField file = new FileUploadField();
	    file.setWidth(200);
	    file.setAllowBlank(false);  
	    file.setName("uploadFormElement");  
	    file.setFieldLabel("PDB File");  
	    generalFieldSet.add(file);  
	    
	    FormPanel breakPanel = new FormPanel();
	    breakPanel.getHeader().setVisible(false);
	    breakPanel.setBodyBorder(false);
	    breakPanel.setBorders(false);
	    generalFieldSet.add(breakPanel);
	    
	    OptionsInputPanel optionsInputPanel = new OptionsInputPanel();
	    generalFieldSet.add(optionsInputPanel);
	    optionsInputPanel.collapse();
	  
	    this.addListener(Events.Submit, new Listener<FormEvent>() 
		{
	        public void handleEvent(FormEvent formEvent) 
	        {
	        	mainController.getJobsForCurrentSession();
	        }
	    });
	    
	    Button submitButton = new Button("Submit");  
	    submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() 
		{  
	    	public void componentSelected(ButtonEvent ce) 
	    	{  
//	    		if (thisPanel.isValid()) 
	    		{  
	    			thisPanel.submit(); 
	    		}  
//	    		else
//	    		{
//	    			MessageBox.info("Action", "Can not upload file", null); 
//	    		}
	    	}  
	    });  
	    
	    this.addButton(submitButton);  
	    
	    this.add(generalFieldSet);
	}
}
