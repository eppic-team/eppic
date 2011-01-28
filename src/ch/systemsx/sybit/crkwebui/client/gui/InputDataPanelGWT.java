package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class InputDataPanelGWT extends FormPanel 
{
	private FormPanel thisForm;
	
	private TextBox emailTextBox;
	
	private HTML resultLabel;
	
	private MainController mainController;
	
	public InputDataPanelGWT(MainController mainController)
	{
		this.mainController = mainController;
	    this.setAction(GWT.getModuleBaseURL() + "fileUpload");
	    this.setEncoding(FormPanel.ENCODING_MULTIPART);
	    this.setMethod(FormPanel.METHOD_POST);
	    this.setWidget(createInputDataForm());
	    
	    thisForm = this;
	}

	private Widget createInputDataForm()
	{
	    FlexTable layout = new FlexTable();
	    layout.setCellSpacing(6);
	    layout.setWidth("500px");
	    FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

	    // Add a title to the form
	    layout.setHTML(0, 0, "CRK");
	    cellFormatter.setColSpan(0, 0, 2);
	    cellFormatter.setHorizontalAlignment(
	        0, 0, HasHorizontalAlignment.ALIGN_CENTER);

	    // Create a FileUpload widget.
	    final FileUpload upload = new FileUpload();
	    upload.setName("uploadFormElement");
	    
	    layout.setHTML(1, 0, "PDB file");
	    layout.setWidget(1, 1, upload);
	    
	    Button submitButton = new Button(
	        "Submit", new ClickHandler() 
	        {
	        	public void onClick(ClickEvent event) 
	        	{
	        		thisForm.submit();
	        	}
	        });
	    
	    
	    emailTextBox = new TextBox();
	    emailTextBox.setName("email");
	    layout.setHTML(2, 0, "Email");
	    layout.setWidget(2, 1, emailTextBox);

	    Grid advancedOptions = new Grid(2, 2);
	    advancedOptions.setCellSpacing(6);
	    advancedOptions.setHTML(0, 0, "Parameter 1");
	    advancedOptions.setWidget(0, 1, new TextBox());

	    DisclosurePanel advancedDisclosure = new DisclosurePanel("Advanced");
	    advancedDisclosure.setAnimationEnabled(true);
	    advancedDisclosure.ensureDebugId("cwDisclosurePanel");
	    advancedDisclosure.setContent(advancedOptions);
	    layout.setWidget(3, 0, advancedDisclosure);
	    cellFormatter.setColSpan(3, 0, 2);
	    
	    layout.setWidget(4, 0, submitButton);
	    cellFormatter.setColSpan(4, 0, 2);
	    cellFormatter.setHorizontalAlignment(
		        4, 0, HasHorizontalAlignment.ALIGN_CENTER);
	    
	    resultLabel = new HTML();
	    layout.setWidget(5, 0, resultLabel);
	    cellFormatter.setColSpan(5, 0, 2);
	    cellFormatter.setHorizontalAlignment(
		        5, 0, HasHorizontalAlignment.ALIGN_CENTER);
	    
	    DecoratorPanel decoratedPanel = new DecoratorPanel();
	    decoratedPanel.setWidget(layout);
	    
	    this.addSubmitHandler(new SubmitHandler() {
			
			@Override
			public void onSubmit(SubmitEvent event) 
			{
				if ((upload.getFilename() == null) ||
					(upload.getFilename().length() == 0)) 
				{
					Window.alert("Please select the file");
					event.cancel();
			    }
				
			}
		});
	    
	    this.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) 
			{
				String strippedResponse = event.getResults();
				strippedResponse = strippedResponse.replace("<pre>", "");
				strippedResponse = strippedResponse.replace("</pre>", "");
//				resultLabel.setHTML("Results can be downloaded from: " + GWT.getModuleBaseURL() + "fileDownload?id=" + strippedResponse);
				
				mainController.getJobsForCurrentSession();
			}
		});
	    
	    return decoratedPanel;
	}
}
