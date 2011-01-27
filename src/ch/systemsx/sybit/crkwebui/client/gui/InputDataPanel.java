package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

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
	    this.setWidth(350);  
	  
	    TextField<String> emailTextField = new TextField<String>();  
	    emailTextField.setName("email");
	    emailTextField.setFieldLabel("Email");  
	    this.add(emailTextField);  
	  
	    FileUploadField file = new FileUploadField();  
	    file.setAllowBlank(false);  
	    file.setName("uploadFormElement");  
	    file.setFieldLabel("PDB File");  
	    this.add(file);  
	  
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
	    		if (thisPanel.isValid()) 
	    		{  
	    			thisPanel.submit(); 
	    		}  
	    		else
	    		{
	    			MessageBox.info("Action", "Can not upload file", null); 
	    		}
	    	}  
	    });  
	    
	    this.addButton(submitButton);  
	}
}
