package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.validators.EmailFieldValidator;
import ch.systemsx.sybit.crkwebui.client.gui.validators.PdbCodeFieldValidator;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;

public class InputDataPanel extends DisplayPanel
{
	private RecaptchaPanel recaptchaPanel;
	
	private FormPanel formPanel;
	
	private RadioGroup inputRadioGroup;
	private Radio pdbCodeRadio;
	private Radio pdbCodeFile;
	
	private FileUploadField file;
	private TextField<String> pdbCodeField;
	private TextField<String> emailTextField;
	
	private OptionsInputPanel optionsInputPanel; 

	public InputDataPanel(MainController mainController) 
	{
		super(mainController);
		init();
	}

	public void init() 
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		this.setBorders(false);
		this.setScrollMode(Scroll.AUTO);
//		this.setBodyBorder(false);
//		this.getHeader().setVisible(false);
		
		formPanel = new FormPanel();

		formPanel.getHeader().setVisible(false);
		formPanel.setBorders(true);
		formPanel.setBodyBorder(false);
		formPanel.setAction(GWT.getModuleBaseURL() + "fileUpload");
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setWidth(500);
		formPanel.setAutoHeight(true);
		formPanel.setPadding(20);

		FormLayout layout = new FormLayout();
		layout.setLabelWidth(170);

		FieldSet generalFieldSet = new FieldSet();
		generalFieldSet.setBorders(false);
		generalFieldSet.setLayout(layout);

		pdbCodeRadio = new Radio();  
		pdbCodeRadio.setBoxLabel("PDB Code");  
		pdbCodeRadio.setValue(true);  
	  
	    pdbCodeFile = new Radio();  
	    pdbCodeFile.setBoxLabel("Upload file");  
	  
	    inputRadioGroup = new RadioGroup();  
	    inputRadioGroup.setFieldLabel("Input type");  
	    inputRadioGroup.add(pdbCodeRadio);  
	    inputRadioGroup.add(pdbCodeFile);  
	    inputRadioGroup.addListener(Events.Change, new Listener<BaseEvent>(){
	        public void handleEvent(BaseEvent be) 
	        {
	        	String selectedType = inputRadioGroup.getValue().getBoxLabel();
	        	
	        	if(selectedType.equals("PDB Code"))
	        	{
	        		file.setVisible(false);
	        		file.setAllowBlank(true);
	        		pdbCodeField.setVisible(true);
	        		pdbCodeField.setAllowBlank(false);
	        	}
	        	else if(selectedType.equals("Upload file"))
	        	{
	        		file.setVisible(true);
	        		file.setAllowBlank(false);
	        		pdbCodeField.setVisible(false);
	        		pdbCodeField.setAllowBlank(true);
	        	}
	        	
	        	formPanel.layout();
	        }
	    });
	    
	    generalFieldSet.add(inputRadioGroup); 
	    
		file = new FileUploadField();
		file.setWidth(200);
		file.setAllowBlank(true);
		file.setName("uploadFormElement");
		file.setFieldLabel(MainController.CONSTANTS.input_file());
		file.setVisible(false);
		generalFieldSet.add(file);
		
		pdbCodeField = new TextField<String>();
		pdbCodeField.setName("code");
		pdbCodeField.setFieldLabel("PDB Code");
		pdbCodeField.setValidator(new PdbCodeFieldValidator());
//		emailTextField.setStyleAttribute("padding", "30px");
		generalFieldSet.add(pdbCodeField);
		
		emailTextField = new TextField<String>();
		emailTextField.setName("email");
		emailTextField.setFieldLabel(MainController.CONSTANTS.input_email());
		emailTextField.setValidator(new EmailFieldValidator());
//		emailTextField.setStyleAttribute("padding", "30px");
		generalFieldSet.add(emailTextField);

		FormPanel breakPanel = new FormPanel();
		breakPanel.getHeader().setVisible(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		generalFieldSet.add(breakPanel);

		optionsInputPanel = new OptionsInputPanel(
				mainController.getSettings().getDefaultParametersValues(),
				mainController.getSettings().getReducedAlphabetList(),
				mainController.getSettings().getScoresTypes(),
				mainController.getWindowHeight());
		generalFieldSet.add(optionsInputPanel);
		optionsInputPanel.collapse();

		formPanel.addListener(Events.Submit, new Listener<FormEvent>()
		{
			public void handleEvent(FormEvent formEvent)
			{
				String jobId = formEvent.getResultHtml();
				
				jobId = jobId.replaceAll("\n", "");
				jobId = jobId.trim();
				
				runJob(jobId);
			}
		});

		Button resetButton = new Button("Reset");
		resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				emailTextField.setValue("");
				file.setValue("");
				optionsInputPanel.fillDefaultValues(mainController
						.getSettings().getDefaultParametersValues());
			}
		});

		formPanel.addButton(resetButton);

		Button submitButton = new Button(
				MainController.CONSTANTS.input_submit());
		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) 
			{
				if (formPanel.isValid())
				{
					mainController.showWaiting("Submitting");
					
					if(pdbCodeFile.getValue())
					{
						formPanel.submit();
					}
					else
					{
						runJob(null);
					}
				}
				else
				{
					MessageBox.info("Action", "Can not upload file - form contains incorrect values", null);
				}
			}
		});

		formPanel.addButton(submitButton);

		if(mainController.getSettings().isUseCaptcha())
		{
			recaptchaPanel = new RecaptchaPanel(mainController.getSettings().getCaptchaPublicKey());
			
			if(mainController.getNrOfSubmissions() < mainController.getSettings().getNrOfAllowedSubmissionsWithoutCaptcha())
			{
				recaptchaPanel.setVisible(false);
			}
			
			generalFieldSet.add(recaptchaPanel);
		}
		
		formPanel.add(generalFieldSet);
		
		//fix for chrome - otherwise unnecessary scrollbars visible
		this.add(new LayoutContainer(), new RowData(0.5, -1, new Margins(0)));
		this.add(formPanel, new RowData(-1, -1, new Margins(0)));
	}
	
	public RecaptchaPanel getRecaptchaPanel()
	{
		return recaptchaPanel;
	}
	
	private void runJob(String jobId)
	{
		mainController.setNrOfSubmissions(mainController.getNrOfSubmissions() + 1);
		
		RunJobData runJobData = new RunJobData();
		runJobData.setEmailAddress(emailTextField.getValue());
		
		String fileName = null;
		
		if(pdbCodeFile.getValue())
		{
			fileName = file.getValue();
			
			if(fileName.startsWith("C:\\fakepath\\"))
			{
				fileName = fileName.substring(12);
			}
			else if(fileName.contains("\\"))
			{
				fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
			}
		}
		else
		{
			fileName = pdbCodeField.getValue();
		}
		
		runJobData.setFileName(fileName);
		runJobData.setJobId(jobId);
		runJobData.setInputParameters(optionsInputPanel
				.getCurrentInputParameters());

		mainController.hideWaiting();
		mainController.runJob(runJobData);
	}
}
