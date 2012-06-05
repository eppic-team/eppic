package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.validators.EmailFieldValidator;
import ch.systemsx.sybit.crkwebui.client.gui.validators.PdbCodeFieldValidator;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.validators.InputParametersComparator;

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
import com.google.gwt.user.client.History;

/**
 * Panel used to submit new job.
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

	/**
	 * Initalizes content of the panel.
	 */
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
		pdbCodeRadio.setBoxLabel(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		pdbCodeRadio.setValue(true);
		pdbCodeRadio.setLabelStyle("font-size: 14px;");

	    pdbFileRadio = new Radio();
	    pdbFileRadio.setBoxLabel(AppPropertiesManager.CONSTANTS.input_upload_file_radio());
	    pdbFileRadio.setLabelStyle("font-size: 14px;");

	    inputRadioGroup = new RadioGroup();
	    inputRadioGroup.setFieldLabel(AppPropertiesManager.CONSTANTS.input_pdb_input_type());
	    if (AppPropertiesManager.CONSTANTS.input_pdb_input_type()!=null &&
	    		AppPropertiesManager.CONSTANTS.input_pdb_input_type().equals(""))
	    	inputRadioGroup.setLabelSeparator("");
	    inputRadioGroup.add(pdbCodeRadio);
	    inputRadioGroup.add(pdbFileRadio);

	    inputRadioGroup.addListener(Events.Change, new Listener<BaseEvent>(){
	        public void handleEvent(BaseEvent be)
	        {
	        	String selectedType = inputRadioGroup.getValue().getBoxLabel();

	        	if(selectedType.equals(AppPropertiesManager.CONSTANTS.input_pdb_code_radio()))
	        	{
	        		file.setVisible(false);
	        		file.setAllowBlank(true);
	        		file.reset();
	        		pdbCodeField.reset();
	        		pdbCodeField.setAllowBlank(false);
	        		pdbCodeField.setVisible(true);
	        	}
	        	else if(selectedType.equals(AppPropertiesManager.CONSTANTS.input_upload_file_radio()))
	        	{
	        		file.setVisible(true);
	        		file.setAllowBlank(false);
	        		pdbCodeField.setValue("");
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
		file.setFieldLabel(AppPropertiesManager.CONSTANTS.input_file());
		file.setLabelStyle("font-size: 14px;");
		file.setVisible(false);
		generalFieldSet.add(file);

		pdbCodeField = new TextField<String>();
		pdbCodeField.setName("code");
		pdbCodeField.setFieldLabel(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		pdbCodeField.setLabelStyle("font-size: 14px;");
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
		emailTextField.setFieldLabel(AppPropertiesManager.CONSTANTS.input_email());
		emailTextField.setLabelStyle("font-size: 14px;");
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

		optionsInputPanel = new OptionsInputPanel(mainController);

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
					mainController.showError(AppPropertiesManager.CONSTANTS.input_submit_error());
					mainController.hideWaiting();
				}
			}
		});

		Button resetButton = new Button(AppPropertiesManager.CONSTANTS.input_reset());
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
				AppPropertiesManager.CONSTANTS.input_submit());
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

	/**
	 * Starts new job. If file is uploaded then jobId is used to identify uploaded file, otherwise jobId is null.
	 * @param jobId identifier of the job
	 */
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
				input = input.toLowerCase().trim();
			}
		}

		runJobData.setInput(input);
		runJobData.setJobId(jobId);
		runJobData.setInputParameters(optionsInputPanel
				.getCurrentInputParameters());

		mainController.hideWaiting();
		mainController.runJob(runJobData);
	}

	/**
	 * Validates and submits form.
	 */
	public void submitForm()
	{
		if(!optionsInputPanel.checkIfAnyMethodSelected())
		{
			MessageBox.info(AppPropertiesManager.CONSTANTS.input_submit_form_invalid_header(),
							AppPropertiesManager.CONSTANTS.input_submit_form_no_methods_selected(),
							null);
		}
		else if (formPanel.isValid())
		{
			mainController.showWaiting(AppPropertiesManager.CONSTANTS.input_submit_waiting_message());

			if(pdbFileRadio.getValue())
			{
				formPanel.submit();
			}
			else if(InputParametersComparator.checkIfEquals(optionsInputPanel.getCurrentInputParameters(),
															mainController.getSettings().getDefaultParametersValues()))
			{
				mainController.hideWaiting();
				String trimmedJobId = pdbCodeField.getValue().toLowerCase().trim();
				mainController.setSelectedJobId(trimmedJobId);
				History.newItem("id/" + trimmedJobId);
			}
			else
			{
				runJob(null);
			}
		}
		else
		{
			MessageBox.info(AppPropertiesManager.CONSTANTS.input_submit_form_invalid_header(),
							AppPropertiesManager.CONSTANTS.input_submit_form_invalid_message(),
							null);
		}
	}

	/**
	 * Retrieves field containing code of the pdb to use as the source for the job.
	 * @return field containing pdb pode
	 */
	public TextField<String> getPdbCodeField() {
		return pdbCodeField;
	}
}
