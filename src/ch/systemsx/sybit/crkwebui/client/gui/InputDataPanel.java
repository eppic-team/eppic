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
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
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
import com.google.gwt.event.dom.client.KeyCodes;

/**
 * The panel used to submit new job
 * @author srebniak_a
 *
 */
public class InputDataPanel extends DisplayPanel
{
	private RecaptchaPanel recaptchaPanel;
	
	private FormPanel formPanel;
	
	private RadioGroup inputRadioGroup;
	private Radio pdbCodeRadio;
	private Radio pdbFileRadio;
	
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
		pdbCodeRadio.setBoxLabel(MainController.CONSTANTS.input_pdb_code_radio());  
		pdbCodeRadio.setValue(true);  
	  
	    pdbFileRadio = new Radio();  
	    pdbFileRadio.setBoxLabel(MainController.CONSTANTS.input_upload_file_radio());  
	  
	    inputRadioGroup = new RadioGroup();  
	    inputRadioGroup.setFieldLabel(MainController.CONSTANTS.input_pdb_input_type());  
	    inputRadioGroup.add(pdbCodeRadio);  
	    inputRadioGroup.add(pdbFileRadio);  
	    inputRadioGroup.addListener(Events.Change, new Listener<BaseEvent>(){
	        public void handleEvent(BaseEvent be) 
	        {
	        	String selectedType = inputRadioGroup.getValue().getBoxLabel();
	        	
	        	if(selectedType.equals(MainController.CONSTANTS.input_pdb_code_radio()))
	        	{
	        		file.setVisible(false);
	        		file.setAllowBlank(true);
	        		pdbCodeField.setVisible(true);
	        		pdbCodeField.setAllowBlank(false);
	        	}
	        	else if(selectedType.equals(MainController.CONSTANTS.input_upload_file_radio()))
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
		pdbCodeField.setFieldLabel(MainController.CONSTANTS.input_pdb_code_radio());
		pdbCodeField.setValidator(new PdbCodeFieldValidator());
		pdbCodeField.setAllowBlank(false);
		pdbCodeField.addKeyListener(new KeyListener(){
			public void componentKeyPress(ComponentEvent event)
			{
				if(event.getKeyCode() == KeyCodes.KEY_ENTER)
				{
					submitForm();
				}
			}
		});
		generalFieldSet.add(pdbCodeField);
		
		emailTextField = new TextField<String>();
		emailTextField.setName("email");
		emailTextField.setFieldLabel(MainController.CONSTANTS.input_email());
		emailTextField.setValidator(new EmailFieldValidator());
		emailTextField.addKeyListener(new KeyListener(){
			public void componentKeyPress(ComponentEvent event)
			{
				if(event.getKeyCode() == KeyCodes.KEY_ENTER)
				{
					submitForm();
				}
			}
		});
		generalFieldSet.add(emailTextField);

		FormPanel breakPanel = new FormPanel();
		breakPanel.getHeader().setVisible(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		generalFieldSet.add(breakPanel);

		optionsInputPanel = new OptionsInputPanel(
				mainController.getSettings().getDefaultParametersValues(),
				mainController.getSettings().getReducedAlphabetList(),
				mainController.getSettings().getSearchModeList(),
				mainController.getSettings().getScoresTypes());
		
		generalFieldSet.add(optionsInputPanel);
		optionsInputPanel.collapse();

		formPanel.addListener(Events.Submit, new Listener<FormEvent>()
		{
			public void handleEvent(FormEvent formEvent)
			{
				String result = formEvent.getResultHtml();
				
				if((result != null) && (result.startsWith("crkupres:")))
				{
					result = result.replaceAll("\n", "");
					result = result.trim();
					result = result.substring(9);
					runJob(result);
				}
				else if((result != null) && (result.startsWith("err:")))
				{
					result = result.replaceAll("\n", "");
					result = result.trim();
					result = result.substring(4);
					mainController.showError(result);
					mainController.hideWaiting();
				}
				else
				{
					mainController.showError(MainController.CONSTANTS.input_submit_error());
					mainController.hideWaiting();
				}
			}
		});

		Button resetButton = new Button(MainController.CONSTANTS.input_reset());
		resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) 
			{
				emailTextField.setValue("");
				
				file.setAllowBlank(true);
				pdbCodeField.setAllowBlank(true);
				pdbCodeField.setValue("");
				file.reset();
				
				file.setVisible(false);
        		file.setAllowBlank(true);
        		pdbCodeField.setVisible(true);
        		pdbCodeField.setAllowBlank(false);
        		
        		pdbCodeRadio.setValue(true);
				
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
				submitForm();
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
		
		String input = null;
		
		if(pdbFileRadio.getValue())
		{
			input = file.getValue();
			
			if(input.startsWith("C:\\fakepath\\"))
			{
				input = input.substring(12);
			}
			else if(input.contains("\\"))
			{
				input = input.substring(input.lastIndexOf("\\") + 1);
			}
		}
		else
		{
			input = pdbCodeField.getValue();
			
			if(input != null)
			{
				input = input.toLowerCase();
			}
		}
		
		runJobData.setInput(input);
		runJobData.setJobId(jobId);
		runJobData.setInputParameters(optionsInputPanel
				.getCurrentInputParameters());

		mainController.hideWaiting();
		mainController.runJob(runJobData);
	}
	
	public void submitForm()
	{
		if(!optionsInputPanel.checkIfAnyMethodSelected())
		{
			MessageBox.info(MainController.CONSTANTS.input_submit_form_invalid_header(),
							MainController.CONSTANTS.input_submit_form_no_methods_selected(),
							null);
		}
		else if (formPanel.isValid())
		{
			mainController.showWaiting(MainController.CONSTANTS.input_submit_waiting_message());
			
			if(pdbFileRadio.getValue())
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
			MessageBox.info(MainController.CONSTANTS.input_submit_form_invalid_header(),
							MainController.CONSTANTS.input_submit_form_invalid_message(),
							null);
		}
	}
}
